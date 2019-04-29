package com.stylefeng.guns.modular.teacherMGR.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.common.constant.factory.PageFactory;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.base.controller.BaseController;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.log.LogObjectHolder;
import com.stylefeng.guns.modular.memberMGR.service.IMemberService;
import com.stylefeng.guns.modular.system.model.Attachment;
import com.stylefeng.guns.modular.system.model.Member;
import com.stylefeng.guns.modular.system.model.Teacher;
import com.stylefeng.guns.modular.system.service.IAttachmentService;
import com.stylefeng.guns.modular.teacherMGR.service.TeacherService;
import com.stylefeng.guns.modular.teacherMGR.warpper.TeacherWrapper;
import com.stylefeng.guns.util.PathUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 教师管理控制器
 *
 * @author fengshuonan
 * @Date 2018-10-04 15:55:06
 */
@Controller
@RequestMapping("/teacher")
public class TeacherController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(TeacherController.class);

    private String PREFIX = "/teacherMGR/teacher/";

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private IAttachmentService attachmentService;

    @Autowired
    private IMemberService memberService;

    /**
     * 跳转到教师管理首页
     */
    @RequestMapping("")
    public String index() {
        return PREFIX + "teacher.html";
    }

    /**
     * 跳转到添加教师管理
     */
    @RequestMapping("/teacher_add")
    public String teacherAdd() {
        return PREFIX + "teacher_add.html";
    }

    /**
     * 跳转到修改教师管理
     */
    @RequestMapping("/teacher_update/{teacherId}")
    public String teacherUpdate(@PathVariable Integer teacherId, Model model) {
        Teacher teacher = teacherService.selectById(teacherId);

        if (null == teacher)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND);

        List<Attachment> avatarList = attachmentService.listAttachment(Teacher.class.getSimpleName(), teacher.getCode());
        if (null != avatarList && avatarList.size() > 0)
            teacher.setAvatar(String.valueOf(avatarList.get(0).getId()));

        model.addAttribute("item", teacher);
        LogObjectHolder.me().set(teacher);
        return PREFIX + "teacher_edit.html";
    }

    /**
     * 获取教师管理列表
     */
    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list(@RequestParam Map<String, String> queryParams) {
        Page<Teacher> page = new PageFactory<Teacher>().defaultPage();
        Page<Map<String, Object>> mapPage = teacherService.selectMapsPage(page, new EntityWrapper<Teacher>() {
            {
                //condition条件分页
                if (queryParams.containsKey("condition") && StringUtils.isNotEmpty(queryParams.get("condition"))) {
                    like("name", queryParams.get("condition"));
                    or();
                    eq("code", queryParams.get("condition"));
                    or();
                    eq("mobile", queryParams.get("condition"));
                }

                if (StringUtils.isNotEmpty(queryParams.get("status"))){
                    try{
                        int status = Integer.parseInt(queryParams.get("status"));
                        eq("status", status);
                    }catch(Exception e){}
                }
            }
        });
        new TeacherWrapper(mapPage.getRecords()).warp();
        return super.packForBT(page);
    }

    /**
     * 获取教师管理列表
     */
    @RequestMapping(value = "/listAll")
    @ResponseBody
    public Object listAll(String condition) {
        return teacherService.selectList(null);
    }

    /**
     * 新增教师管理
     */
    @RequestMapping(value = "/add")
    @ResponseBody
    public Object add(Teacher teacher, String masterName, String masterCode) {

        Attachment icon = null;
        List<Attachment> attachmentList = attachmentService.listAttachment(masterName, masterCode);
        if (null != attachmentList && attachmentList.size() > 0) {
            icon = attachmentList.get(0);
            teacher.setAvatar(PathUtil.generate(iconVisitURL, String.valueOf(icon.getId())));
        }

        teacherService.create(teacher);

        // 更新ICON资源
        if (null != icon && null != icon.getId())
            try {
                icon.setMasterName(Teacher.class.getSimpleName());
                icon.setMasterCode(String.valueOf(teacher.getId()));

                attachmentService.updateById(icon);
            } catch (Exception e) {
                log.warn("更新图标失败");
            }

        // 添加老师用户信息
        // 如果老师信息没有电话号码则不添加
        if (StringUtils.isNotEmpty(teacher.getMobile())){
            Member teacherMember = new Member();
            teacherMember.setName(teacher.getName());
            teacherMember.setUserName(teacher.getCode());
            teacherMember.setGender(teacher.getGender());
            teacherMember.setStar(99);
            teacherMember.setNickname(teacher.getName());
            teacherMember.setMobileNumber(teacher.getMobile());

            try {
                memberService.createMember(teacherMember);
            }catch(Exception e){
                log.warn("老师用户创建失败", e.getMessage());
            }
        }

        return SUCCESS_TIP;
    }

    /**
     * 删除教师管理
     */
    @RequestMapping(value = "/pause")
    @ResponseBody
    public Object pause(@RequestParam String code) {
        teacherService.doPause(code);
        return SUCCESS_TIP;
    }

    /**
     * 删除教师管理
     */
    @RequestMapping(value = "/resume")
    @ResponseBody
    public Object resume(@RequestParam String code) {
        teacherService.doResume(code);
        return SUCCESS_TIP;
    }

    /**
     * 修改教师管理
     */
    @RequestMapping(value = "/update")
    @ResponseBody
    public Object update(Teacher teacher, String masterName, String masterCode) {

        Attachment icon = null;
        List<Attachment> attachmentList = attachmentService.listAttachment(masterName, masterCode);
        if (null != attachmentList && attachmentList.size() > 0) {
            icon = attachmentList.get(0);
            teacher.setAvatar(PathUtil.generate(iconVisitURL, String.valueOf(icon.getId())));
        }

        Teacher existTeacher = teacherService.selectById(teacher.getId());

        if (null == existTeacher)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"教师"});
        /* 同步老师手机号到会员表 */
        if(!existTeacher.getMobile().equals(teacher.getMobile())){
            Teacher oldTeacher = existTeacher;
            Member member = memberService.selectOne(new EntityWrapper<Member>() {{
                eq("mobile_number", oldTeacher.getMobile());
            }});
            if( member != null){
                member.setMobileNumber(teacher.getMobile());
                memberService.updateById(member);
            }
        }


        teacherService.updateById(teacher);

        existTeacher = teacherService.selectById(teacher.getId());

        // 更新ICON资源
        if (null != icon && null != icon.getId())
            try {
                icon.setMasterName(Teacher.class.getSimpleName());
                icon.setMasterCode(existTeacher.getCode());

                attachmentService.updateAndRemoveOther(icon);
            } catch (Exception e) {
                log.warn("更新图标失败");
            }

        // 添加老师用户信息
        // 如果老师信息没有电话号码则不添加
        if (StringUtils.isNotEmpty(existTeacher.getMobile())){
            Member teacherMember = new Member();
            teacherMember.setName(existTeacher.getName());
            teacherMember.setUserName(existTeacher.getCode());
            teacherMember.setGender(existTeacher.getGender());
            teacherMember.setStar(99);
            teacherMember.setNickname(existTeacher.getName());
            teacherMember.setMobileNumber(existTeacher.getMobile());

            try {
                memberService.createMember(teacherMember);
            }catch(Exception e){
                log.warn("老师用户创建失败", e.getMessage());
            }
        }
        return SUCCESS_TIP;
    }

    /**
     * 教师管理详情
     */
    @RequestMapping(value = "/detail/{teacherId}")
    @ResponseBody
    public Object detail(@PathVariable("teacherId") Integer teacherId) {
        return teacherService.selectById(teacherId);
    }
}
