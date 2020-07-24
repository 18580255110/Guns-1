package com.stylefeng.guns.rest.modular.education.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.stylefeng.guns.common.constant.Const;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.admin.Administrator;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.modular.adjustMGR.service.IAdjustStudentService;
import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.classMGR.service.ICourseOutlineService;
import com.stylefeng.guns.modular.classMGR.service.ICourseService;
import com.stylefeng.guns.modular.classMGR.transfer.ClassPlan;
import com.stylefeng.guns.modular.classRoomMGR.service.IClassroomService;
import com.stylefeng.guns.modular.contentMGR.service.IContentService;
import com.stylefeng.guns.modular.education.service.IScheduleClassService;
import com.stylefeng.guns.modular.education.service.IScheduleStudentService;
import com.stylefeng.guns.modular.education.service.IStudentClassService;
import com.stylefeng.guns.modular.education.service.IStudentPrivilegeService;
import com.stylefeng.guns.modular.education.transfer.StudentClassInfo;
import com.stylefeng.guns.modular.education.transfer.StudentPlan;
import com.stylefeng.guns.modular.memberMGR.service.IMemberService;
import com.stylefeng.guns.modular.studentMGR.service.IStudentService;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.modular.system.service.IDictService;
import com.stylefeng.guns.modular.teacherMGR.service.TeacherService;
import com.stylefeng.guns.rest.core.ApiController;
import com.stylefeng.guns.rest.core.Responser;
import com.stylefeng.guns.rest.core.SimpleResponser;
import com.stylefeng.guns.rest.modular.education.requester.*;
import com.stylefeng.guns.rest.modular.education.responser.*;
import com.stylefeng.guns.rest.modular.student.responser.StudentResponse;
import com.stylefeng.guns.util.DateUtil;
import com.stylefeng.guns.util.ToolUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Created by 罗华.
 */
@RestController
@RequestMapping("/education")
@Api(value = "EducationController", tags = "教务接口")
public class EducationController extends ApiController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IClassroomService classroomService;

    @Autowired
    private ICourseService courseService;

    @Autowired
    private IClassService classService;

    @Autowired
    private IMemberService memberService;

    @Autowired
    private IStudentService studentService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private ICourseOutlineService courseOutlineService;

    @Autowired
    private IScheduleStudentService scheduleStudentService;

    @Autowired
    private IScheduleClassService scheduleClassService;

    @Autowired
    private IAdjustStudentService adjustStudentService;

    @Autowired
    private IStudentClassService studentClassService;

    @Autowired
    private IContentService contentService;

    @Autowired
    private IStudentPrivilegeService studentPrivilegeService;

    @Autowired
    private IDictService dictService;

    @Value("${application.education.adjust.maxTimes:4}")
    private int maxAdjustTimes = 4;

    @Value("${application.education.change.maxTimes:3}")
    private int maxChangeTimes = 3;

    @Value("${application.app.version:2.0.0}")
    private String appVersion;

    @RequestMapping(value = "/class/list", method = RequestMethod.POST)
    @ApiOperation(value="可报名班级列表", httpMethod = "POST", response = ClassListResponse.class)
    public Responser listClass(ClassQueryRequester requester){

        Member member = currMember();

        Map<String, Object> queryMap = requester.toMap();

        // 当前开放报名的班级
        // 客户要求没有到报名时间的班级仍然可以搜索到，但是不能实际报名，所以这里不能限制时间
        if (!(Const.APP_VERSION.equals(appVersion))) {
            // 老版本需要限制正常报名时间
            log.info("### old app version");
            queryMap.put("signDate", DateUtil.format(DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH), "yyyy-MM-dd"));
        }
        queryMap.put("forceSignEndDate", DateUtil.format(DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH), "yyyy-MM-dd"));
