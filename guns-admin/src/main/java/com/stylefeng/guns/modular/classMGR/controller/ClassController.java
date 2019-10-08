package com.stylefeng.guns.modular.classMGR.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.google.common.reflect.ClassPath;
import com.stylefeng.guns.common.constant.factory.PageFactory;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.base.controller.BaseController;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.log.LogObjectHolder;
import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.classMGR.service.ICourseOutlineService;
import com.stylefeng.guns.modular.classMGR.service.ICourseService;
import com.stylefeng.guns.modular.classMGR.transfer.ClassPlan;
import com.stylefeng.guns.modular.classMGR.warpper.ClassWrapper;
import com.stylefeng.guns.modular.classRoomMGR.service.IClassroomService;
import com.stylefeng.guns.modular.courseMGR.warpper.CourseWrapper;
import com.stylefeng.guns.modular.education.service.IScheduleClassService;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.modular.system.model.ClassSignableEnum;
import com.stylefeng.guns.modular.system.model.Classroom;
import com.stylefeng.guns.modular.system.model.CourseOutline;
import com.stylefeng.guns.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * 课程管理控制器
 *
 * @author fengshuonan
 * @Date 2018-10-20 11:35:19
 */
@Controller
@RequestMapping("/class")
public class ClassController extends BaseController {

    private String PREFIX = "/classMGR/class/";

    @Autowired
    private IClassService classService;
    @Autowired
    private IClassroomService classroomService;
    @Autowired
    private ICourseOutlineService courseOutlineService;
    @Autowired
    private ICourseService courseService;
    @Autowired
    private IScheduleClassService scheduleClassService;

    /**
     * 跳转到课程管理首页
     */
    @RequestMapping("")
    public String index() {
        return PREFIX + "class.html";
    }

    /**
     * 跳转到添加课程管理
     */
    @RequestMapping("/class_add")
    public String classAdd(Model model) {
        Date now = new Date();
        List<String> academicYearList = new ArrayList<>();
        for(int i = 0 ; i < 3; i++){
            academicYearList.add(DateUtil.getYear(DateUtils.truncate(now, Calendar.YEAR)));
            now = DateUtil.add(now, Calendar.YEAR,1);
        }
        model.addAttribute("academicYears", JSON.toJSON(academicYearList));
        model.addAttribute("operate", "add");
        return PREFIX + "class_wizard.html";
    }

    /**
     * 跳转到修改课程管理
     */
    @RequestMapping("/class_update/{classCode}")
    public String classUpdate(@PathVariable("classCode") String code, Model model) {
        Map<String, Object> classInstanceMap = classService.getMap(code);
        new ClassWrapper(classInstanceMap).warp();

        Map<String, Object> courseInstanceMap = courseService.getMap((String) classInstanceMap.get("courseCode"));
        new CourseWrapper(courseInstanceMap).warp();


        model.addAttribute("classItem",classInstanceMap);
        model.addAttribute("courseItem",courseInstanceMap);

        Date now = new Date();
        List<String> academicYearList = new ArrayList<>();
        for(int i = 0 ; i < 3; i++){
            academicYearList.add(DateUtil.getYear(DateUtils.truncate(now, Calendar.YEAR)));
            now = DateUtil.add(now, Calendar.YEAR,1);
        }
        model.addAttribute("academicYears", JSON.toJSON(academicYearList));
        model.addAttribute("operate", "update");

        LogObjectHolder.me().set(classInstanceMap);
        return PREFIX + "class_edit.html";
    }

