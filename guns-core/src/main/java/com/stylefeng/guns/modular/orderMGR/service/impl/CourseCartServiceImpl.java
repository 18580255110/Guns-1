package com.stylefeng.guns.modular.orderMGR.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.modular.classMGR.service.IClassAuthorityService;
import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.classMGR.service.ICourseService;
import com.stylefeng.guns.modular.education.CourseMethodEnum;
import com.stylefeng.guns.modular.education.service.IStudentClassService;
import com.stylefeng.guns.modular.education.service.IStudentPrivilegeService;
import com.stylefeng.guns.modular.examineMGR.service.IExamineAnswerService;
import com.stylefeng.guns.modular.examineMGR.service.IExamineService;
import com.stylefeng.guns.modular.memberMGR.service.IMemberService;
import com.stylefeng.guns.modular.orderMGR.OrderAddList;
import com.stylefeng.guns.modular.orderMGR.service.ICourseCartService;
import com.stylefeng.guns.modular.orderMGR.service.IOrderService;
import com.stylefeng.guns.modular.studentMGR.service.IStudentService;
import com.stylefeng.guns.modular.studentMGR.service.IStudentZoneService;
import com.stylefeng.guns.modular.system.dao.CourseCartMapper;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.util.CodeKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2018/11/15 22:46
 * @Version 1.0
 */
@Service
public class CourseCartServiceImpl extends ServiceImpl<CourseCartMapper, CourseCart> implements ICourseCartService {
    private final static Logger log = LoggerFactory.getLogger(CourseCartServiceImpl.class);

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IClassService classService;

    @Autowired
    private ICourseService courseService;

    @Autowired
    private IExamineService examineService;

    @Autowired
    private IExamineAnswerService examineAnswerService;

    @Autowired
    private IStudentClassService studentClassService;

    @Autowired
    private IStudentService studentService;

    @Autowired
    private IMemberService memberService;

    @Autowired
    private IStudentZoneService studentZoneService;

    @Autowired
    private IClassAuthorityService classAuthorityService;

    @Autowired
    private IStudentPrivilegeService studentPrivilegeService;

    private static final Map<Integer, String> DayOfWeekMap = new HashMap<Integer, String>();
    private static final Map<Integer, String> DayOfMonthMap = new HashMap<Integer, String>();
    static {
        DayOfWeekMap.put(Calendar.MONDAY, "周一");
        DayOfWeekMap.put(Calendar.TUESDAY, "周二");
        DayOfWeekMap.put(Calendar.WEDNESDAY, "周三");
        DayOfWeekMap.put(Calendar.THURSDAY, "周四");
        DayOfWeekMap.put(Calendar.FRIDAY, "周五");
        DayOfWeekMap.put(Calendar.SATURDAY, "周六");
        DayOfWeekMap.put(Calendar.SUNDAY, "周日");
    }

    @Override
    public String doJoin(Member member, Student student, com.stylefeng.guns.modular.system.model.Class classInfo, boolean skipTest, SignChannel channel, SignType type) {
        if (null == member)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND);

