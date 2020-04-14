package com.stylefeng.guns.task.order;

import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.orderMGR.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Map;

/**
 * 过期订单清理任务
 *
 * 在 GunsTaskApplication 中创建Bean
 *
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/7/4 16:26
 * @Version 1.0
 */
public class OrderRecycleTask {

    private final static Logger log = LoggerFactory.getLogger(OrderRecycleTask.class);

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IClassService classService;

    /**
     * 每小时清理一次
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void expireClean(){
        List<Map<String, Object>> orderList = orderService.queryExpiredOrderList();

        log.info("Has {} order need clean", orderList.size());

        for(Map<String, Object> order : orderList){

            if (!(order.containsKey("acceptNo")))
                continue;

            String orderNo = (String) order.get("acceptNo");

            if (null == orderNo)
                continue;

//            Order currOrder = orderService.get((String)order.get("acceptNo"));
//
//            if (null == currOrder)
//                continue;
//
//            currOrder.setStatus(OrderStateEnum.Expire.code);
//            currOrder.setDesc("[" + DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm") + "]订单支付超时");

            try {
                orderService.doExpired((String) order.get("acceptNo"));
//                orderService.updateById(currOrder);
                log.info("Order {} clear with expired!", orderNo);
            }catch(Exception e){
                log.warn("Order {} clean failed!", orderNo, e);
            }
        }
    }
}