    /**
     * 跳转到修改课程管理
     */
    @RequestMapping("/presign_setting/{classCode}")
    public String openClassPresignSetting(@PathVariable("classCode") String code, Model model) {
        Map<String, Object> currClassInfo = classService.getMap(code);

        if (null == currClassInfo)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"班级"});

        new ClassWrapper(currClassInfo).warp();
        model.addAttribute("classItem",currClassInfo);

        Map<String, Object> currCourseInfo = courseService.getMap((String)currClassInfo.get("courseCode"));
        new CourseWrapper(currCourseInfo).warp();
        model.addAttribute("courseItem", currCourseInfo);

        String presignClassCode = (String) currClassInfo.get("presignSourceClassCode");
        if (StringUtils.isNotEmpty(presignClassCode)){
            Map<String, Object> sourceClassInfo = classService.getMap(presignClassCode);
            new ClassWrapper(sourceClassInfo).warp();
            model.addAttribute("sourceClass", sourceClassInfo);

            Map<String, Object> sourceCourseInfo = courseService.getMap((String)sourceClassInfo.get("courseCode"));
            new CourseWrapper(sourceCourseInfo).warp();
            model.addAttribute("sourceCourse", sourceCourseInfo);
        }

        LogObjectHolder.me().set(currClassInfo);
        return PREFIX + "class_presign_setting.html";
    }


    /**
     * 跳转到修改课程管理
     */
    @RequestMapping("/crosssign_setting/{classCode}")
    public String openClassCrosssignSetting(@PathVariable("classCode") String code, Model model) {
        Map<String, Object> classInstanceMap = classService.getMap(code);
        new ClassWrapper(classInstanceMap).warp();

        model.addAttribute("classItem",classInstanceMap);
        LogObjectHolder.me().set(classInstanceMap);
        return PREFIX + "class_crosssign_setting.html";
    }

    /**
     * 获取课程管理列表
     */
    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list(@RequestParam Map<String, Object> queryParams) {
        //分页查詢

        if (queryParams.containsKey("signState")){
            int signQueryState = 0;
            try {
                signQueryState = Integer.parseInt(queryParams.get("signState").toString());
            }catch(Exception e){}

            switch (signQueryState){
                case 1:
                    // 当天可以报名
                    queryParams.put("signable", ClassSignableEnum.YES.code);
                    queryParams.put("signDate", DateUtil.format(new Date(), "yyyy-MM-dd"));
                    break;
                case 2:
                    // 未来一个月内即将报名
                    queryParams.put("signable", ClassSignableEnum.YES.code);
                    queryParams.put("signFutureBeginDate", DateUtil.format(DateUtils.addDays(new Date(), 1), "yyyy-MM-dd"));
                    queryParams.put("signFutureEndDate", DateUtil.format(DateUtils.addDays(new Date(), 30), "yyyy-MM-dd"));
                    break;
                case 3:
                    // 已结束报名的班级
                    queryParams.put("signable", ClassSignableEnum.YES.code);
                    queryParams.put("signCompleteDate", DateUtil.format(new Date(), "yyyy-MM-dd"));
                    break;
                case 4:
                    // 被禁用报名的班级
                    queryParams.put("signable", ClassSignableEnum.NO.code);
                    break;
                default:
                    break;
            }
        }

        queryParams.put("status","1");
        Page<Map<String, Object>> pageMap = classService.selectMapsPage(queryParams);
        //包装数据
        new ClassWrapper(pageMap.getRecords()).warp();
        return super.packForBT(pageMap);
    }

    /**
     * 获取教室管理列表
     */
    @RequestMapping(value = "/listAll")
    @ResponseBody
    public Object listRoom(String condition) {

        Wrapper<Class> classQueryWrapper = new EntityWrapper<Class>();
        classQueryWrapper.eq("status", GenericState.Valid.code);

        if (null != condition && condition.length() > 0){
            classQueryWrapper.like("name", condition);
        }
        classQueryWrapper.orderBy("id", false);

        return classService.selectList(classQueryWrapper);
    }

    /**
     * 新增班级
     */
    @RequestMapping(value = "/add")
    @ResponseBody
    public Object add(Class classInstance, String planList) {

        List<ClassPlan> classPlanList = new ArrayList<ClassPlan>();
        try {
            classPlanList = JSON.parseArray(planList, ClassPlan.class);
        }catch(Exception e){
        }

        if (classPlanList.isEmpty())
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"开班计划"});

        classService.createClass(classInstance, classPlanList);
        return SUCCESS_TIP;
    }

    /**
     * 删除班级
     */
    @RequestMapping(value = "/delete")
    @ResponseBody
    public Object delete(@RequestParam String classCode) {
        if (null == classCode)
            return SUCCESS_TIP;

        classService.deleteClass(classCode);
        return SUCCESS_TIP;
    }

    /**
     * 停止报名
     */
    @RequestMapping(value = "/signable/stop")
    @ResponseBody
    public Object stop(@RequestParam String classCode) {
        if (null == classCode)
            return SUCCESS_TIP;

        classService.stopSign(classCode);
        return SUCCESS_TIP;
    }

    /**
     * 启用报名
     */
    @RequestMapping(value = "/signable/resume")
    @ResponseBody
    public Object resume(@RequestParam String classCode) {
        if (null == classCode)
            return SUCCESS_TIP;

        classService.resumeSign(classCode);
        return SUCCESS_TIP;
    }


    /**
     * 停止入学测试
     */
    @RequestMapping(value = "/examinable/stop")
    @ResponseBody
    public Object examinableStop(@RequestParam String classCode) {
        if (null == classCode)
            return SUCCESS_TIP;

        classService.stopExaminable(classCode);
        return SUCCESS_TIP;
    }

    /**
     * 启用报名
     */
    @RequestMapping(value = "/examinable/resume")
    @ResponseBody
    public Object examinableResume(@RequestParam String classCode) {
        if (null == classCode)
            return SUCCESS_TIP;

        classService.resumeExaminable(classCode);
        return SUCCESS_TIP;
    }

    /**
     * 修改班级
     */
    @RequestMapping(value = "/update")
    @ResponseBody
    public Object update(Class classInstance, String planList) {
        List<ClassPlan> classPlanList = new ArrayList<ClassPlan>();
        try {
            classPlanList = JSON.parseArray(planList, ClassPlan.class);
        }catch(Exception e){
        }

        if (classPlanList.isEmpty())
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"开班计划"});

        classService.updateClass(classInstance, classPlanList);
        return SUCCESS_TIP;
    }

    /**
     * 修改班级跨报设置
     */
    @RequestMapping("/crosssign_setting/save")
    @ResponseBody
    public Object saveClassCrosssignSetting(Class classSetting, Model model) {

        Class classInstance = classService.get(classSetting.getCode());

        if (null == classInstance)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"班级"});

        Date crossStartDate = null;
        Date crossEndDate = null;
        if (GenericState.Valid.code == classSetting.getCrossable()){
            crossStartDate = classSetting.getCrossStartDate();
            crossEndDate = classSetting.getCrossEndDate();

            if (null == crossStartDate || null == crossEndDate)
                throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"时间设置"});
        }

        classInstance.setCrossable(classSetting.getCrossable());
        classInstance.setCrossStartDate(crossStartDate);
        classInstance.setCrossEndDate(crossEndDate);

        classService.updateById(classInstance);

        LogObjectHolder.me().set(classInstance);
        return SUCCESS_TIP;
    }


    /**
     * 修改班级续报设置
     */
    @RequestMapping("/presign_setting/save")
    @ResponseBody
    public Object saveClassPresignSetting( Class classSetting, Model model) {

        Class classInstance = classService.get(classSetting.getCode());

        if (null == classInstance)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"班级"});

        Date presignStartDate = null;
        Date presignEndDate = null;
        if (StringUtils.isNotEmpty(classSetting.getPresignSourceClassCode())){
            presignStartDate = classSetting.getPresignStartDate();
            presignEndDate = classSetting.getPresignEndDate();

            if (null == presignStartDate || null == presignEndDate)
                throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"时间设置"});

            if (presignStartDate.after(presignEndDate))
                throw new ServiceException(MessageConstant.MessageCode.SYS_DATA_OVERTOP, new String[]{"开始时间不能小于结束时间"});
        }

        classInstance.setPresignSourceClassCode(classSetting.getPresignSourceClassCode());
        classInstance.setPresignStartDate(presignStartDate);
        classInstance.setPresignEndDate(presignEndDate);

        classService.updateById(classInstance);

        LogObjectHolder.me().set(classInstance);
        return SUCCESS_TIP;
    }

    @RequestMapping("/plan/list")
    @ResponseBody
    public Object planList(String classCode){

        Map<String, Object> classPlanInfo = new HashMap<String, Object>();

        Date now = new Date();

        Date beginDate = DateUtils.truncate(now, Calendar.MONTH);
        Date endDate = DateUtils.addMonths(beginDate, 12);

        Map<String, Object> queryMap = new HashMap<String, Object>();
        queryMap.put("beginDate", beginDate);
        queryMap.put("endDate", endDate);
        queryMap.put("status", GenericState.Valid.code);

        List<ClassPlan> planList = scheduleClassService.selectPlanList(queryMap);

        List<ClassPlan> classPlanList = new ArrayList<ClassPlan>();

        if (null != classCode) {
            // 加载可以编辑的排班计划
            Iterator<ClassPlan> planListIter = planList.iterator();
            while (planListIter.hasNext()) {
                ClassPlan plan = planListIter.next();
                if (classCode.equals(plan.getClassCode())) {
                    classPlanList.add(plan);
                    planListIter.remove();
                }
            }
        }

        //classPlanInfo.put("allClassPlanList", planList);
        classPlanInfo.put("classPlanList", classPlanList);
        return classPlanInfo;
    }

    /**
     * 课程管理详情
     */
    @RequestMapping(value = "/detail/{classId}")
    @ResponseBody
    public Object detail(@PathVariable("classId") Integer classId) {
        return classService.selectById(classId);
    }
}
