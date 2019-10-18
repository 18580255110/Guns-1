package com.stylefeng.guns.rest.modular.order.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.modular.adjustMGR.service.IAdjustStudentService;
import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.education.service.IStudentClassService;
import com.stylefeng.guns.modular.memberMGR.service.IMemberService;
import com.stylefeng.guns.modular.orderMGR.OrderServiceTypeEnum;
import com.stylefeng.guns.modular.orderMGR.service.ICourseCartService;
import com.stylefeng.guns.modular.orderMGR.service.IOrderService;
import com.stylefeng.guns.modular.payMGR.service.IPayService;
import com.stylefeng.guns.modular.studentMGR.service.IStudentService;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.rest.core.ApiController;
import com.stylefeng.guns.rest.core.Responser;
import com.stylefeng.guns.rest.core.SimpleResponser;
import com.stylefeng.guns.rest.modular.education.responser.ClassResponser;
import com.stylefeng.guns.rest.modular.order.requester.OrderPostRequester;
import com.stylefeng.guns.rest.modular.order.responser.CartListResponser;
import com.stylefeng.guns.rest.modular.order.responser.ClassOrderResponser;
import com.stylefeng.guns.rest.modular.order.responser.OrderListResponser;
import com.stylefeng.guns.rest.modular.order.responser.OrderPostResponser;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.*;

/**
 * 订单
 * Created by 罗华.
 */