        if (null == student)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND);

        if (null == classInfo)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND);

        // 报名附加信息
        Map<String, Object> extraParams = new HashMap<String, Object>();

        if (SignChannel.Admin.equals(channel)){
            // 后台报名逻辑
            Map<String, Object> queryParams = new HashMap<String, Object>();
            List<Integer> stateList = new ArrayList<>();
            List<Integer> payStateList = new ArrayList<>();

            stateList.add(OrderStateEnum.PreCreate.code);
            stateList.add(OrderStateEnum.Valid.code);

            queryParams.put("stateList", stateList);

            payStateList.add(PayStateEnum.Failed.code);
            payStateList.add(PayStateEnum.NoPay.code);
            queryParams.put("payStateList", payStateList);

            List<Map<String, Object>> orderList = orderService.queryForList(queryParams);
            for(Map<String, Object> order : orderList){
                String orderNo = (String)order.get("accept_no");
                Order existOrder = orderService.get(orderNo);

                if (null == existOrder)
                    continue;

                existOrder.setStatus(OrderStateEnum.InValid.code);
                existOrder.setDesc("后台报名置失效");

                orderService.updateById(existOrder);
            }
        }else{
            // 前台报名逻辑
            int studentGrade = student.getGrade();
            int classGrade = classInfo.getGrade();

            if (studentGrade != classGrade){
                log.info("class grade = {}, student grade = {}", classGrade, studentGrade);
                throw new ServiceException(MessageConstant.MessageCode.GRADE_NOT_MATCH);
            }

            List<CourseCart> existSelected = selectList(new EntityWrapper<CourseCart>()
                    .eq("user_name", member.getUserName())
                    .eq("student_code", student.getCode())
                    .eq("class_code", classInfo.getCode())
                    .ne("status", CourseCartStateEnum.Invalid.code)
            );

            int existSelectedCount = 0;
            if (existSelected.size() > 0){
                // 包含有已失效、过期的订单不纳入已订购的范围
                for(CourseCart courseCart : existSelected){
                    if (CourseCartStateEnum.Valid.code == courseCart.getStatus()){
                        // 有效的购课单项目
                        existSelectedCount++;
                    }else if (CourseCartStateEnum.Ordered.code == courseCart.getStatus()){
                        Order order = orderService.get(courseCart);
                        if (null == order)
                            continue;

                        int orderState = order.getStatus();
                        if (OrderStateEnum.InValid.code == orderState
                                || OrderStateEnum.Expire.code == orderState){
                            continue;
                        }

                        existSelectedCount++;
                    }
                }
            }

            if (existSelectedCount > 0)
                throw new ServiceException(MessageConstant.MessageCode.COURSE_SELECTED);

            // 班型报名权限
            boolean hasPrivilege = studentPrivilegeService.hasPrivilege(student, classInfo);

            if (!hasPrivilege){
                // 没有报名权限

                // 检查是否没有做入学测试
                // 入学测试校验
                // 正常情况下不会出现没有权限但是测试达标的数据， 因为入学测试需要弹出提示让用户做题，所以保留
                if (!skipTest && ClassExaminableEnum.YES.equals(ClassExaminableEnum.instanceOf(classInfo.getExaminable()))){
                    Map<String, Object> queryParams = new HashMap<String, Object>();
                    queryParams.put("classCode", classInfo.getCode());
                    ExamineApply examineApply = examineService.findExamineApply(queryParams);

                    if (null == examineApply)
                        throw new ServiceException(MessageConstant.MessageCode.ORDER_NEED_EXAMINE);

                    Wrapper<ExamineAnswer> queryWrapper = new EntityWrapper<>();
                    queryWrapper.eq("paper_code", examineApply.getPaperCode());
                    queryWrapper.eq("student_code", student.getCode());
                    queryWrapper.ge("score", examineApply.getPassScore());
                    queryWrapper.eq("status", ExamineAnswerStateEnum.Finish.code);
                    int passCount = examineAnswerService.selectCount(queryWrapper);

                    if (0 >= passCount) {
                        log.info("paper = {}, student = {}, passScore = {}", examineApply.getPaperCode(), student.getCode(), examineApply.getPassScore());
                        throw new ServiceException(MessageConstant.MessageCode.ORDER_NEED_EXAMINE);
                    }else{
                        // 如果出现了没有权限但是测试达标的情况， 添加用户班型权限
                        hasPrivilege = true;
                    }
                }
            }

            if (!hasPrivilege)
                throw new ServiceException(MessageConstant.MessageCode.ORDER_NO_PRIVILEGE);

            // 检查班级报名状态
            // 2019-09-30 调整逻辑
            //if (!skipTest && !zoneStudent && !hasPrivilege)
            //    classService.checkJoinState(classInfo, member, student);
            classService.checkJoinState(classInfo, type);
        }

        // 加入选课单
        return select(member, student, classInfo, extraParams);
    }

    @Override
    public void remove(Member member, Student student, Class classInfo) {
        if (null == member)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND);

        if (null == student)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND);

        if (null == classInfo)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND);

        CourseCart existSelected = selectOne(new EntityWrapper<CourseCart>()
                        .eq("user_name", member.getUserName())
                        .eq("student_code", student.getCode())
                        .eq("class_code", classInfo.getCode())
                        .eq("status", CourseCartStateEnum.Valid.code)
        );

        if (null == existSelected)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"选课信息"});

        existSelected.setStatus(CourseCartStateEnum.Invalid.code);
        updateById(existSelected);
    }

    @Override
    public CourseCart get(String code) {
        if (null == code)
            return null;

        return selectOne(new EntityWrapper<CourseCart>().eq("code", code));
    }

    @Override
    public void generateOrder(String userName, String student, String classCode) {

        CourseCart existSelected = selectOne(new EntityWrapper<CourseCart>()
                        .eq("user_name", userName)
                        .eq("student_code", student)
                        .eq("class_code", classCode)
                        .eq("status", CourseCartStateEnum.Valid.code)
        );

        if (null == existSelected)
            return;

        existSelected.setStatus(CourseCartStateEnum.Ordered.code);
        updateById(existSelected);
    }

    @Override
    public void doAutoPreSign(Class classInfo) {

        Class sourceClass = classService.get(classInfo.getPresignSourceClassCode());

        if (null == sourceClass)
            return;

        Wrapper<StudentClass> queryWrapper = new EntityWrapper<StudentClass>();
        queryWrapper.eq("class_code", sourceClass.getCode());
        queryWrapper.eq("status", GenericState.Valid.code);

        List<StudentClass> signedList = studentClassService.selectList(queryWrapper);

        for(StudentClass studentClass : signedList){
            Student student = studentService.get(studentClass.getStudentCode());
            Member member = memberService.get(student.getUserName());

            try {
                // 使用新的报名接口 20190930
                String courseCartCode = doJoin(member, student, classInfo, true, SignChannel.Admin, SignType.Inherit);
                OrderItem orderItem = new OrderItem();
                orderItem.setCourseCartCode(courseCartCode);
                orderItem.setItemObject(OrderItemTypeEnum.Course.code);
                orderItem.setItemObjectCode(classInfo.getCode());
                orderItem.setItemAmount(classInfo.getPrice());
                OrderAddList orderAddList = new OrderAddList();
                orderAddList.add(orderItem);

                Map<String, Object> extendInfo = new HashMap<>();
                orderService.order(member, orderAddList, PayMethodEnum.weixin, extendInfo);
            }catch(Exception e){
                log.error("报名失败, 学员-{}, 班级-{}, 用户-{} ", student.getCode(), classInfo.getCode(), member.getUserName());
                log.error(e.getMessage(), e);
            }
        }
    }

    private String select(Member member, Student student, Class classInfo, Map<String, Object> extraParams) {

        // 查询班级剩余报名额度
        int spareCount = classService.queryOrderedCount(classInfo.getCode());
        if (classInfo.getQuato() <= spareCount){
            throw new ServiceException(MessageConstant.MessageCode.ORDER_NO_CAPACITY);
        }

        CourseCart courseCart = new CourseCart();
        Date now = new Date();

        String courseCartCode = CodeKit.generateCourseCart();
        courseCart.setCode(courseCartCode);
        courseCart.setUserName(member.getUserName());
        courseCart.setStudentCode(student.getCode());
        courseCart.setStudent(student.getName());
        courseCart.setJoinDate(now);
        courseCart.setCourseName(classInfo.getCourseName());
        courseCart.setClassCode(classInfo.getCode());
        courseCart.setClassName(classInfo.getName());

        Course course = courseService.get(classInfo.getCourseCode());
        courseCart.setClassMethod(CourseMethodEnum.instanceOf(course.getMethod()).text);

        courseCart.setClassTime(classInfo.getStudyTimeDesp());
        courseCart.setClassroom(classInfo.getClassRoom());

        courseCart.setTeacher(classInfo.getTeacher());
        courseCart.setAssister(classInfo.getTeacherSecond());

        courseCart.setStatus(CourseCartStateEnum.Valid.code);
        courseCart.setAmount(classInfo.getPrice());
        insert(courseCart);

        return courseCartCode;
    }

}
