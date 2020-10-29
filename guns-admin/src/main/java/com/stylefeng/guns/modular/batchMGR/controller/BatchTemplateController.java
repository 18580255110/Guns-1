package com.stylefeng.guns.modular.batchMGR.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.base.tips.ErrorTip;
import com.stylefeng.guns.core.base.tips.SuccessTip;
import com.stylefeng.guns.core.base.tips.Tip;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.modular.classMGR.service.ICourseService;
import com.stylefeng.guns.modular.classRoomMGR.service.IClassroomService;
import com.stylefeng.guns.modular.system.model.Classroom;
import com.stylefeng.guns.modular.system.model.Course;
import com.stylefeng.guns.modular.system.model.Dict;
import com.stylefeng.guns.modular.system.model.Teacher;
import com.stylefeng.guns.modular.system.service.IAttachmentService;
import com.stylefeng.guns.modular.system.service.IDictService;
import com.stylefeng.guns.modular.system.transfer.AttachmentInfo;
import com.stylefeng.guns.modular.teacherMGR.service.TeacherService;
import com.stylefeng.guns.util.DateUtil;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

import static com.stylefeng.guns.util.ExcelUtil.addCells;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/3/26 00:58
 * @Version 1.0
 */
@Controller
@RequestMapping("/batch/template")
public class BatchTemplateController {
    private static final String TEMP_MASTER_NAME = "_TEMPLATE_DOWNLOAD_";


    private static final List<String> TEMPLATE_SIGN_HEADER_DEFINE = new ArrayList<String>(){
        private static final long serialVersionUID = -5456067913018841267L;
        {
            add("班级编码");
            add("家长手机");
            add("家长名称");
            add("学员编码");
            add("学员名称");
            add("学员性别");
            add("学员年龄");
            add("所读年级");
            add("所读学校");
            add("目标学校");
            add("支付类型");
        }
    };


    private static final List<String> TEMPLATE_CLASS_HEADER_DEFINE = new ArrayList<String>(){
        private static final long serialVersionUID = -5456067913018841267L;
        {
            add("课程");
            add("班级名称");
            add("学年");
            add("学期");
            add("班型");
            add("讲师");
            add("辅导员");
            add("限额人数");
            add("教室");
            add("续报来源班级");
            add("开始续保日期");
            add("结束续保日期");
            add("允许跨报");
            add("跨报开始日期");
            add("跨报结束日期");
            add("开始报名日期");
            add("截止报名日期");
            add("是否需要测试");
            add("课程计划描述");
            add("报名费(元)");
            add("排班计划");
        }
    };




    @Autowired
    private IAttachmentService attachmentService;

    @Autowired
    private ICourseService courseService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private IClassroomService classroomService;

    @Autowired
    private IDictService dictService;

    @RequestMapping("/sign")
    @ResponseBody
    public Tip downloadSignTemplate(){
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("报名信息");

        CellStyle css = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        css.setDataFormat(format.getFormat("@"));
        for(int i = 0; i < 16; i++) {
            sheet.setDefaultColumnStyle(i, css);
        }
        Row header = sheet.createRow(0);
        header.setHeight((short) (35 * 20));
        Font headerFont = workbook.createFont();
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setFontName("黑体");
        headerFont.setColor(HSSFColor.BLACK.index);
        XSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillBackgroundColor(HSSFColor.GREEN.index);
        headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        headerStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        // 添加头部
        addCells(header, TEMPLATE_SIGN_HEADER_DEFINE, headerStyle);

        // 性别
        String[] gendarList = generateGendarDictionary();
        // 年级
        String[] gradeList = generateGradeDictionary();
        // 支付类型
        String[] payTypeList = generatePaytypeDictionary();
        // YesOrNo
        String[] yesOrNo = {"(1)是", "(0)否"};

        sheet = setHSSFValidation(sheet, gendarList, 1, 100, 5, 5);//  .
        sheet = setHSSFValidation(sheet, gradeList, 1, 100, 7, 7);// 前101行都设置为选择列表形式.
        sheet = setHSSFValidation(sheet, payTypeList, 1, 100, 10, 10);// 前101行都设置为选择列表形式.

        for (int i = 0; i < TEMPLATE_SIGN_HEADER_DEFINE.size(); i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i,sheet.getColumnWidth(i)*13/10);
        }

