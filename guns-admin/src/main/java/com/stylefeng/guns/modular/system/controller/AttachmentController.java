package com.stylefeng.guns.modular.system.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.base.tips.ErrorTip;
import com.stylefeng.guns.core.base.tips.SuccessTip;
import com.stylefeng.guns.core.base.tips.Tip;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.modular.system.model.Attachment;
import com.stylefeng.guns.modular.system.service.IAttachmentService;
import com.stylefeng.guns.modular.system.transfer.AttachmentInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by 罗华.
 */
@RestController
@RequestMapping("/attachment")
public class AttachmentController {
    private static final String TEMP_MASTER_NAME = "_TEMP_ATTACHMENT_";
    @Autowired
    private IAttachmentService attachmentService;

    @RequestMapping(value = "/upload/async", method = RequestMethod.POST)
    public Tip uploadDirect(MultipartFile[] files){

        AttachmentInfo uploadInfo = AttachmentInfo.fromUpload(files);

        if (null == uploadInfo)
            return new ErrorTip(HttpStatus.BAD_REQUEST.value(), "没有上传文件");

        String code = UUID.randomUUID().toString();
        attachmentService.saveAttachment(uploadInfo, TEMP_MASTER_NAME, code);

        Map<String, String> result = new HashMap<String, String>();
        result.put("name", TEMP_MASTER_NAME);
        result.put("code", code);

        SuccessTip tip = new SuccessTip();
        tip.setData(result);
        return tip;
    }

    @RequestMapping(value = "/upload" ,method = RequestMethod.POST)
    public Tip upload(
            MultipartFile[] files,
            String masterCode,
            String masterName
    ){
        AttachmentInfo uploadInfo = AttachmentInfo.fromUpload(files);

        if (null == uploadInfo)
            return new ErrorTip(HttpStatus.BAD_REQUEST.value(), "没有上传文件");

        String savedIds = attachmentService.saveAttachment(uploadInfo, masterName, masterCode);

        List<Long> ids = new ArrayList<>();
        if (null != savedIds){
            StringTokenizer tokenizer = new StringTokenizer(savedIds, ",");
            while(tokenizer.hasMoreTokens()){
                ids.add(Long.parseLong(tokenizer.nextToken()));
            }
        }

        SuccessTip tip = new SuccessTip();
        tip.setData(ids);
        return tip;
    }

    @RequestMapping(value = "/view/user/avator/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    public void viewAvator( @PathVariable("id")Long id, Integer gender, HttpServletResponse response){
        File zipFile = null;
        String masterName = "USER_AVATOR";

        List<Attachment> attachmentList = attachmentService.listAttachment(masterName, String.valueOf(id));
        Attachment avatorSource = null;

        if (null != attachmentList && attachmentList.size() > 0){
            avatorSource = attachmentList.get(0);
        }

        InputStream avatorStream = null;

        String fileName = "";
        try {
            if (null == avatorSource || null == avatorSource.getPath()) {
                DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
                avatorStream = resourceLoader.getResource("classpath:/avator/boy.gif").getInputStream();
                fileName = "默认头像";
            } else {
                fileName = avatorSource.getFileName();
                zipFile = new File(avatorSource.getPath());
                avatorStream = new FileInputStream(zipFile);
            }

            response.reset();
            response.setContentType("bin");
            response.addHeader("Content-Disposition",
                    "attachment; filename=\"" + new String(URLEncoder.encode(fileName, "UTF-8")) + "\"");
            byte[] buffer = new byte[1024];
            int byteread = 0;
            while ((byteread = avatorStream.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, byteread);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (null != avatorStream)
                try {
                    avatorStream.close();
                } catch (IOException e) {
                }
        }

        return;
    }

    @RequestMapping(value = "/view/icon/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    public void viewUpload( @PathVariable("id")Long id, Integer gender, HttpServletResponse response){
        File zipFile = null;
        String masterName = "ICON_IMG";

        Attachment resource = attachmentService.get(id);

        InputStream resStream = null;

        String fileName = "";
        try {
            if (null == resource || null == resource.getPath()) {
                DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
                resStream = resourceLoader.getResource("classpath:/icon/no-pic.jpg").getInputStream();
                fileName = "默认图标";
            } else {
                fileName = resource.getFileName();
                zipFile = new File(resource.getPath());
                resStream = new FileInputStream(zipFile);
            }

            response.reset();
            response.setContentType("bin");
            response.addHeader("Content-Disposition",
                    "attachment; filename=\"" + new String(URLEncoder.encode(fileName, "UTF-8")) + "\"");
            byte[] buffer = new byte[1024];
            int byteread = 0;
            while ((byteread = resStream.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, byteread);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (null != resStream)
                try {
                    resStream.close();
                } catch (IOException e) {
                }
        }

        return;
    }

    @RequestMapping("/download/app")
    public void download(String platform, HttpServletResponse response) throws IOException {
        File zipFile = null;

        if ("android".equalsIgnoreCase(platform)) {
            response.sendRedirect("http://www.kecui.com.cn/kecui.apk");
        } else if ("ios".equalsIgnoreCase(platform)) {
            //response.sendRedirect("https://itunes.apple.com/cn/app/%E7%A7%91%E8%90%83%E5%9F%B9%E4%BC%98/id1459216538?mt=8");
            response.sendRedirect("https://www.pgyer.com/ftrR");
        }
    }

    @RequestMapping("/download/{id}")
    public void download(@PathVariable("id") Long id, HttpServletResponse response) {
        File zipFile = null;

        Attachment attachment = attachmentService.selectById(id);

        if (null == attachment)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS);

        zipFile = new File(attachment.getPath());
        try {
            // 输出文件
            FileInputStream in = new FileInputStream(zipFile);
            response.reset();
            response.setContentType("bin");
            response.addHeader("Content-Disposition",
                    "attachment; filename=\"" + new String(URLEncoder.encode(attachment.getFileName(), "UTF-8")) + "\"");
            byte[] buffer = new byte[1024];
            int byteread = 0;
            while ((byteread = in.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, byteread);
            }
            in.close();
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    @RequestMapping("/download")
    public void download(String masterName, String masterCode, HttpServletResponse response) {
        File zipFile = null;

        Wrapper<Attachment> attachmentWrapper = new EntityWrapper<>();
        attachmentWrapper.eq("master_name", masterName);
        attachmentWrapper.eq("master_code", masterCode);
        attachmentWrapper.eq("status", GenericState.Valid.code);

        Attachment attachment = attachmentService.selectOne(attachmentWrapper);
        InputStream resStream = null;
        String fileName = "";

        try {
            if (null == attachment) {
                DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
                resStream = resourceLoader.getResource("classpath:/avator/boy.gif").getInputStream();
                fileName = "默认头像";
            } else {
                resStream = new FileInputStream(new File(attachment.getPath()));
                fileName = attachment.getFileName();
            }
        }catch(Exception e){}

        if (null == resStream)
            return ;

        try {
            // 输出文件
            response.reset();
            response.setContentType("bin");
            response.addHeader("Content-Disposition",
                    "attachment; filename=\"" + new String(URLEncoder.encode(fileName, "UTF-8")) + "\"");
            byte[] buffer = new byte[1024];
            int byteread = 0;
            while ((byteread = resStream.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, byteread);
            }

        } catch (Exception e) {
            // e.printStackTrace();
        } finally {
            try {
                resStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
