package com.stylefeng.guns.modular.orderMGR.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.stylefeng.guns.common.constant.factory.PageFactory;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.education.service.IScheduleClassService;
import com.stylefeng.guns.modular.education.service.IScheduleStudentService;
import com.stylefeng.guns.modular.education.service.IStudentClassService;
import com.stylefeng.guns.modular.memberMGR.service.IMemberService;
import com.stylefeng.guns.modular.orderMGR.OrderAddList;
import com.stylefeng.guns.modular.orderMGR.service.ICourseCartService;
import com.stylefeng.guns.modular.orderMGR.service.IOrderService;
import com.stylefeng.guns.modular.payMGR.service.IPayService;
import com.stylefeng.guns.modular.system.dao.OrderItemMapper;
import com.stylefeng.guns.modular.system.dao.OrderMapper;
import com.stylefeng.guns.modular.system.dao.OrderMemberMapper;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.util.CodeKit;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <p>
 * 订单 服务实现类
 * </p>
 *
 * @author simple.song
 * @since 2018-10-18
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderMemberMapper orderMemberMapper;

    @Autowired
    private ICourseCartService courseCartService;

    @Autowired
    private IPayService payService;

    @Autowired
    private IScheduleClassService scheduleClassService;

    @Autowired
    private IScheduleStudentService scheduleStudentService;

    @Autowired
    private IStudentClassService studentClassService;

    @Autowired
    private IMemberService memberService;

    @Autowired
    private IClassService classService;

    @Override
    public Order order(Member member, OrderAddList addList, PayMethodEnum payMethod, Map<String, Object> extraPostData) {

        List<OrderItem> itemList = buildOrderItem(addList, extraPostData);
        // 算费
        long amount = calculate(itemList);
        Order order = buildOrder(member, amount, payMethod, extraPostData);
        // 生成订单
        insert(order);
        // 生成订单项
        String orderNo = order.getAcceptNo();
        for(OrderItem orderItem : itemList){
            orderItem.setOrderNo(orderNo);
            orderItemMapper.insert(orderItem);

            CourseCart courseCart = courseCartService.get(orderItem.getCourseCartCode());
            if (null != courseCart && OrderItemTypeEnum.Course.equals(OrderItemTypeEnum.instanceOf(orderItem.getItemObject()))){
                // 清理购物车信息
                courseCartService.generateOrder(member.getUserName(), courseCart.getStudentCode(), orderItem.getItemObjectCode());
            }
        }

        // 生成订单用户信息
        OrderMember orderMember = new OrderMember();
        orderMember.setOrderNo(orderNo);
        orderMember.setUsername(member.getUserName());

        orderMemberMapper.insert(orderMember);
        return order;
    }

    @Override
    public List<OrderItem> listItems(String orderNo, OrderItemTypeEnum type) {
        if (null == orderNo)
            return new ArrayList<OrderItem>();

        Wrapper<OrderItem> orderItemWrapper = new EntityWrapper<OrderItem>();
        orderItemWrapper.eq("order_no", orderNo);
        orderItemWrapper.eq("item_object", type.code);

        return orderItemMapper.selectList(orderItemWrapper);
    }

    @Override
    public Order get(String orderNo) {
        if (null == orderNo)
            return null;

        Wrapper<Order> queryWrapper = new EntityWrapper<Order>();
        queryWrapper.eq("accept_no", orderNo);
        return selectOne(queryWrapper);
    }

    @Override
    public Order get(CourseCart courseCart) {
        if (null == courseCart || null == courseCart.getCode())
            return null;

        Wrapper<OrderItem> orderItemWrapper = new EntityWrapper<OrderItem>();
        orderItemWrapper.eq("course_cart_code", courseCart.getCode());
        orderItemWrapper.eq("item_object", OrderItemTypeEnum.Course.code);

        List<OrderItem> orderItemList = orderItemMapper.selectList(orderItemWrapper);

        if (null == orderItemList || orderItemList.isEmpty())
            return null;

        OrderItem courseItem = orderItemList.get(0);

        if (null == courseItem)
            return null;

        String orderNo = courseItem.getOrderNo();

        if (null == orderNo)
            return null;

        return get(orderNo);
    }

    @Override
    public void completePay(String order) {
        Order currOrder = get(order);

        if (PayStateEnum.NoPay.code != currOrder.getPayStatus()){
            log.info(" order({}) is handled! ", currOrder.getAcceptNo());
            return;
        }

        currOrder.setPayStatus(PayStateEnum.PayOk.code);
        currOrder.setPayResult(PayStateEnum.PayOk.text);
        currOrder.setPayDate(new Date());

        updateById(currOrder);

        Wrapper<OrderItem> orderItemWrapper = new EntityWrapper<OrderItem>();
        orderItemWrapper.eq("order_no", order);
        orderItemWrapper.eq("item_object", OrderItemTypeEnum.Course.code);

        List<OrderItem> orderItemList = orderItemMapper.selectList(orderItemWrapper);

        for(OrderItem courseItem : orderItemList){

            CourseCart courseCart = courseCartService.get(courseItem.getCourseCartCode());

            Wrapper<ScheduleClass> scheduleClassWrapper = new EntityWrapper<ScheduleClass>();
            scheduleClassWrapper.eq("class_code", courseCart.getClassCode());
            scheduleClassWrapper.eq("status", GenericState.Valid.code);

            List<ScheduleClass> classScheduleList = scheduleClassService.selectList(scheduleClassWrapper);

            // 学员报班信息表
            StudentClass studentClass = new StudentClass();
            studentClass.setOrderNo(order);
            studentClass.setStudentCode(courseCart.getStudentCode());
            studentClass.setClassCode(courseCart.getClassCode());
            studentClass.setClassName(courseCart.getClassName());
            studentClass.setPeriod(classScheduleList.size());
            studentClass.setStatus(GenericState.Valid.code);

            studentClassService.insert(studentClass);

            for(ScheduleClass classSchedule : classScheduleList){
                ScheduleStudent scheduleStudent = new ScheduleStudent();
                scheduleStudent.setCode(CodeKit.generateStudentSchedule());
                scheduleStudent.setStudentCode(courseCart.getStudentCode());
                scheduleStudent.setStudentName(courseCart.getStudent());
                scheduleStudent.setClassCode(classSchedule.getClassCode());
                scheduleStudent.setClassName(courseCart.getClassName());
                scheduleStudent.setOutlineCode(classSchedule.getOutlineCode());
                scheduleStudent.setOutline(classSchedule.getOutline());
                scheduleStudent.setStudyDate(classSchedule.getStudyDate());
                scheduleStudent.setStatus(GenericState.Valid.code);

                scheduleStudentService.insert(scheduleStudent);
            }
        }
    }

    @Override
    public Member getMemberInfo(String orderNo) {

        if (null == orderNo)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"订单号"});

        Order order = get(orderNo);

        if (null == order)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"订单"});

        OrderMember orderMemberWrapper = new OrderMember();
        orderMemberWrapper.setOrderNo(orderNo);

        OrderMember existOrderMember = orderMemberMapper.selectOne(orderMemberWrapper);

        if (null == existOrderMember)
            return null;

        return memberService.get(existOrderMember.getUsername());
    }

    @Override
    public void cancel(String orderNo) {
        if (null == orderNo)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"订单号"});

        Order order = get(orderNo);

        if (null == order)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"订单"});

        order.setStatus(OrderStateEnum.InValid.code);
        updateById(order);
    }

    @Override
    public void failedPay(String order, String message) {
        Order currOrder = get(order);

        currOrder.setPayStatus(PayStateEnum.Failed.code);
        currOrder.setPayResult(message);
        currOrder.setPayDate(new Date());

        updateById(currOrder);
    }

    @Override
    public List<Map<String, Object>> queryForList(Map<String, Object> queryParams) {
        Map<String, Object> arguments = buildQueryArguments(queryParams);
        return orderMapper.queryForList(arguments);
    }

    @Override
    public Page<Map<String, Object>> selectMapsPage(Map<String, Object> queryMap) {
        Map<String, Object> arguments = buildQueryArguments(queryMap);
        Page<Map<String, Object>> page = new PageFactory<Map<String, Object>>().defaultPage();

        List<Map<String, Object>> resultMap = orderMapper.selectPageList(page, arguments);
        page.setRecords(resultMap);
        return page;
    }

    @Override
    public void doCopyForChange(String studentCode, String sourceClass, String targetClass) {
        String sourceOrderNo = studentClassService.getOrderNo(studentCode, sourceClass);

        if (null == sourceOrderNo) {
            log.error("===> Source order no not found");
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"原班级编码"});
        }

        Order sourceOrder = get(sourceOrderNo);
        if (null == sourceOrder) {
            log.error("===> Source order not found");
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"原班级"});
        }

        Order copyOrder = new Order();
        String[] ignoreProperties = new String[]{"id"};
        BeanUtils.copyProperties(sourceOrder, copyOrder, ignoreProperties);


    }

    private Map<String, Object> buildQueryArguments(Map<String, Object> queryParams) {
        Iterator<String> queryKeyIter = queryParams.keySet().iterator();
        Map<String, Object> arguments = new HashMap<String, Object>();

        while(queryKeyIter.hasNext()){
            String key = queryKeyIter.next();
            Object value = queryParams.get(key);

            if (null == value)
                continue;

            if (String.class.equals(value.getClass())){
                if (StringUtils.isEmpty((String) value))
                    continue;
            }
            arguments.put(key, queryParams.get(key));
        }
        return arguments;
    }

    /**
     * 算费
     *
     * @param itemList
     * @return
     */
    private long calculate(List<OrderItem> itemList) {

        long amount = 0L;

        for(OrderItem orderItem : itemList){
            OrderItemTypeEnum type = OrderItemTypeEnum.instanceOf(orderItem.getItemObject());
            switch (type){
                case Course:
                    amount += calcCourse(orderItem);
                    break;
                default:
                    break;
            }
        }
        return amount;
    }

    /**
     * 课程算费
     *
     * @param orderItem
     * @return
     */
    private long calcCourse(OrderItem orderItem) {
        return orderItem.getItemAmount();
    }

    private List<OrderItem> buildOrderItem(OrderAddList addList, Map<String, Object> extraPostData) {
        List<OrderItem> orderItemList = new ArrayList<OrderItem>();
        for(OrderItem orderItem : addList){
            OrderItemTypeEnum type = OrderItemTypeEnum.instanceOf(orderItem.getItemObject());

            if (OrderItemTypeEnum.Course.equals(type)){
                // 是课程， 但是没有选课单号
                String courseCartCode = orderItem.getCourseCartCode();
                if (null == courseCartCode)
                    throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"选课单号"});

                CourseCart courseCart = courseCartService.get(courseCartCode);
                if (null == courseCart)
                    throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"选课单"} );

                // 是课程，判断是否已下订单
                Wrapper<OrderItem> queryWrapper = new EntityWrapper<OrderItem>();
                queryWrapper.eq("course_cart_code", orderItem.getCourseCartCode());

                if (orderItemMapper.selectCount(queryWrapper) > 0)
                    // 不能重复提交
                    throw new ServiceException(MessageConstant.MessageCode.ORDER_REQUEST_ORDERED);
                // 班级信息
                Class classInfo = classService.get(courseCart.getClassCode());
                if (null == classInfo)
                    throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"班级信息"});

                // 查询班级剩余报名额度
                if (classService.isNotSpared(classInfo)){
                    throw new ServiceException(MessageConstant.MessageCode.ORDER_NO_CAPACITY);
                }
            }
            orderItem.setItemCode(CodeKit.generateOrderItem());
            orderItemList.add(orderItem);
        }
        return orderItemList;
    }

    private Order buildOrder(Member member, long amount, PayMethodEnum payMethod, Map<String, Object> extraPostData) {
        Order order = new Order();
        Date now = new Date();

        order.setAcceptNo(CodeKit.generateOrder());
        order.setAcceptDate(now);
        order.setAmount(amount);
        order.setPayStatus(PayStateEnum.NoPay.code);
        order.setPayMethod(payMethod.code);
        order.setUserName(member.getUserName());
        order.setDesc("订购课程");

        order.setStatus(OrderStateEnum.Valid.code);

        return order;
    }
}
