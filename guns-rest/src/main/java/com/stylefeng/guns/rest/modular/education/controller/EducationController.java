package com.stylefeng.guns.rest.modular.education.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.modular.adjustMGR.service.IAdjustStudentService;
import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.classMGR.service.ICourseOutlineService;
import com.stylefeng.guns.modular.classMGR.service.ICourseService;
import com.stylefeng.guns.modular.classMGR.transfer.ClassPlan;
import com.stylefeng.guns.modular.classRoomMGR.service.IClassroomService;
import com.stylefeng.guns.modular.education.service.IScheduleClassService;
import com.stylefeng.guns.modular.education.service.IScheduleStudentService;
import com.stylefeng.guns.modular.education.service.IStudentClassService;
import com.stylefeng.guns.modular.education.transfer.StudentPlan;
import com.stylefeng.guns.modular.memberMGR.service.IMemberService;
import com.stylefeng.guns.modular.studentMGR.service.IStudentService;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.modular.system.model.Class;
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
    private ICourseOutlineService courseOutlineService;

    @Autowired
    private IScheduleStudentService scheduleStudentService;

    @Autowired
    private IScheduleClassService scheduleClassService;

    @Autowired
    private IAdjustStudentService adjustStudentService;

    @Autowired
    private IStudentClassService studentClassService;

    @Value("${application.education.adjust.maxTimes:4}")
    private int maxAdjustTimes = 4;

    @RequestMapping(value = "/class/list", method = RequestMethod.POST)
    @ApiOperation(value="可报名班级列表", httpMethod = "POST", response = ClassListResponse.class)
    public Responser listClass(ClassQueryRequester requester){

        Member member = currMember();

        Map<String, Object> queryMap = requester.toMap();

        // 当前开放报名的班级
//        queryMap.put("signDate", DateUtil.format(DateUtils.truncate(now, Calendar.DAY_OF_MONTH), "yyyy-MM-dd"));
        queryMap.put("signable", ClassSignableEnum.YES.code);
        List<com.stylefeng.guns.modular.system.model.Class> classList = classService.queryListForSign(queryMap);

        return assembleClassList(classList);
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
        Date now = new Date();

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

        List<Student> studentList = studentClassService.listSignedStudent(queryMap);

        Set<StudentResponse> studentSet = new HashSet<>();
        for(Student student : studentList){
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
        changeClassQuery.put("grades", String.valueOf(currClass.getGrade()));
        changeClassQuery.put("abilities", String.valueOf(currClass.getAbility()));
        changeClassQuery.put("subjects", course.getSubject());
        changeClassQuery.put("signable", ClassSignableEnum.YES.code);

        List<com.stylefeng.guns.modular.system.model.Class> classList = classService.queryListForChange(changeClassQuery);

        Set<Class> classSet = new HashSet<>();
        for (com.stylefeng.guns.modular.system.model.Class classInfo : classList){
            // 查询班级剩余报名额度
            Wrapper<StudentClass> queryWrapper = new EntityWrapper<>();
            queryWrapper.eq("class_code", classInfo.getCode());
            queryWrapper.eq("status", GenericState.Valid.code);
            int existCount = studentClassService.selectCount(queryWrapper);

            if (existCount >= classInfo.getQuato() ){
                continue;
            }

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
    public Responser listClass4CrossNew(@RequestBody
                                            @Valid
                                                    AdjustQueryRequester requester){

        Member member = currMember();

        List<Student> studentList = studentService.listStudents(member.getUserName());
        Set<Class> classSignSet = new HashSet<>();
        Set<Class> classChangeSet = new HashSet<>();
        for(Student student : studentList) {
            List<StudentClass> studentClassInfoList = studentClassService.selectCurrentClassInfo(student);
            if (null == studentClassInfoList || studentClassInfoList.isEmpty()){
                // 老学员新报
                classSignSet.addAll(listClass4CrossWithSign(student));
            }else{
                for(StudentClass studentClass : studentClassInfoList) {
                    classChangeSet.addAll(listClass4CrossWithChange(studentClass.getClassCode()));
                }
            }
        }

        if (classSignSet.isEmpty() && classChangeSet.isEmpty()){

        }

        return ClassCrossListResponse.me(classSignSet, classChangeSet);

    }

    private Collection<? extends Class> listClass4CrossWithSign(Student student) {
        // 用户历史报班列表
        Map<String, Object> historyQueryMap = new HashMap<>();
        historyQueryMap.put("studyFinished", true);
        List<com.stylefeng.guns.modular.system.model.Class> hisClassList = studentClassService.selectMemberHistorySignedClass(student, historyQueryMap);

        // 只春、秋学期才能支持续保、跨报
        Iterator<Class> hisClassIterator = hisClassList.iterator();
        Set<Integer> subjects = new HashSet<>();
        while(hisClassIterator.hasNext()){
            Class hisClassInfo = hisClassIterator.next();
            Course hisCourseInfo = courseService.get(hisClassInfo.getCourseCode());
            int cycle = hisClassInfo.getCycle();

            switch(cycle){
                case 1:
                case 2:
                    subjects.add(Integer.parseInt(hisCourseInfo.getSubject()));
                    break;
                default:
                    hisClassIterator.remove();
                    break;
            }
        }

        Set<Class> classSet = new HashSet<>();
        if (hisClassList.isEmpty()){
            // 没有订购过课程的用户，直接返回
            return classSet;
        }

        // 老用户可以享受优先报名资格
        Map<String, Object> changeClassQuery = new HashMap<String, Object>();
        changeClassQuery.put("signable", ClassSignableEnum.YES.code);

        StringBuilder subjectBuilder = new StringBuilder();
        for(int subject : subjects){
            subjectBuilder.append(subject).append(",");
        }
        if (subjectBuilder.length() > 0)
            changeClassQuery.put("subjects", subjectBuilder.substring(0, subjectBuilder.length() - 1));

        List<com.stylefeng.guns.modular.system.model.Class> classList = classService.queryListForCross(changeClassQuery);

        for (com.stylefeng.guns.modular.system.model.Class classInfo : classList){
            if (null == classInfo){
                continue;
            }
            if (!(classInfo.isValid())){
                continue;
            }

            classSet.add(classInfo);
        }

        return classSet;
    }

    private Collection<? extends Class> listClass4CrossWithChange(String classCode) {

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
            Wrapper<StudentClass> queryWrapper = new EntityWrapper<>();
            queryWrapper.eq("class_code", classInfo.getCode());
            queryWrapper.eq("status", GenericState.Valid.code);
            int existCount = studentClassService.selectCount(queryWrapper);

            if (existCount >= classInfo.getQuato() ){
                continue;
            }

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

        return SimpleResponser.success();
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

        adjustStudentService.adjustClass(member, student, fromData, destData);

        return SimpleResponser.success();
    }

    @ApiOperation(value="课程表", httpMethod = "POST", response = PlanListResponser.class)
    @RequestMapping("/course/plan/list")
    public Responser planList(
            @ApiParam(required = true, value = "课程表查询")
            @RequestBody
            @Valid
            QueryPlanListRequester requester){

        Member member = currMember();

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

    @ApiOperation(value="单天课程表", httpMethod = "POST", response = PlanOfDayResponserWrapper.class)
    @RequestMapping(value = "/course/plan/day", method = RequestMethod.POST)
    public Responser queryPlanOfDay(
            @ApiParam(required = true, value = "单天课程表查询")
            @RequestBody
            @Valid
            QueryPlanOfDayRequester requester, HttpServletRequest request
    ){
        Member member = currMember();

        List<Student> studentList = studentService.listStudents(member.getUserName());

        if (studentList.isEmpty()){
            log.warn("Member {} have not student");
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"学生"});
        }

        List<ScheduleStudent> planList = new ArrayList<ScheduleStudent>();
        for (Student student : studentList){
            planList.addAll(studentService.listCoursePlan(student.getCode(), requester.getDay(), new Integer[]{1}));
        }

        List<PlanOfDayResponser> responserList = new ArrayList<PlanOfDayResponser>();
        for(ScheduleStudent plan : planList){
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

            responserList.add(PlanOfDayResponser.me(ClassResponser.me(classInfo), outline));
        }

        return PlanOfDayResponserWrapper.me(responserList);
    }

    private Responser assembleClassList(List<Class> classList) {
        Set<com.stylefeng.guns.modular.system.model.Class> classSet = new HashSet<>();
        for(Class classInfo : classList){
            // 去重
            classSet.add(classInfo);
        }

        return ClassListResponse.me(classSet);
    }

}
