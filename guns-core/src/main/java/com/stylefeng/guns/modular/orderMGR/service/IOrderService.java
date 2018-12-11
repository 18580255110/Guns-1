package com.stylefeng.guns.modular.orderMGR.service;

import com.baomidou.mybatisplus.service.IService;
import com.stylefeng.guns.modular.orderMGR.OrderAddList;
import com.stylefeng.guns.modular.system.model.Member;
import com.stylefeng.guns.modular.system.model.Order;
import com.stylefeng.guns.modular.system.model.PayMethodEnum;

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
    String order(Member member, OrderAddList addList, PayMethodEnum payMethod, Map<String, Object> extraPostData);
}
