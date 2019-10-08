package com.stylefeng.guns.modular.statisticMGR.student.controller;

import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.common.constant.factory.PageFactory;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.core.base.controller.BaseController;
import com.stylefeng.guns.core.base.tips.ErrorTip;
import com.stylefeng.guns.core.base.tips.Tip;
import com.stylefeng.guns.modular.statisticMGR.student.warpper.StudentSignWrapper;
import com.stylefeng.guns.modular.system.dao.StatisticMapper;
import com.stylefeng.guns.modular.system.model.Attachment;
import com.stylefeng.guns.modular.system.service.IAttachmentService;
import com.stylefeng.guns.util.DateUtil;
import com.stylefeng.guns.util.PathUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static com.stylefeng.guns.util.ExcelUtil.addCell;
import static com.stylefeng.guns.util.ExcelUtil.addCells;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/2/26 8:47
 * @Version 1.0
 */
@Controller
@RequestMapping("/statistic/student/sign")
public class StudentSignStatisticController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(StudentSignStatisticController.class);

    private String PREFIX = "/statisticMGR/student/";

    @Value("${application.attachment.visit-url}")
    private String viewPath = "/";

    private static final List<String> STUDENT_SIGN_HEADER_DEFINE = new ArrayList<String>(){
        private static final long serialVersionUID = -5456067913018841267L;
        {
            add("序号");
            add("学员编号");
            add("学员名称");
            add("家长电话");
            add("所报班级");
            add("教授老师");
            add("报名时间");
        }
    };
    @Autowired
    private IAttachmentService attachmentService;
    @Autowired
    private StatisticMapper statisticMapper;
    /**
     * 跳转到学员报名统计首页
     */
    @RequestMapping("")
    public String index() {
        return PREFIX + "sign.html";
    }

    /**
     * 学员报名记录列表
     */
    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list(@RequestParam Map<String, Object> queryParams) {
        //分页查詢
        Page<Map<String, Object>> page = new PageFactory<Map<String, Object>>().defaultPage();

        Map<String, Object> arguments = buildQueryArguments(queryParams);

        List<Map<String, Object>> studentSignList = statisticMapper.statisticStudentSign(page, arguments);

        page.setRecords(studentSignList);
        //包装数据
        new StudentSignWrapper(page.getRecords()).warp();
        return super.packForBT(page);
    }

    /**
     * 导出订单管理
     */
    @RequestMapping(value = "/export")
    @ResponseBody
    public Object export(@RequestParam Map<String, Object> queryParams) {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        Page<Map<String, Object>> page = new PageFactory<Map<String, Object>>().defaultPage();
        page.setSize(2000); // 默认只导出2000条，请加入筛选条件导出
        Map<String, Object> arguments = buildQueryArguments(queryParams);
        List<Map<String, Object>> orderList = statisticMapper.statisticStudentSign(page, arguments);

        if (null == orderList){
            orderList = new ArrayList<Map<String, Object>>();
        }

        resultMap.put("dataResult", orderList);
        resultMap.put("totalCount", orderList.size());

        XSSFWorkbook workbook = buildStudentSignExportWorkbook(STUDENT_SIGN_HEADER_DEFINE, resultMap);

        File storeFolder = attachmentService.getStoreFolder();
        String filename = attachmentService.getFilename();
        File destFile = new File(storeFolder, filename);

        Tip result = null;
        boolean exportSucceed = false;
        try {
            workbook.write(new FileOutputStream(destFile));
            exportSucceed = true;
        } catch (IOException e) {
            e.printStackTrace();
            result = new ErrorTip(500, "导出失败");
        }

        if (exportSucceed) {
            log.info("Order export file success");
            Attachment attachment = new Attachment();
            attachment.setMasterName("STATISTIC_STUDENT_SIGN_EXPORT");
            attachment.setMasterCode(filename);
            attachment.setStatus(GenericState.Valid.code);
            attachment.setFileName("Sign-" + DateUtil.getyyMMddHHmmss() + ".xlsx");
            attachment.setAttachmentName(filename);
            attachment.setType("excel");
            attachment.setPath(destFile.getAbsolutePath());
            attachment.setSize(0L);
            attachmentService.insert(attachment);

            result = SUCCESS_TIP;
            result.setMessage(PathUtil.generate(viewPath, String.valueOf(attachment.getId())));
        }
        return result;
    }

    private XSSFWorkbook buildStudentSignExportWorkbook(List<String> headers, Map<String, Object> result) {
        XSSFWorkbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("学员报名统计");

        // 合并列，显示统计信息： 总数量、总金额
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 11));
        Row top = sheet.createRow(0);
        top.setHeight((short) (40 * 20));
        Font topFont = workbook.createFont();
        topFont.setFontHeightInPoints((short) 20);
        topFont.setFontName("黑体");
        topFont.setColor(HSSFColor.BLACK.index);
        XSSFCellStyle topStyle = workbook.createCellStyle();
        topStyle.setFont(topFont);
        topStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        topStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        topStyle.setIndention((short) 2);
        // 添加统计信息
        String totalCount = String.valueOf(result.get("totalCount"));

        addCell(top, "", topStyle);
        addCell(top, "报名总数： " + totalCount , topStyle);

        Row header = sheet.createRow(1);
        header.setHeight((short) (35 * 20));

        Font headerFont = workbook.createFont();
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setFontName("黑体");
        headerFont.setColor(HSSFColor.BLACK.index);
        XSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        headerStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        // 添加头部
        addCells(header, headers, headerStyle);

        int valueStartRow = 2;
        int valueIndex = 1;

        Font valueFont = workbook.createFont();
        valueFont.setFontHeightInPoints((short) 9);
        valueFont.setFontName("宋体");
        valueFont.setColor(HSSFColor.BLACK.index);
        XSSFCellStyle valueStyle = workbook.createCellStyle();
        valueStyle.setFont(valueFont);
        valueStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        valueStyle.setIndention((short) 1);
        valueStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

        List<Map<String, Object>> resultList = (List<Map<String, Object>>) result.get("dataResult");

        for(Map<String, Object> item : resultList){
            Row valueRow = sheet.createRow(valueStartRow++);

            String signDate = "";
            try{
                signDate = DateUtil.format((Date)item.get("signDate"), "yyyy-MM-dd HH:mm:ss");
            }catch(Exception e){}

            addCell(valueRow, valueIndex++, valueStyle);
            addCell(valueRow, item.get("studentCode"), valueStyle);
            addCell(valueRow, item.get("studentName"), valueStyle);
            addCell(valueRow, item.get("memberMobile"), valueStyle);
            addCell(valueRow, item.get("className"), valueStyle);
            addCell(valueRow, item.get("teacher"), valueStyle);
            addCell(valueRow, signDate, valueStyle);
        }

        //
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i,sheet.getColumnWidth(i)*11/10);
        }

        return workbook;
    }
}