//        queryMap.put("forceSignEndTime", Integer.parseInt(DateUtil.getHHmm()));
        queryMap.put("signable", ClassSignableEnum.YES.code);

        List<com.stylefeng.guns.modular.system.model.Class> classList = classService.queryListForSign(queryMap);

        return assembleClassList(currToken(), classList);
        /*************************************************************************
         * 原来针对跨报的错误理解代码，暂时不用
         * -----------------------------------------------------------------------
        // 用户历史报班列表
        Map<String, Object> historyQueryMap = new HashMap<>();
        historyQueryMap.put("studyFinished", true);
        List<com.stylefeng.guns.modular.system.model.Class> hisClassList = studentClassService.selectMemberHistorySignedClass(member, historyQueryMap);

        // 只春、秋学期才能支持续保、跨报
        Iterator<Class> hisClassIterator = hisClassList.iterator();
        Set<Integer> cycles = new HashSet<>();
        Set<Integer> grades = new HashSet<>();
        Set<Integer> subjects = new HashSet<>();
        while(hisClassIterator.hasNext()){
            Class hisClassInfo = hisClassIterator.next();
            Course hisCourseInfo = courseService.get(hisClassInfo.getCourseCode());
            int cycle = hisClassInfo.getCycle();
            if (1 != cycle && 2 != cycle){
                // 春、秋两季班才可以
                hisClassIterator.remove();
            }else{
                cycles.add(cycle);
                grades.add(hisClassInfo.getGrade());
                subjects.add(Integer.parseInt(hisCourseInfo.getSubject()));
            }
        }

        if (hisClassList.isEmpty()){
            // 没有订购过课程的用户，直接返回
            return assembleClassList(classList);
        }

        // 老用户可以享受优先报名资格
        queryMap.remove("signDate");
        queryMap.put("signFutureBeginDate", DateUtil.format(DateUtils.addDays(new Date(), 1), "yyyy-MM-dd"));
        queryMap.put("signFutureEndDate", DateUtil.format(DateUtils.addDays(new Date(), 365), "yyyy-MM-dd"));

        if (!queryMap.containsKey("cycles") || ToolUtil.isEmpty(queryMap.get("cycles"))){
            StringBuilder cycleBuilder = new StringBuilder();
            for(int cycle : cycles){
                cycleBuilder.append(cycle).append(",");
            }
            if (cycleBuilder.length() > 0)
                queryMap.put("cycles", cycleBuilder.substring(0, cycleBuilder.length() - 1));
        }
        if (!queryMap.containsKey("subjects") || ToolUtil.isEmpty(queryMap.get("subjects"))){
            StringBuilder subjectBuilder = new StringBuilder();
            for(int subject : subjects){
                subjectBuilder.append(subject).append(",");
            }
            if (subjectBuilder.length() > 0)
                queryMap.put("subjects", subjectBuilder.substring(0, subjectBuilder.length() - 1));
        }
        if (!queryMap.containsKey("grades") || ToolUtil.isEmpty(queryMap.get("grades"))){
            StringBuilder gradeBuilder = new StringBuilder();
            for(int grade : grades){
                gradeBuilder.append(grade).append(",");
            }
            if (gradeBuilder.length() > 0)
                queryMap.put("grades", gradeBuilder.substring(0, gradeBuilder.length() - 1));
        }

        classList.addAll( classService.queryListForSign(queryMap) );

        return assembleClassList(classList);
         **/
    }

    @RequestMapping(value = "/class/list4teacher", method = RequestMethod.POST)
    @ApiOperation(value="老师班级列表", httpMethod = "POST", response = ClassListResponse.class)
    public Responser listTeacherClass(
            @ApiParam(required = true, value = "老师班级列表查询")
            @RequestBody
            @Valid
            ClassQueryRequester requester){

        Member member = currMember();

        Map<String, Object> queryMap = requester.toMap();
        queryMap.put("teacherCode", member.getUserName()); // 设置为当前老师
        queryMap.put("forceSignEndDate", DateUtil.format(DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH), "yyyy-MM-dd"));

        List<com.stylefeng.guns.modular.system.model.Class> classList = classService.queryListForTeacher(member.getUserName(), queryMap);

        return ClassListResponse.me(classList);
    }


    @RequestMapping(value = "/class/signlist", method = RequestMethod.POST)
    @ApiOperation(value="班级报班学员列表", httpMethod = "POST", response = ClassSignListResponse.class)
    public Responser listStudentSign(
            @ApiParam(required = true, value = "班级报班学员列表查询")
            @RequestBody
            @Valid
            ClassSignQueryRequester requester){

        Member member = currMember();

        Map<String, Object> queryMap = requester.toMap();
        Date now = new Date();

        List<StudentClassInfo> studentList = studentClassService.listSignedStudent(queryMap);

        Set<StudentResponse> studentSet = new HashSet<>();
        for(StudentClassInfo student : studentList){
            StudentResponse studentResponse = new StudentResponse();
            BeanUtils.copyProperties(student, studentResponse);

            Member studentMember = memberService.get(student.getUserName());
            if (null == studentMember){
                studentSet.add(studentResponse);
                continue;
            }

            studentResponse.setMemberName(studentMember.getName());
            studentResponse.setMemberMobile(studentMember.getMobileNumber());

            studentSet.add(studentResponse);
        }

        return ClassSignListResponse.me(studentSet);
    }

    @ApiOperation(value="班级详情", httpMethod = "POST", response = ClassDetailResponse.class)
    @ApiImplicitParam(name = "code", value = "班级编码", required = true, dataType = "String")
    @RequestMapping("/class/detail/{code}")
    public Responser detailForClass(@PathVariable("code") String code) {
        Wrapper<com.stylefeng.guns.modular.system.model.Class> queryWrapper = new EntityWrapper<com.stylefeng.guns.modular.system.model.Class>();
        queryWrapper.eq("code", code);

        com.stylefeng.guns.modular.system.model.Class classInfo = classService.selectOne(queryWrapper);

        if (null == classInfo)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND);

        int signedCount = classService.queryOrderedCount(classInfo.getCode());
        int maxCount = classInfo.getQuato();

        classInfo.setSignQuato(signedCount > maxCount ? maxCount : signedCount);

        int maxSchedule = classInfo.getPeriod();
        Map<String, Object> planQueryMap = new HashMap<String, Object>();
//        planQueryMap.put("beginDate", DateUtil.add(new Date(), Calendar.DAY_OF_MONTH, 1));
//        planQueryMap.put("beginDate", DateUtils.truncate(DateUtil.add(new Date(), Calendar.DAY_OF_MONTH, 1), Calendar.DAY_OF_MONTH));
        // 按当天算，并且beginTime  =  当前时间
        planQueryMap.put("beginDate", DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH));
