package com.stylefeng.guns.modular.orderMGR.warpper;

import com.stylefeng.guns.common.constant.factory.ConstantFactory;
import com.stylefeng.guns.core.base.warpper.BaseControllerWarpper;
import com.stylefeng.guns.modular.system.model.OrderStateEnum;
import com.stylefeng.guns.modular.system.model.PayStateEnum;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 学生信息包装类
 * @author: simple.song
 * Date: 2018/10/7 Time: 10:55
 */
public class OrderWrapper extends BaseControllerWarpper{


    public OrderWrapper(Object obj) {
        super(obj);
    }

    @Override
    protected void warpTheMap(Map<String, Object> map) {
        map.put("payMethodName", ConstantFactory.me().getPayMethodName(Integer.valueOf(map.get("payMethod").toString())));
        map.put("amount", ConstantFactory.me().fenToYuan(map.get("amount").toString()));
        map.put("oldAmount", ConstantFactory.me().fenToYuan(map.get("oldAmount").toString()));

        int state = Integer.parseInt(map.get("status").toString());
        int payState = Integer.parseInt(map.get("payStatus").toString());

        if (OrderStateEnum.InValid.code == state){
            map.put("statusName", OrderStateEnum.InValid.text);
        }else if (OrderStateEnum.Expire.code == state) {
            map.put("statusName", OrderStateEnum.Expire.text);
        }else{
            PayStateEnum payStateEnum = PayStateEnum.instanceOf(payState);
            if (null == payStateEnum){
                map.put("statusName", "未知");
            }else {
                map.put("statusName", payStateEnum.text);
            }
        }
    }
}