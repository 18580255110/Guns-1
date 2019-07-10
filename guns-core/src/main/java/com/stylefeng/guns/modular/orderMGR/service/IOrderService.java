package com.stylefeng.guns.modular.orderMGR.service;

import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.IService;
import com.stylefeng.guns.modular.orderMGR.OrderAddList;
import com.stylefeng.guns.modular.system.model.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单 服务类
 * </p>
 *
 * @author simple.song
 * @since 2018-10-18
 */
public interface IOrderService extends IService<Order> {
    /**
     * 生成订单
     *
     * @param member
     * @param addList
     * @param payMethod
     * @param extraPostData
     * @return
     */
    Order order(Member member, OrderAddList addList, PayMethodEnum payMethod, Map<String, Object> extraPostData);

    /**
     * 订单项目列表
     *
     * @param acceptNo
     * @param course
     * @return
     */
    List<OrderItem> listItems(String acceptNo, OrderItemTypeEnum type);

    /**
     * 获取订单
     *
     * @param orderNo
     * @return
     */
    Order get(String orderNo);

    /**
     * 获取订单
     *
     * @param courseCart
     * @return
     */
    Order get(CourseCart courseCart);

    /**
     *
     * @param paySequence
     * @param order
     */
    void completePay(String order);

    /**
     * 获取订单用户信息
     * @param orderNo
     * @return
     */
    Member getMemberInfo(String orderNo);

    /**
     * 取消订单
     *
     * @param member
     * @param orderNo
     */
    void cancel(String orderNo);

    /**
     * 支付失败
     *
     * @param order
     * @param message
     */
    void failedPay(String order, String message);

    /**
     * 查询订单
     *
     * @param queryParams
     * @return
     */
    List<Map<String, Object>> queryForList(Map<String, Object> queryParams);

    /**
     * 查询订单
     *
     * @param queryParmas
     * @return
     */
    Page<Map<String, Object>> selectMapsPage(Map<String, Object> queryParmas);

    /**
     * 订单拷贝-转班
     * @param studentCode
     * @param sourceClass
     * @param targetClass
     */
    void doCopyForChange(String studentCode, String sourceClass, String targetClass);

    /**
     * 订单撤销
     * @param orderNo
     */
    void doReverse(String orderNo);

    /**
     * 查询已过期订单
     *
     * 生成订单后24小时没有支付成功认定为过期订单
     * @return
     */
    List<Map<String, Object>> queryExpiredOrderList();
}