//        planQueryMap.put("beginTime", Integer.parseInt(DateUtil.getHHmm()));
        planQueryMap.put("status", GenericState.Valid.code);
        planQueryMap.put("classCode", classInfo.getCode());
        List<ClassPlan> remainClassPlanList = scheduleClassService.selectPlanList(planQueryMap);
        Iterator<ClassPlan> classPlanIterator = remainClassPlanList.iterator();
        while(classPlanIterator.hasNext()){
            ClassPlan classPlan = classPlanIterator.next();
            Date studyDate = classPlan.getStudyDate();

            if (studyDate.getTime() == DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH).getTime()) {
                int time = Integer.parseInt(classPlan.getClassTime());
                int nowTime = Integer.parseInt(DateUtil.getHHmm());
                if (time < nowTime) {
                    classPlanIterator.remove();
                }
            }
        }

        BigDecimal perPrice = new BigDecimal(String.valueOf(classInfo.getPrice())).divide(new BigDecimal(maxSchedule), 10, RoundingMode.HALF_UP);
        BigDecimal remainPrice = new BigDecimal(remainClassPlanList.size()).multiply(perPrice);
        BigDecimal signPrice = remainPrice.setScale(0, RoundingMode.HALF_UP);
        classInfo.setSignPrice(signPrice.longValue());

        Map<String, Object> queryMap = new HashMap<String, Object>();
        queryMap.put("classCode", classInfo.getCode());
        queryMap.put("status", GenericState.Valid.code);

        List<ClassPlan> classPlanList = scheduleClassService.selectPlanList(queryMap);
        return ClassDetailResponse.me(classInfo, classPlanList);
    }

    @ApiOperation(value="课时列表", httpMethod = "POST", response = OutlineListResponser.class)
    @RequestMapping(value = "/outline/list", method = RequestMethod.POST)
    public Responser outlineList(
            @ApiParam(required = true, value = "课时查询")
            @RequestBody
            @Valid
            OutlineListQueryRequester requester){

        Member member = currMember();

        List<Student> studentList = new ArrayList<Student>();
        if (requester.notDirectStudent()){
            // 没有指定学员, 找到用户下所有学员列表
            studentList = studentService.listStudents(member.getUserName());
        }else{
            studentList.add(studentService.get(requester.getStudent()));
        }

        List<ScheduleStudent> planList = new ArrayList<ScheduleStudent>();
        Student student = null;
        for(Student currStudent : studentList) {
            planList.addAll(studentService.listCoursePlan(requester.getClassCode(), currStudent.getCode()));
            if (planList.isEmpty()){
                continue;
            }

            student = currStudent;
            break;
        }

        if (null == student){
            // 没有找到
            return OutlineListResponser.me(0, new ArrayList<OutlineResponse>());
        }

        // 查找已调课次数
        int adjustTimes = adjustStudentService.countAdjust(requester.getClassCode(), student.getCode(), AdjustStudentTypeEnum.Adjust);

        List<OutlineResponse> outlineResponseList = new ArrayList<OutlineResponse>();
        for(ScheduleStudent plan : planList){
            String outlineCode = plan.getOutlineCode();

            CourseOutline outline = courseOutlineService.get(outlineCode);

            if (null == outline)
                continue;

            OutlineResponse response = OutlineResponse.me(outline);

            if (!plan.isValid()){
                // 可能是已调课，找到当前有效的课程计划
                ScheduleStudent adjustedSchedule = scheduleStudentService.getAdjustedSchedule(plan.getCode());
                if (null == adjustedSchedule) {
                    response.setCanAdjust(false);
                } else{
                    CourseOutline newoutline = courseOutlineService.get(adjustedSchedule.getOutlineCode());
                    response = OutlineResponse.me(newoutline);
                    response.setCanAdjust(true);
                }
            } else {
                if (plan.isOver()){
                    // 已上课
                    response.setCanAdjust(false);
                }else {
                    response.setCanAdjust(true);
                }
            }

            outlineResponseList.add(response);
        }

        return OutlineListResponser.me(maxAdjustTimes - adjustTimes, outlineResponseList);
    }

    @ApiOperation(value="教室详情", httpMethod = "POST", response = ClassroomDetailResponse.class)
    @ApiImplicitParam(name = "code", value = "教室编码", required = true, dataType = "String", example = "JS000001")
    @RequestMapping("/classroom/detail/{code}")
    @ResponseBody
    public Responser detailForClassroom(@PathVariable("code") String code){
        Wrapper<Classroom> queryWrapper = new EntityWrapper<Classroom>();

        Classroom classroom = classroomService.selectOne(queryWrapper);

        if (null == classroom)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND);

        return ClassroomDetailResponse.me(classroom);
    }

    @RequestMapping(value = "/course/detail/{code}", method = {RequestMethod.POST, RequestMethod.GET})
    @ApiOperation(value="课程详情", httpMethod = "POST", response = CourseDetailResponse.class)
    @ApiImplicitParam(name = "code", value = "课程编码", required = true, dataType = "String", example = "KC000001")
    public Responser detailForCourse(@PathVariable("code") String code) {
        Wrapper<Course> queryWrapper = new EntityWrapper<Course>();
        queryWrapper.eq("code", code);

        Course course = courseService.selectOne(queryWrapper);

        if (null == course)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND);

        return CourseDetailResponse.me(course);
    }

    @RequestMapping(value = "/class/list4adjust", method = RequestMethod.POST)
    @ApiOperation(value="可调课班级列表", httpMethod = "POST", response = ClassListResponse.class)
    public Responser listClass4Adjust(
            @RequestBody
            @Valid
            AdjustQueryRequester requester){

        Member currMember = currMember();
        // 当前报班信息
        Wrapper<ScheduleStudent> studentPlanQueryWrapper = new EntityWrapper<>();
        studentPlanQueryWrapper.eq("student_code", requester.getStudent());
        //studentPlanQueryWrapper.eq("class_code", requester.getClassCode());
        studentPlanQueryWrapper.eq("outline_code", requester.getOutlineCode());
        studentPlanQueryWrapper.eq("status", GenericState.Valid.code);

        List<ScheduleStudent> studentPlanList = scheduleStudentService.selectList(studentPlanQueryWrapper);

        if (studentPlanList.isEmpty() || studentPlanList.size() > 1){
            // 只能有一个
            throw new ServiceException(MessageConstant.MessageCode.SYS_DATA_ILLEGAL);
        }

        Class currClassInfo = classService.get(studentPlanList.get(0).getClassCode());

        Wrapper<ScheduleClass> planQueryWrapper = new EntityWrapper<>();
        planQueryWrapper.eq("outline_code", requester.getOutlineCode());
        planQueryWrapper.gt("study_date", DateUtil.format(new Date(), "yyyy-MM-dd"));
        planQueryWrapper.eq("status", GenericState.Valid.code);

        List<ScheduleClass> classPlanList = scheduleClassService.selectList(planQueryWrapper);
        Set<Class> classResponserSet = new HashSet<>();

        Date now = new Date();
        for(ScheduleClass classPlan : classPlanList){
            Class classInfo = classService.get(classPlan.getClassCode());
            if (null == classInfo){
                continue;
            }
            if (!(classInfo.isValid())){
                continue;
            }
            if (currClassInfo.getCode().equals(classInfo.getCode())){
                // 过滤掉自己
                continue;
            }
            if (0 != classInfo.getCycle().compareTo(currClassInfo.getCycle())){
                // 不是同学期，过滤掉
                continue;
            }
            if (0 != classInfo.getAbility().compareTo(currClassInfo.getAbility())){
                // 不是同班型，过滤掉
                continue;
            }
            if (classInfo.getEndDate().compareTo(DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH)) <= 0){
                // 已结束的班级，过滤掉
                continue;
            }

            if (1 == classInfo.getCrossable() && now.before(classInfo.getCrossStartDate())){
                // 跨报班级，未开始跨报
                continue;
            }

            int cmpResult = DateUtil.compareDate(now, classInfo.getSignStartDate(), Calendar.DAY_OF_MONTH);
            if ((cmpResult >= 0 && ClassSignableEnum.YES.code != classInfo.getSignable()) || cmpResult < 0){
                // 班级还未开放报名
                continue;
            }

            classResponserSet.add(classInfo);
        }

        return ClassListResponse.me(classResponserSet);
    }

    @RequestMapping(value = "/class/list4change", method = RequestMethod.POST)
    @ApiOperation(value="可转班班级列表", httpMethod = "POST", response = ClassListResponse.class)
    public Responser listClass4Change(
            @RequestBody
            @Valid
            AdjustQueryRequester requester){

        Member member = currMember();

        Class currClass = classService.get(requester.getClassCode());
        Course course = courseService.get(currClass.getCourseCode());

        Map<String, Object> changeClassQuery = new HashMap<String, Object>();
        changeClassQuery.put("classCycles", String.valueOf(currClass.getCycle()));
        changeClassQuery.put("academicYear", currClass.getAcademicYear());
        changeClassQuery.put("grades", String.valueOf(currClass.getGrade()));
        changeClassQuery.put("abilities", String.valueOf(currClass.getAbility()));
        changeClassQuery.put("subjects", course.getSubject());
        changeClassQuery.put("signable", ClassSignableEnum.YES.code);
        changeClassQuery.put("forceSignEndDate", DateUtil.format(DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH), "yyyy-MM-dd"));

        Date now = new Date();
        boolean crossChange = false; // 是否在跨报期间转班
        if (1 == currClass.getCrossable() && null != currClass.getCrossStartDate() && null != currClass.getCrossEndDate()){
            if (now.compareTo(currClass.getCrossStartDate()) >= 0 && now.compareTo(currClass.getCrossEndDate()) < 0){
                crossChange = true;
            }
        }

        log.info("Cross change = {}", crossChange);
        List<com.stylefeng.guns.modular.system.model.Class> classList = classService.queryListForChange(changeClassQuery, crossChange);

        Set<Class> classSet = new HashSet<>();
        for (com.stylefeng.guns.modular.system.model.Class classInfo : classList){

            if (null == classInfo){
                continue;
            }

            if (!(classInfo.isValid())){
                continue;
            }

            if (classInfo.getCode().equals(currClass.getCode())){
                // 过滤掉自己
                continue;
            }

            if (1 == classInfo.getCrossable() && crossChange){

                if (now.before(classInfo.getCrossStartDate()))
                    // 跨报班级，未开始跨报
                    continue;

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(now);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                if (hour < 10)
                    continue; // 跨报班级，必须在10点后转班

            }

            if (crossChange){
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(now);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                if (hour < 10)
                    continue; // 跨报班级，必须在10点后转班
            }

            // 查询班级剩余报名额度
            int existCount = classService.queryOrderedCount(classInfo.getCode());

            if (existCount >= classInfo.getQuato() ){
                continue;
            }

            if (currClass.getPrice().equals(classInfo.getPrice())){
                classSet.add(classInfo);
            }
        }

        return ClassListResponse.me(classSet);
    }

    /**
     * @deprecated
     * @return
     */