@RestController
@RequestMapping("/order")
@Api(tags = "订单接口")
@Validated
public class OrderController extends ApiController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private ICourseCartService courseCartService;

    @Autowired
    private IMemberService memberService;

    @Autowired
    private IStudentService studentService;

    @Autowired
    private IClassService classService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IPayService payService;

    @Autowired
    private IStudentClassService studentClassService;

    @Autowired
    private IAdjustStudentService adjustStudentService;

    @RequestMapping(value = "/cart/join", method = RequestMethod.POST)
    @ApiOperation(value="加入选课单", httpMethod = "POST", response = SimpleResponser.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "student", value = "学员编码", required = false, dataType = "String", example = "XY000001"),
            @ApiImplicitParam(name = "classCode", value = "班级编码", required = true, dataType = "String", example = "BJ000001"),
            @ApiImplicitParam(name = "from", value = "来源", required = true, dataType = "Integer", example = "1")
    }
    )
    public Responser joinCart(
            @NotBlank(message = "班级不能为空")
            @RequestParam(required = true, name = "classCode")
            String classCode,
            @RequestParam(required = false, name = "student")
            String student,
            @RequestParam(required = false, name = "from")
            Integer from
    ){

        Member member = currMember();

        if (null == member)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND);

        if (null == from)
            from = 1; // 正常下单

        Student existStudent = findStudent(member.getUserName(), student);

        if (null == existStudent)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"学员"});

        com.stylefeng.guns.modular.system.model.Class classInfo = classService.get(classCode);

        if (null == classInfo)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"班级 <" + classCode + ">"});

        courseCartService.doJoin(member, existStudent, classInfo, 2 == from ? true : false, SignChannel.App, SignType.Normal);

        return SimpleResponser.success();
    }

    @RequestMapping(value = "/cart/remove", method = RequestMethod.POST)
    @ApiOperation(value="从选课单移除", httpMethod = "POST", response = SimpleResponser.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "student", value = "学员编码", required = false, dataType = "String", example = "XY000001"),
            @ApiImplicitParam(name = "classCode", value = "班级编码", required = true, dataType = "String", example = "BJ000001")
    }
    )
    public Responser removeCart(
            @NotBlank(message = "班级不能为空")
            @RequestParam(name = "classCode", required = true)
            String classCode,
            @RequestParam(name = "student", required = false)
            String student){

        Member member = currMember();

        if (null == member)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"会员"});

        Student existStudent = findStudent(member.getUserName(), student);

        if (null == existStudent)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"学员"});

        com.stylefeng.guns.modular.system.model.Class classInfo = classService.get(classCode);

        if (null == classInfo)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"选课信息"});

        courseCartService.remove(member, existStudent, classInfo);

        return SimpleResponser.success();
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ApiOperation(value="生成订单", httpMethod = "POST", response = OrderPostResponser.class)
    public Responser createOrder(
            @Valid
            @RequestBody
            @ApiParam(required = true, value = "订单提交信息")
            OrderPostRequester requester){

        Member member = currMember();

        PayMethodEnum payMethod = PayMethodEnum.instanceOf(requester.getPayMethod());
        if (null == payMethod || PayMethodEnum.NULL.equals(payMethod))
            throw new ServiceException(MessageConstant.MessageCode.PAY_METHOD_NOT_FOUND);

        OrderServiceTypeEnum serviceType = OrderServiceTypeEnum.instanceOf(requester.getService());

        Map<String, Object> extraPostData = new HashMap<String, Object>();

        OrderPostResponser responser = null;
        switch (serviceType){
            case Order:
                // 报名(订购)
                Order order = orderService.order(member, requester.getAddList(), payMethod, extraPostData);
                // 支付下单
                String paySequence = payService.createPayOrder(order);
                order.setOutSequence(paySequence);
                orderService.updateById(order);

                responser = OrderPostResponser.me(order.getAcceptNo(), paySequence);
                break;
        }

        if (null == responser)
            throw new ServiceException(MessageConstant.MessageCode.SYS_EXCEPTION);

        return responser;
    }

    @ApiOperation(value="选课单", httpMethod = "POST", response = CartListResponser.class)
    @RequestMapping(value = "/cart/list", method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "status", value = "状态 1 已选课 ", required = false, dataType = "Integer", example = "1"),
    })
    public Responser listCart(
            Integer status
    ){
        Member currMember = currMember();

        Wrapper<CourseCart> queryWrapper = new EntityWrapper<CourseCart>();
        queryWrapper.eq("user_name", currMember.getUserName());
        queryWrapper.eq("status", CourseCartStateEnum.Valid.code);

        List<CourseCart> courseCartList = courseCartService.selectList(queryWrapper);

        return CartListResponser.me(courseCartList);
    }

    @ApiOperation(value="订单列表", httpMethod = "POST", response = OrderListResponser.class)
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "status", value = "状态 1 待付款 2 已付款 ", required = false, dataType = "Integer", example = "1"),
    })
    public Responser listOrder(
            @RequestParam(name = "status", required = false)
            Integer status
    ){
        Member member = currMember();

        Wrapper<Order> queryWrapper = new EntityWrapper<Order>();
        queryWrapper.eq("user_name", member.getUserName());
        queryWrapper.eq("status", OrderStateEnum.Valid.code);

        if (null != status) {
            switch(status){
                case 1:
                    queryWrapper.in("pay_status", new Integer[]{PayStateEnum.Failed.code, PayStateEnum.NoPay.code});
                    break;
                case 2:
                    queryWrapper.in("pay_status", new Integer[]{PayStateEnum.Paying.code, PayStateEnum.PayOk.code});
                    break;
                default:
                    queryWrapper.eq("pay_status", status); // 按照实际状态值查询
            }
        }

        List<Order> orderList = orderService.selectList(queryWrapper);

        Set<ClassOrderResponser> classOrderSet = new TreeSet<ClassOrderResponser>();
        for(Order order : orderList){
            List<OrderItem> orderItemList = orderService.listItems(order.getAcceptNo(), OrderItemTypeEnum.Course);

            for(OrderItem classItem : orderItemList){
                CourseCart courseCart = courseCartService.get(classItem.getCourseCartCode());
                Student currStudent = studentService.get(courseCart.getStudentCode());

                String orgClassCode = classItem.getItemObjectCode();
                //adjustStudentService.
                Wrapper<StudentClass> studentClassWrapper = new EntityWrapper<StudentClass>();
                studentClassWrapper.eq("student_code", currStudent.getCode());
                List<StudentClass> studentClassList = studentClassService.selectList(studentClassWrapper);
                for(StudentClass studentClass : studentClassList){
                    if (!(orgClassCode.equals(studentClass.getClassCode())))
                        continue;

                    if (GenericState.Valid.code != studentClass.getStatus()){
                        StudentClass currentValidClass = adjustStudentService.getCurrentValidClass(studentClass);

                        if (null == currentValidClass)
                            // 没有找到有效的订购课程，一般是退费了。
                            continue;

                        orgClassCode = currentValidClass.getClassCode();
                    }
                }

                Class classInfo = classService.get(orgClassCode);
                classOrderSet.add(ClassOrderResponser.me(order, ClassResponser.me(classInfo)));
            }
        }

        return OrderListResponser.me(classOrderSet);
    }

    @ApiOperation(value="订单取消", httpMethod = "POST", response = SimpleResponser.class)
    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderNo", value = "订单号", required = true, dataType = "String", example = "h1hj1901011508051789")
    })
    public Responser cancelOrder(
            @RequestParam(name = "orderNo", required = true)
            String orderNo
    ){
        Member member = currMember();

        orderService.cancel(orderNo);

        return SimpleResponser.success();
    }

    /**
     * 找到一个合适学员信息
     *
     * @param userName
     * @param student
     * @return
     */
    private Student findStudent(String userName, String student) {
        Student existStudent = null;

        if (null != student) {
            existStudent = studentService.get(student);
        }else{
            List<Student> studentList = studentService.listStudents(userName);
            if (null != studentList && studentList.size() > 0)
                existStudent = studentList.get(0);
        }
        return existStudent;
    }

}
