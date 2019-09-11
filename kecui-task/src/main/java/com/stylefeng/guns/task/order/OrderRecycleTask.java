package com.stylefeng.guns.task.order;

import com.stylefeng.guns.modular.orderMGR.service.IOrderService;
import com.stylefeng.guns.modular.system.model.Order;
import com.stylefeng.guns.modular.system.model.OrderStateEnum;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 过期订单清理任务
 *
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/7/4 16:26
 * @Version 1.0
 */
@Component
public class OrderRecycleTask {

    private final static Logger log = LoggerFactory.getLogger(OrderRecycleTask.class);

    @Autowired
    private IOrderService orderService;

    /**
     * 每小时清理一次
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void expireClean(){
        List<Map<String, Object>> orderList = orderService.queryExpiredOrderList();

        log.info("Has {} order need clean", orderList.size());

        for(Map<String, Object> order : orderList){
            Order currOrder = orderService.get((String)order.get("acceptNo"));

            if (null == currOrder)
                continue;

            currOrder.setStatus(OrderStateEnum.Expire.code);
            currOrder.setDesc("[" + DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm") + "]订单支付超时");

            try {
                orderService.updateById(currOrder);
                log.info("Order {} clear with expired!", currOrder.getAcceptNo());
            }catch(Exception e){
                log.warn("Order {} clean failed!", currOrder.getAcceptNo(), e);
            }
        }
    }
}