//    public Responser listClass4Cross(){
//
//        Member member = currMember();
//
//        // 用户历史报班列表
//        Map<String, Object> historyQueryMap = new HashMap<>();
//        historyQueryMap.put("studyFinished", true);
//        List<com.stylefeng.guns.modular.system.model.Class> hisClassList = studentClassService.selectMemberHistorySignedClass(member, historyQueryMap);
//
//        // 只春、秋学期才能支持续保、跨报
//        Iterator<Class> hisClassIterator = hisClassList.iterator();
//        Set<Integer> cycles = new HashSet<>();
//        Set<Integer> grades = new HashSet<>();
//        Set<Integer> subjects = new HashSet<>();
//        while(hisClassIterator.hasNext()){
//            Class hisClassInfo = hisClassIterator.next();
//            Course hisCourseInfo = courseService.get(hisClassInfo.getCourseCode());
//            int cycle = hisClassInfo.getCycle();
//
//            switch(cycle){
//                case 1:
//                case 2:
//                    cycles.add(cycle);
//                    grades.add(hisClassInfo.getGrade());
//                    subjects.add(Integer.parseInt(hisCourseInfo.getSubject()));
//                    break;
//                default:
//                    hisClassIterator.remove();
//                    break;
//            }
//        }
//
//        Set<Class> classSet = new HashSet<>();
//        if (hisClassList.isEmpty()){
//            // 没有订购过课程的用户，直接返回
//            return ClassListResponse.me(classSet);
//        }
//
//        // 老用户可以享受优先报名资格
//        Map<String, Object> changeClassQuery = new HashMap<String, Object>();
//        changeClassQuery.put("signFutureBeginDate", DateUtil.format(DateUtils.addDays(new Date(), 1), "yyyy-MM-dd"));
//        changeClassQuery.put("signFutureEndDate", DateUtil.format(DateUtils.addDays(new Date(), 365), "yyyy-MM-dd"));
//        changeClassQuery.put("signable", ClassSignableEnum.YES.code);
//
//        StringBuilder cycleBuilder = new StringBuilder();
//        for(int cycle : cycles){
//            cycleBuilder.append(cycle).append(",");
//        }
//        if (cycleBuilder.length() > 0)
//            changeClassQuery.put("cycles", cycleBuilder.substring(0, cycleBuilder.length() - 1));
//        StringBuilder subjectBuilder = new StringBuilder();
//        for(int subject : subjects){
//            subjectBuilder.append(subject).append(",");
//        }
//        if (subjectBuilder.length() > 0)
//            changeClassQuery.put("subjects", subjectBuilder.substring(0, subjectBuilder.length() - 1));
//        StringBuilder gradeBuilder = new StringBuilder();
//        for(int grade : grades){
//            gradeBuilder.append(grade).append(",");
//        }
//        if (gradeBuilder.length() > 0)
//            changeClassQuery.put("grades", gradeBuilder.substring(0, gradeBuilder.length() - 1));
//
//        List<com.stylefeng.guns.modular.system.model.Class> classList = classService.queryListForCross(changeClassQuery);
//
//
//        for (com.stylefeng.guns.modular.system.model.Class classInfo : classList){
//            if (null == classInfo){
//                continue;
//            }
//            if (!(classInfo.isValid())){
//                continue;
//            }
//
//            classSet.add(classInfo);
//        }
//
//        // TODO 暂屏蔽，25日测试完成后放开
//        classSet = new HashSet<>();
//        return ClassListResponse.me(classSet);
//    }


    @RequestMapping(value = "/class/list4cross", method = RequestMethod.POST)
    @ApiOperation(value="可跨报班级列表", httpMethod = "POST", response = ClassCrossListResponse.class)
    public Responser listClass4CrossNew(
            @RequestBody
            @Valid
            CrossQueryRequester requester
    ){

        Member member = currMember();

        Set<Class> classSignSet = new HashSet<>();
        Set<Class> classChangeSet = new HashSet<>();
        Map<String, Collection<Class>> mapping = new HashMap<String, Collection<Class>>();
        Map<String, String> studentMapping = new HashMap<String, String>();

        if (null == requester.getStudent() || requester.getStudent().length() == 0){
            // app小于2.0版本，跨报没有学员选择
            log.warn("Old app version! no student selected!");
            List<Student> studentList = studentService.listStudents(member.getUserName());
            for(Student student : studentList){
                try {
                    classSignSet.addAll(listClass4CrossWithSign(student));
                }catch(Exception e){}
            }
        }else{
            Student student = studentService.get(requester.getStudent());

            if (null == student || GenericState.Valid.code != student.getStatus())
                throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"学员"});

            classSignSet.addAll(listClass4CrossWithSign(student));
        }
        return ClassCrossListResponse.me(classSignSet, classChangeSet, mapping, studentMapping);

