package com.stylefeng.guns.task.pay;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.modular.orderMGR.service.IOrderService;
import com.stylefeng.guns.modular.system.model.Order;
import com.stylefeng.guns.modular.system.model.PayStateEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * 模拟缴费（不启用）
 *
 * 在 GunsTaskApplication 中创建Bean
 *
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/1/1 10:38
 * @Version 1.0
 */
public class PayMocker {
    private static final Logger log = LoggerFactory.getLogger(PayMocker.class);

    @Autowired
    private IOrderService orderService;

    @Scheduled(fixedDelay = 5000)
    public void mockPay(){
        Wrapper<Order> orderWrapper = new EntityWrapper<Order>();
        orderWrapper.eq("status", GenericState.Valid.code);
        orderWrapper.eq("pay_status", PayStateEnum.NoPay.code);

        List<Order> orderList = orderService.selectList(orderWrapper);

        log.debug("Need pay order size = {}", orderList.size());
        for(Order order : orderList){
            orderService.completePay(order.getAcceptNo());
        }
    }

}
