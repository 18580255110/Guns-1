package com.stylefeng.guns.modular.statisticMGR.student.warpper;

import com.stylefeng.guns.common.constant.factory.ConstantFactory;
import com.stylefeng.guns.core.base.warpper.BaseControllerWarpper;

import java.util.Map;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/2/26 9:39
 * @Version 1.0
 */
public class StudentSignWrapper extends BaseControllerWarpper {

    public StudentSignWrapper(Object obj) {
        super(obj);
    }

    @Override
    protected void warpTheMap(Map<String, Object> map) {
        map.put("payMethodName", ConstantFactory.me().getPayMethodName(Integer.valueOf(map.get("payMethod").toString())));
        map.put("amount", ConstantFactory.me().fenToYuan(map.get("amount").toString()));

    }
}