        AttachmentInfo attachmentInfo = new AttachmentInfo();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {

            workbook.write(bos);

            byte[] content = bos.toByteArray();
            attachmentInfo.addContent(content);
            attachmentInfo.parseType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            attachmentInfo.addOrgNames("batch-sign.template.xlsx");
            attachmentInfo.addSize(Long.valueOf(content.length));
            attachmentInfo.addDescription("batch-sign.template.xlsx");
        } catch (IOException e) {
            e.printStackTrace();
        }


        String code = UUID.randomUUID().toString();
        attachmentService.saveAttachment(attachmentInfo, TEMP_MASTER_NAME, code);

        Map<String, String> result = new HashMap<String, String>();
        result.put("name", TEMP_MASTER_NAME);
        result.put("code", code);

        SuccessTip tip = new SuccessTip();
        tip.setData(result);
        return tip;
    }

    @RequestMapping("/class")
    @ResponseBody
    public Tip downloadClassTemplate(){
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("开班信息");

        CellStyle css = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        css.setDataFormat(format.getFormat("@"));
        for(int i = 0; i < 16; i++) {
            sheet.setDefaultColumnStyle(i, css);
        }
        Row header = sheet.createRow(0);
        header.setHeight((short) (35 * 20));
        Font headerFont = workbook.createFont();
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setFontName("黑体");
        headerFont.setColor(HSSFColor.BLACK.index);
        XSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillBackgroundColor(HSSFColor.GREEN.index);
        headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        headerStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        // 添加头部
        addCells(header, TEMPLATE_CLASS_HEADER_DEFINE, headerStyle);

        // 课程
        String[] courseDictList = generateCourseDictionary();
        // 教师辅导员
        String[] teacherDictList = generateTeacherDictionary();
        // 教室
        String[] roomList = generateClassroomDictionary();
        // 学年
        String[] academicYearList = generateAcademicYearDictionary();
        // 学期
        String[] cycleList = generateCycleDictionary();
        // 班次
        String[] abilityList = generateAbilityDictionary();
        // YesOrNo
        String[] yesOrNo = {"(1)是", "(0)否"};

        sheet = setHSSFValidation(workbook, "course_hidden", 1, sheet, courseDictList, 1, 100, 0, 0);//  .
        sheet = setHSSFValidation(sheet, academicYearList, 1, 100, 2, 2);// 前101行都设置为选择列表形式.
        sheet = setHSSFValidation(sheet, cycleList, 1, 100, 3, 3);// 前101行都设置为选择列表形式.
        sheet = setHSSFValidation(sheet, abilityList, 1, 100, 4, 4);// 前101行都设置为选择列表形式.
        sheet = setHSSFValidation(sheet, teacherDictList, 1, 100, 5, 5);// 前101行都设置为选择列表形式.
        sheet = setHSSFValidation(sheet, teacherDictList, 1, 100, 6, 6);// 前101行都设置为选择列表形式.
        sheet = setHSSFValidation(workbook, "room_hidden", 2, sheet, roomList, 1, 100, 8, 8);// 前101行都设置为选择列表形式.
        sheet = setHSSFValidation(sheet, yesOrNo, 1, 100, 12, 12);// 前101行都设置为选择列表形式.
        sheet = setHSSFValidation(sheet, yesOrNo, 1, 100, 17, 17);// 前101行都设置为选择列表形式.

        for (int i = 0; i < TEMPLATE_CLASS_HEADER_DEFINE.size(); i++) {
            sheet.autoSizeColumn(i);
            switch(i){
                case 0:
                    sheet.setColumnWidth(i,calcWidth(courseDictList));
                    break;
                case 2:
                    sheet.setColumnWidth(i,calcWidth(academicYearList));
                    break;
                case 3:
                    sheet.setColumnWidth(i,calcWidth(cycleList));
                    break;
                case 4:
                    sheet.setColumnWidth(i,calcWidth(abilityList));
                    break;
                case 5:
                case 6:
                    sheet.setColumnWidth(i,calcWidth(teacherDictList));
                    break;
                case 8:
                    sheet.setColumnWidth(i,calcWidth(roomList));
                    break;
                default:
                    sheet.setColumnWidth(i,sheet.getColumnWidth(i)*13/10);
                    break;
            }

        }

        AttachmentInfo attachmentInfo = new AttachmentInfo();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {

            workbook.write(bos);

            byte[] content = bos.toByteArray();
            attachmentInfo.addContent(content);
            attachmentInfo.parseType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            attachmentInfo.addOrgNames("batch-class.template.xlsx");
            attachmentInfo.addSize(Long.valueOf(content.length));
            attachmentInfo.addDescription("batch-class.template.xlsx");
        } catch (IOException e) {
            e.printStackTrace();
        }


        String code = UUID.randomUUID().toString();
        attachmentService.saveAttachment(attachmentInfo, TEMP_MASTER_NAME, code);

        Map<String, String> result = new HashMap<String, String>();
        result.put("name", TEMP_MASTER_NAME);
        result.put("code", code);

        SuccessTip tip = new SuccessTip();
        tip.setData(result);
        return tip;
    }

    private int calcWidth(String[] courseDictList) {
        int max = 0;
        for(String courseInfo : courseDictList){
            int length = courseInfo.length() * 550;
            if (max < length)
                max = length;
        }

        System.out.println("<<<" + max);
        return max;
    }

    private String[] generateAbilityDictionary() {

        List<Dict> dictList = dictService.selectByParentCode("ability");

        if (null == dictList || dictList.isEmpty()){
            return new String[0];
        }

        List<String> abilityDictList = new ArrayList<>();

        for(Dict dict : dictList){
            abilityDictList.add("(" + dict.getCode() + ")" + dict.getName());
        }

        String[] dictionary = new String[abilityDictList.size()];
        return abilityDictList.toArray(dictionary);
    }

    private String[] generateCycleDictionary() {

        List<Dict> dictList = dictService.selectByParentCode("cycle");

        if (null == dictList || dictList.isEmpty()){
            return new String[0];
        }

        List<String> cycleDictList = new ArrayList<>();

        for(Dict dict : dictList){
            cycleDictList.add("(" + dict.getCode() + ")" + dict.getName());
        }

        String[] dictionary = new String[cycleDictList.size()];
        return cycleDictList.toArray(dictionary);
    }

    private String[] generateAcademicYearDictionary() {
        Date now = new Date();
        return new String[]{
                DateUtil.format(now, "yyyy"),
                DateUtil.format(DateUtil.add(now, Calendar.YEAR, 1), "yyyy"),
                DateUtil.format(DateUtil.add(now, Calendar.YEAR, 2), "yyyy"),
        };
    }

    private String[] generateClassroomDictionary() {
        List<Classroom> rootmList = classroomService.selectList(new EntityWrapper<Classroom>(){
            {
                eq("status", GenericState.Valid.code);
            }
        });

        if (null == rootmList || rootmList.isEmpty()){
            return new String[0];
        }

        List<String> classrootDictList = new ArrayList<>();

        for(Classroom room : rootmList){
            classrootDictList.add("(" + room.getCode() + ")" + room.getName());
        }

        String[] dictionary = new String[classrootDictList.size()];
        return classrootDictList.toArray(dictionary);
    }

    private String[] generateTeacherDictionary() {
        List<Teacher> teacherList = teacherService.selectList(new EntityWrapper<Teacher>(){
            {
                eq("status", GenericState.Valid.code);
            }
        });

        if (null == teacherList || teacherList.isEmpty()){
            return new String[0];
        }

        List<String> teacherDictList = new ArrayList<>();

        for(Teacher teacher : teacherList){
            teacherDictList.add("(" + teacher.getCode() + ")" + teacher.getName());
        }

        String[] dictionary = new String[teacherDictList.size()];
        return teacherDictList.toArray(dictionary);
    }

    private String[] generateCourseDictionary() {
        List<Course> courseList = courseService.selectList(new EntityWrapper<Course>(){
            {
                eq("status", GenericState.Valid.code);
            }
        });

        if (null == courseList || courseList.isEmpty()){
            return new String[0];
        }

        List<String> courseDictList = new ArrayList<>();

        for(Course course : courseList){
            courseDictList.add("(" + course.getCode() + ")" + course.getName());
        }
        String[] dictionary = new String[courseDictList.size()];
        return courseDictList.toArray(dictionary);
    }


    private String[] generatePaytypeDictionary() {

        List<Dict> dictList = dictService.selectByParentCode("pay_type");

        if (null == dictList || dictList.isEmpty()){
            return new String[0];
        }

        List<String> cycleDictList = new ArrayList<>();

        for(Dict dict : dictList){
            cycleDictList.add("(" + dict.getCode() + ")" + dict.getName());
        }

        String[] dictionary = new String[cycleDictList.size()];
        return cycleDictList.toArray(dictionary);
    }

    private String[] generateGradeDictionary() {

        List<Dict> dictList = dictService.selectByParentCode("school_grade");

        if (null == dictList || dictList.isEmpty()){
            return new String[0];
        }

        List<String> cycleDictList = new ArrayList<>();

        for(Dict dict : dictList){
            cycleDictList.add("(" + dict.getCode() + ")" + dict.getName());
        }

        String[] dictionary = new String[cycleDictList.size()];
        return cycleDictList.toArray(dictionary);
    }

    private String[] generateGendarDictionary() {

        List<Dict> dictList = dictService.selectByParentCode("sys_sex");

        if (null == dictList || dictList.isEmpty()){
            return new String[0];
        }

        List<String> cycleDictList = new ArrayList<>();

        for(Dict dict : dictList){
            cycleDictList.add("(" + dict.getCode() + ")" + dict.getName());
        }

        String[] dictionary = new String[cycleDictList.size()];
        return cycleDictList.toArray(dictionary);
    }

    /**
     * 设置某些列的值只能输入预制的数据,显示下拉框.
     *
     * @param sheet
     *            要设置的sheet.
     * @param textlist
     *            下拉框显示的内容
     * @param firstRow
     *            开始行
     * @param endRow
     *            结束行
     * @param firstCol
     *            开始列
     * @param endCol
     *            结束列
     * @return 设置好的sheet.
     */
    public static XSSFSheet setHSSFValidation(XSSFSheet sheet,
                                              String[] textlist, int firstRow, int endRow, int firstCol,
                                              int endCol) {

        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
        XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(textlist);
        CellRangeAddressList addressList = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
        XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
        return sheet;
    }


    /**
     * 设置某些列的值只能输入预制的数据,显示下拉框.
     *
     * @param wb
     *            要设置的sheet.*
     * @param sheet
     *            要设置的sheet.
     * @param textlist
     *            下拉框显示的内容
     * @param firstRow
     *            开始行
     * @param endRow
     *            结束行
     * @param firstCol
     *            开始列
     * @param endCol
     *            结束列
     * @return 设置好的sheet.
     */
    public static XSSFSheet setHSSFValidation(Workbook wb, String hiddenName, int hiddeIndex, XSSFSheet sheet,
                                              String[] textlist, int firstRow, int endRow, int firstCol,
                                              int endCol) {
        Sheet hiddenSheet = wb.createSheet(hiddenName);
        for (int i = 0; i < textlist.length; i++) {
            hiddenSheet.createRow(i).createCell(0).setCellValue(textlist[i]);
        }

        Name category1Name = wb.createName();
        category1Name.setNameName(hiddenName+"_hidden");

        category1Name.setRefersToFormula(hiddenName + "!" + "$A$1:$A$" + textlist.length);

        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
//        XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper
//                .createExplicitListConstraint(textlist);
        XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint(hiddenName+"_hidden");
        CellRangeAddressList addressList = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
        XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);

        wb.setSheetHidden(hiddeIndex, true);
        return sheet;
    }
}