/*
        Student student = studentService.get(requester.getStudent());

        if (null == student) {
            List<Student> studentList = studentService.listStudents(member.getUserName());
            if (null != studentList && studentList.size() > 0){
                student = studentList.get(0);
            }
        }

        if (null == student || GenericState.Valid.code != student.getStatus())
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"学员"});

        Date now = new Date();

        Date halfDate = DateUtil.parse(DateUtil.getYear(now) + "-06-01", "yyyy-MM-dd");

        Date beginDate = null;
        Date endDate = null;
        if (now.compareTo(halfDate) > 0) {
            //下半年
            endDate = DateUtil.parse(DateUtil.getYear(now) + "-10-01", "yyyy-MM-dd");
            beginDate = DateUtil.parse(DateUtil.getYear(now) + "-04-01", "yyyy-MM-dd");
        }else{
            //上半年
            beginDate = DateUtil.parse(DateUtil.getYear(DateUtil.add(now, Calendar.YEAR, -1)) + "-10-01", "yyyy-MM-dd");
            endDate = DateUtil.parse(DateUtil.getYear(now) + "-04-01", "yyyy-MM-dd");
        }
        // 查找本期跨报开始、结束日期
        Map<String, Object> crossQueryParams = new HashMap<>();
        crossQueryParams.put("beginDate", DateUtil.format(endDate, "yyyy-MM-dd"));
        crossQueryParams.put("crossable", GenericState.Valid.code);
        crossQueryParams.put("signable", ClassSignableEnum.YES.code);
        crossQueryParams.put("grades", String.valueOf(student.getGrade()));

        // 查找本期跨报的班级
        List<Class> classInfoList = classService.queryListForCross(crossQueryParams);
        Date crossStartDate = null;
        Date crossEndDate = null;
        for(Class classInfo : classInfoList){
            if (null == crossStartDate){
                crossStartDate = classInfo.getCrossStartDate();
            }
            if (null == crossEndDate) {
                crossEndDate = classInfo.getCrossEndDate();
            }

            if (null!= crossStartDate && crossStartDate.after(classInfo.getCrossStartDate())){
                crossStartDate = classInfo.getCrossStartDate();
            }

            if (null != crossEndDate && crossEndDate.before(classInfo.getCrossEndDate())){
                crossEndDate = classInfo.getCrossEndDate();
            }
        }

        log.info("Cross sign begin date = {}, end date = {}", crossStartDate, crossEndDate);
        if (now.before(crossStartDate)){
            // 跨报未开始
            Content content = contentService.get("CT00000000000002");
            String contentMsg = content.getContent().replaceAll("\\{beginDate\\}", DateUtil.format(crossStartDate, "yyyy年MM月dd日"));
            throw new ServiceException(MessageConstant.MessageCode.SYS_TEMPLATE_MESSAGE, new String[]{contentMsg});
        }

        if (now.after(crossEndDate)){
            //跨报已结束
            Content content = contentService.get("CT00000000000003");
            String contentMsg = content.getContent().replaceAll("\\{endDate\\}", DateUtil.format(crossStartDate, "yyyy年MM月dd日"));
            throw new ServiceException(MessageConstant.MessageCode.SYS_TEMPLATE_MESSAGE, new String[]{contentMsg});
        }


        Set<Class> classChangeSet = new HashSet<>();
        Map<String, Collection<Class>> mapping = new HashMap<String, Collection<Class>>();
        Map<String, String> studentMapping = new HashMap<String, String>();

        // 用户历史报班列表
        Map<String, Object> historyQueryMap = new HashMap<>();
        historyQueryMap.put("beginSignDate", DateUtil.format(beginDate, "yyyy-MM-dd"));
        historyQueryMap.put("endSignDate", DateUtil.format(crossStartDate, "yyyy-MM-dd"));
        historyQueryMap.put("student", student.getCode());
        historyQueryMap.put("payStates", String.valueOf(PayStateEnum.PayOk.code));

        List<com.stylefeng.guns.modular.system.model.Class> hisClassList = studentClassService.selectMemberHistorySignedClass(student, historyQueryMap);

        // 只春、暑、秋、寒 学期才能支持续保、跨报
        Iterator<Class> hisClassIterator = hisClassList.iterator();
        Set<Integer> subjects = new HashSet<>();
        Set<Integer> abilities = new HashSet<>();
        while(hisClassIterator.hasNext()){
            Class hisClassInfo = hisClassIterator.next();
            Course hisCourseInfo = courseService.get(hisClassInfo.getCourseCode());
            int cycle = hisClassInfo.getCycle();

            switch(cycle){
                case 1:
                case 2:
                case 3:
                case 4:
                    subjects.add(Integer.parseInt(hisCourseInfo.getSubject()));
                    abilities.add(hisClassInfo.getAbility());
                    break;
                default:
                    hisClassIterator.remove();
                    break;
            }
        }

        if (!(hisClassList.isEmpty())){
            // 老学员
            classSignSet.addAll(listClass4CrossWithSign(student, subjects, abilities));
        }else{
            // 没有报名上学期的学员
            //跨报进行
            Content content = contentService.get("CT00000000000009");
            throw new ServiceException(MessageConstant.MessageCode.SYS_TEMPLATE_MESSAGE, new String[]{content.getContent()});
        }

        return ClassCrossListResponse.me(classSignSet, classChangeSet, mapping, studentMapping);
        */
    }

    private Collection<? extends Class> listClass4CrossWithSign(Student student) {
        Date now = new Date();

        Date halfDate = DateUtil.parse(DateUtil.getYear(now) + "-06-01", "yyyy-MM-dd");

        Date beginDate = null;
        Date endDate = null;
        if (now.compareTo(halfDate) > 0) {
            //下半年
            endDate = DateUtil.parse(DateUtil.getYear(now) + "-10-01", "yyyy-MM-dd");
            beginDate = DateUtil.parse(DateUtil.getYear(now) + "-04-01", "yyyy-MM-dd");
        }else{
            //上半年
            beginDate = DateUtil.parse(DateUtil.getYear(DateUtil.add(now, Calendar.YEAR, -1)) + "-10-01", "yyyy-MM-dd");
            endDate = DateUtil.parse(DateUtil.getYear(now) + "-04-01", "yyyy-MM-dd");
        }
        // 查找本期跨报开始、结束日期
        Map<String, Object> crossQueryParams = new HashMap<>();
        crossQueryParams.put("beginDate", DateUtil.format(endDate, "yyyy-MM-dd"));
        crossQueryParams.put("crossable", GenericState.Valid.code);
        crossQueryParams.put("signable", ClassSignableEnum.YES.code);
        crossQueryParams.put("grades", String.valueOf(student.getGrade()));

        // 查找本期跨报的班级
        List<Class> classInfoList = classService.queryListForCross(crossQueryParams);
        Date crossStartDate = null;
        Date crossEndDate = null;

        Set<CrossWindow> crossWindowSet = new HashSet<>();
        for(Class classInfo : classInfoList){

            log.info("Add cross window class = {}", classInfo.getCode());
            crossWindowSet.add(new CrossWindow(classInfo.getCrossStartDate(), classInfo.getCrossEndDate()));
//
//            if (null == crossStartDate){
//                crossStartDate = classInfo.getCrossStartDate();
//            }
//
//            if (null == crossEndDate) {
//                crossEndDate = classInfo.getCrossEndDate();
//            }
//
//            if (null!= crossStartDate && crossStartDate.compareTo(classInfo.getCrossStartDate()) == 0){
//                continue;
//            }
//
//            if (null!= crossStartDate && crossStartDate.after(classInfo.getCrossStartDate())){
//                crossStartDate = classInfo.getCrossStartDate();
//            }
//
//            if (null != crossEndDate && crossEndDate.before(classInfo.getCrossEndDate())){
//                crossEndDate = classInfo.getCrossEndDate();
//            }
        }

        boolean notBegin = true;
        boolean isFinish = true;

        for(CrossWindow crossWindow : crossWindowSet){
            log.info("Cross sign compare begin date = {}, end date = {}", crossStartDate, crossEndDate);

            if (now.before(crossWindow.getBeginDate())) {
                if (!notBegin) {
                    notBegin = true;
                    crossStartDate = crossWindow.getBeginDate();
                }
                continue; // 属于没有开始的
            }else if (null != crossStartDate && now.before(crossStartDate)){
                if (crossStartDate.after(crossWindow.getBeginDate()))
                    crossStartDate = crossWindow.getBeginDate();

                continue;
            }
            crossStartDate = crossWindow.getBeginDate();
            notBegin = false;

            crossEndDate = crossWindow.getEndDate();
            if (now.after(crossWindow.getEndDate())){
                continue;
            }

            isFinish = false;
            break;
        }

        log.info("Cross sign begin date = {}, end date = {}", crossStartDate, crossEndDate);
//        if (now.before(crossStartDate)){
        if (notBegin){
            // 跨报未开始
            Content content = contentService.get("CT00000000000002");
            String contentMsg = content.getContent().replaceAll("\\{beginDate\\}", DateUtil.format(null == crossStartDate ? DateUtil.add(new Date(), Calendar.DAY_OF_MONTH, 1) : crossStartDate, "yyyy年MM月dd日"));
            throw new ServiceException(MessageConstant.MessageCode.SYS_TEMPLATE_MESSAGE, new String[]{contentMsg});
        }

//        if (now.after(crossEndDate)){
        if (isFinish){
            //跨报已结束
            Content content = contentService.get("CT00000000000003");
            String contentMsg = content.getContent().replaceAll("\\{endDate\\}", DateUtil.format(null == crossEndDate ? DateUtil.add(new Date(), Calendar.DAY_OF_MONTH, -1) : crossEndDate, "yyyy年MM月dd日"));
            throw new ServiceException(MessageConstant.MessageCode.SYS_TEMPLATE_MESSAGE, new String[]{contentMsg});
        }

        Set<Class> classSignSet = new HashSet<>();

        // 用户历史报班列表
        Map<String, Object> historyQueryMap = new HashMap<>();
        historyQueryMap.put("beginSignDate", DateUtil.format(beginDate, "yyyy-MM-dd"));
        historyQueryMap.put("endSignDate", DateUtil.format(crossStartDate, "yyyy-MM-dd"));
        historyQueryMap.put("student", student.getCode());
        historyQueryMap.put("payStates", String.valueOf(PayStateEnum.PayOk.code));

        List<com.stylefeng.guns.modular.system.model.Class> hisClassList = studentClassService.selectMemberHistorySignedClass(student, historyQueryMap);

        // 只春、暑、秋、寒 学期才能支持续保、跨报
        Iterator<Class> hisClassIterator = hisClassList.iterator();
        Set<Integer> subjects = new HashSet<>();
        Set<Integer> abilities = new HashSet<>();
        while(hisClassIterator.hasNext()){
            Class hisClassInfo = hisClassIterator.next();
            Course hisCourseInfo = courseService.get(hisClassInfo.getCourseCode());
            int cycle = hisClassInfo.getCycle();

            switch(cycle){
                case 1:
                case 2:
                case 3:
                case 4:
                    subjects.add(Integer.parseInt(hisCourseInfo.getSubject()));
                    abilities.add(hisClassInfo.getAbility());
                    break;
                default:
                    hisClassIterator.remove();
                    break;
            }
        }

        if (!(hisClassList.isEmpty())){
            // 老学员
            classSignSet.addAll(listClass4CrossWithSign(student, subjects, abilities));
        }else{
            // 没有报名上学期的学员
            //跨报进行
            Content content = contentService.get("CT00000000000009");
            throw new ServiceException(MessageConstant.MessageCode.SYS_TEMPLATE_MESSAGE, new String[]{content.getContent()});
        }

        return classSignSet;
    }

    private Collection<? extends Class> listClass4CrossWithSign(Student student, Set<Integer> subjects, Set<Integer> abilities) {

        Set<Class> classSet = new HashSet<>();

        // 老用户可以享受优先报名资格
        Map<String, Object> crossClassQuery = new HashMap<String, Object>();
        crossClassQuery.put("studentCode", student.getCode()); // 查询跨报必须要传
        crossClassQuery.put("noSigned", true);
        crossClassQuery.put("signable", ClassSignableEnum.YES.code);
        crossClassQuery.put("crossable", ClassSignableEnum.YES.code);
        crossClassQuery.put("grades", student.getGrade().toString());
        crossClassQuery.put("crossDate", DateUtil.format(new Date(), "yyyy-MM-dd"));

        StringBuilder subjectBuilder = new StringBuilder();
        for(int subject : subjects){
            subjectBuilder.append(subject).append(",");
        }
        if (subjectBuilder.length() > 0)
            crossClassQuery.put("subjects", subjectBuilder.substring(0, subjectBuilder.length() - 1));

//        StringBuilder abilityBuilder = new StringBuilder();
//        for(int ability : abilities){
//            abilityBuilder.append(ability).append(",");
//        }
        //if (abilityBuilder.length() > 0)
            //crossClassQuery.put("abilities", abilityBuilder.substring(0, subjectBuilder.length() - 1));

        List<com.stylefeng.guns.modular.system.model.Class> classList = classService.queryListForCross(crossClassQuery);

        for (com.stylefeng.guns.modular.system.model.Class classInfo : classList){
            if (null == classInfo){
                continue;
            }
            if (!(classInfo.isValid())){
                continue;
            }

            if (studentPrivilegeService.hasPrivilege(student, classInfo))
                classSet.add(classInfo);
            else if (studentPrivilegeService.hasAdvancePrivilege(student, classInfo))
                classSet.add(classInfo);
            else
                log.warn("Student = {} has not privilege, classInfo = {}", student.getCode(), classInfo.getCode());

        }

        return classSet;
    }

    private Collection<Class> listClass4CrossWithChange (String classCode){

        Class currClass = classService.get(classCode);
        Course course = courseService.get(currClass.getCourseCode());

        Map<String, Object> changeClassQuery = new HashMap<String, Object>();
        changeClassQuery.put("classCycles", String.valueOf(currClass.getCycle()));
        changeClassQuery.put("grades", String.valueOf(currClass.getGrade()));
        changeClassQuery.put("abilities", String.valueOf(currClass.getAbility()));
        changeClassQuery.put("subjects", course.getSubject());
        changeClassQuery.put("signable", ClassSignableEnum.YES.code);

        List<com.stylefeng.guns.modular.system.model.Class> classList = classService.queryListForCrossChange(changeClassQuery);

        Set<Class> classSet = new HashSet<>();
        for (com.stylefeng.guns.modular.system.model.Class classInfo : classList){
            // 查询班级剩余报名额度
            int signedCount = classService.queryOrderedCount(classInfo.getCode());
            if (classInfo.getQuato() <= signedCount)
                continue;

            if (null == classInfo){
                continue;
            }
            if (!(classInfo.isValid())){
                continue;
            }
            if (classInfo.getCode().equals(currClass.getCode())){
                // 过滤掉自己
                continue;
            }
            if (currClass.getPrice().equals(classInfo.getPrice())){
                classSet.add(classInfo);
            }
        }

        return classSet;
    }

    @RequestMapping(value = "/adjust/course", method = RequestMethod.POST)
    @ApiOperation(value = "调课申请", httpMethod = "POST", response = SimpleResponser.class)
    @ResponseBody
    public Responser adjustCourse(
            @ApiParam(required = true, value = "调课申请")
            @RequestBody
            @Valid
            AdjustApplyRequester requester) {
        Member member = currMember();

        Student student = studentService.get(requester.getStudentCode());

        Class sourceClass = classService.get(requester.getSourceClass());
        Class targetClass = classService.get(requester.getTargetClass());

        Map<String, Object> fromData = new HashMap<>();
        fromData.put("sourceClass", sourceClass);
        fromData.put("outlineCode", requester.getOutlineCode());

        Map<String, Object> destData = new HashMap<>();
            destData.put("targetClass", targetClass);

        adjustStudentService.adjustCourse(member, student, fromData, destData);

        SimpleResponser response = SimpleResponser.success();
        response.setMessage("您的调课申请已经接受");

        return response;
    }

    @RequestMapping(value = "/adjust/class", method = RequestMethod.POST)
    @ApiOperation(value = "转班申请", httpMethod = "POST", response = SimpleResponser.class)
    @ResponseBody
    public Responser changeClass(
            @ApiParam(required = true, value = "转班申请")
            @RequestBody
            @Valid
            ChangeApplyRequester requester) {

        Member member = currMember();

        Student student = studentService.get(requester.getStudentCode());

        Class sourceClass = classService.get(requester.getSourceClass());
        Class targetClass = classService.get(requester.getTargetClass());

        Map<String, Object> fromData = new HashMap<>();
        fromData.put("sourceClass", sourceClass);

        Map<String, Object> destData = new HashMap<>();
            destData.put("targetClass", targetClass);

        AdjustStudent adjustApply = adjustStudentService.adjustClass(member, student, fromData, destData);

            Administrator administrator = new Administrator();
        administrator.setAccount("1");
            administrator.setId(1);
        administrator.setName("科萃教育");
        adjustStudentService.setAdministrator(administrator);

        AdjustStudentApproveStateEnum action = AdjustStudentApproveStateEnum.Refuse;
        String remark = "";
        if (adjustStudentService.canChange(adjustApply)){
            action = AdjustStudentApproveStateEnum.Appove;
            remark = "审核通过";
        }
        adjustStudentService.doChangeApprove(adjustApply.getId(), action, remark);

        SimpleResponser response = SimpleResponser.success();
        response.setMessage("转班成功");
        return response;
    }

    @ApiOperation(value="教师课程表", httpMethod = "POST", response = ClassPlanListResponser.class)
    @RequestMapping("/course/plan/list4teacher")
    public Responser teacherPlanList(
            @ApiParam(required = true, value = "课程表查询")
            @RequestBody
            @Valid
            QueryPlanListRequester requester
    ){

        Member member = currMember();

            // 老师用户, 展示老师的课程表
            Date now = new Date();

            Map<String, Object> queryMap = new HashMap<String, Object>();

        queryMap.put("endDate", now);
        queryMap.put("teacherCode", member.getUserName());
        queryMap.put("status", GenericState.Valid.code);

        if (ToolUtil.isNotEmpty(requester.getMonth())){
            Date queryDate = DateUtil.parse(requester.getMonth(), "yyyyMM");
            queryMap.put("beginDate", DateUtil.format(queryDate, "yyyy-MM-dd"));
            queryMap.put("endDate", DateUtil.format(DateUtil.add(queryDate, Calendar.MONTH, 1), "yyyy-MM-dd"));
        }

            List<ClassPlan> planList = scheduleClassService.selectPlanList(queryMap);

            return ClassPlanListResponser.me(planList);
    }

    @ApiOperation(value="课程表", httpMethod = "POST", response = PlanListResponser.class)
    @RequestMapping("/course/plan/list")
    public Responser planList(
            @ApiParam(required = true, value = "课程表查询")
            @RequestBody
            @Valid
            QueryPlanListRequester requester){

        Member member = currMember();
        Integer target = requester.getTarget();
        boolean isTeacher = false;
        if (null == target){
            isTeacher = member.isTeacher();
        }else{
            isTeacher = 99 == target;
        }

        if (isTeacher){
            // 老师用户, 展示老师的课程表
            Date now = new Date();

            Map<String, Object> queryMap = new HashMap<String, Object>();

            queryMap.put("endDate", now);
            queryMap.put("teacherCode", member.getUserName());
            queryMap.put("status", GenericState.Valid.code);

            if (ToolUtil.isNotEmpty(requester.getMonth())){
                Date queryDate = DateUtil.parse(requester.getMonth(), "yyyyMM");
                queryMap.put("beginDate", DateUtil.format(queryDate, "yyyy-MM-dd"));
                queryMap.put("endDate", DateUtil.format(DateUtil.add(queryDate, Calendar.MONTH, 1), "yyyy-MM-dd"));
            }

            List<ClassPlan> planList = scheduleClassService.selectPlanList(queryMap);

            return ClassPlanListResponser.me(planList);
        }else{
            Map<String, Object> queryMap = new HashMap<String, Object>();
            String studentCode = requester.getStudent();
            String classCode = requester.getClassCode();

            List<String> studentCodeList = new ArrayList<>();
            if (ToolUtil.isNotEmpty(studentCode)) {
                studentCodeList.add(studentCode);
            }else{
                // 没有指定学员， 展示当前会员所有学员的课程表信息
                List<Student> studentList = studentService.listStudents(member.getUserName()) ;
                if (null != studentList){
                    for(Student student : studentList){
                        studentCodeList.add(student.getCode());
                    }
                }
            }
            queryMap.put("students", studentCodeList);

            if (ToolUtil.isNotEmpty(classCode))
                queryMap.put("classCode", classCode);

            if (ToolUtil.isNotEmpty(requester.getMonth())){
                Date queryDate = DateUtil.parse(requester.getMonth(), "yyyyMM");
                queryMap.put("beginDate", DateUtil.format(queryDate, "yyyy-MM-dd"));
                queryMap.put("endDate", DateUtil.format(DateUtil.add(queryDate, Calendar.MONTH, 1), "yyyy-MM-dd"));
            }

            queryMap.put("status", GenericState.Valid.code);

            List<StudentPlan> planList = scheduleStudentService.selectPlanList(queryMap);

            return PlanListResponser.me(planList);
        }
    }

    @ApiOperation(value="单天课程表", httpMethod = "POST", response = PlanOfDayResponserWrapper.class)
    @RequestMapping(value = "/course/plan/day", method = RequestMethod.POST)
    public Responser queryPlanOfDay(
            @ApiParam(required = true, value = "单天课程表查询")
            @RequestBody
            @Valid
            QueryPlanOfDayRequester requester, HttpServletRequest request
    ){
        Member member = currMember();

        List<PlanOfDayResponser> responserList = new ArrayList<PlanOfDayResponser>();

        if (member.isTeacher()){
            Map<String, Object> queryMap = new HashMap<String, Object>();
            Date queryDate = requester.getDay();
            queryMap.put("beginDate", DateUtil.format(queryDate, "yyyy-MM-dd"));
            queryMap.put("endDate", DateUtil.format(DateUtil.add(queryDate, Calendar.DAY_OF_MONTH, 1), "yyyy-MM-dd"));
            queryMap.put("status", GenericState.Valid.code);
            queryMap.put("teacherCode", member.getUserName());

            List<ClassPlan> classPlanList = scheduleClassService.selectPlanList(queryMap);

            for (ClassPlan plan : classPlanList) {
                com.stylefeng.guns.modular.system.model.Class classInfo = classService.get(plan.getClassCode());
                if (null == classInfo) {
                    log.warn("Class info is null");
                    continue;
                }

                CourseOutline outline = courseOutlineService.get(plan.getOutlineCode());
                if (null == outline) {
                    log.warn("Class info is null");
                    continue;
                }

                ClassResponser classResponser = ClassResponser.me(classInfo);
                responserList.add(PlanOfDayResponser.me(classResponser, outline));
            }
        }else {

            List<Student> studentList = studentService.listStudents(member.getUserName());

            if (studentList.isEmpty()) {
                log.warn("Member {} have not student");
                throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"学生"});
            }

            List<ScheduleStudent> planList = new ArrayList<ScheduleStudent>();
            for (Student student : studentList) {
                planList.addAll(studentService.listCoursePlan(student.getCode(), requester.getDay(), new Integer[]{1}));
            }

            for (ScheduleStudent plan : planList) {
                com.stylefeng.guns.modular.system.model.Class classInfo = classService.get(plan.getClassCode());
                if (null == classInfo) {
                    log.warn("Class info is null");
                    continue;
                }

                CourseOutline outline = courseOutlineService.get(plan.getOutlineCode());
                if (null == outline) {
                    log.warn("Class info is null");
                    continue;
                }

                ClassResponser classResponser = ClassResponser.me(classInfo);
                classResponser.setStudent(plan.getStudentName());
                responserList.add(PlanOfDayResponser.me(classResponser, outline));
            }
        }

        return PlanOfDayResponserWrapper.me(responserList);
    }

    private Responser assembleClassList(String token, List<Class> classList) {
        Set<com.stylefeng.guns.modular.system.model.Class> classSet = new TreeSet<>(new Comparator<Class>() {
            @Override
            public int compare(Class c1, Class c2) {
                return 0 == c1.getBeginDate().compareTo(c2.getBeginDate()) ? c1.getId().compareTo(c2.getId()) : c1.getBeginDate().compareTo(c2.getBeginDate());
            }
        });
        for(Class classInfo : classList){
            // 去重
            int maxCount = classInfo.getQuato();
            int signedCount = classService.queryOrderedCount(classInfo.getCode());
            classInfo.setSignQuato(signedCount > maxCount ? maxCount : signedCount );

            int maxSchedule = classInfo.getPeriod();
            Map<String, Object> planQueryMap = new HashMap<String, Object>();
//            planQueryMap.put("beginDate", DateUtils.truncate(DateUtil.add(new Date(), Calendar.DAY_OF_MONTH, 1), Calendar.DAY_OF_MONTH));
            // 匹配精确到时间
            planQueryMap.put("beginDate", DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH));
//            planQueryMap.put("beginTime", Integer.parseInt(DateUtil.getHHmm()));
            planQueryMap.put("status", GenericState.Valid.code);
            planQueryMap.put("classCode", classInfo.getCode());

            List<ClassPlan> remainClassPlanList = scheduleClassService.selectPlanList(planQueryMap);
            Iterator<ClassPlan> classPlanIterator = remainClassPlanList.iterator();
            while(classPlanIterator.hasNext()){
                ClassPlan classPlan = classPlanIterator.next();
                Date studyDate = classPlan.getStudyDate();

                if (studyDate.getTime() == DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH).getTime()) {
                    int time = Integer.parseInt(classPlan.getClassTime());
                    int nowTime = Integer.parseInt(DateUtil.getHHmm());
                    if (time < nowTime) {
                        classPlanIterator.remove();
                    }
                }
            }

            BigDecimal perPrice = new BigDecimal(String.valueOf(classInfo.getPrice())).divide(new BigDecimal(maxSchedule), 10, RoundingMode.HALF_UP);
            BigDecimal remainPrice = new BigDecimal(remainClassPlanList.size()).multiply(perPrice);
            BigDecimal signPrice = remainPrice.setScale(0, RoundingMode.HALF_UP);

            classInfo.setSignPrice(signPrice.longValue());
            classSet.add(classInfo);
        }

        return ClassListResponse.me(classSet);
    }


    /**
     * 查询班级已报名数量
     * @param code
     * @return
     */
    @RequestMapping(value = "/count/signed/{code}")
    @ResponseBody
    public Object classSignCount(@PathVariable("code") String code) {
        return classService.queryOrderedCount(code);
    }

}
