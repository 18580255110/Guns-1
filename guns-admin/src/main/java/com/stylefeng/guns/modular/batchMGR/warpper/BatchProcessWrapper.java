package com.stylefeng.guns.modular.batchMGR.warpper;

import com.stylefeng.guns.common.constant.factory.ConstantFactory;
import com.stylefeng.guns.core.base.warpper.BaseControllerWarpper;
import com.stylefeng.guns.modular.system.model.BatchProcessStatusEnum;
import com.stylefeng.guns.modular.system.model.BatchServiceEnum;

import java.util.Map;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/3/18 00:55
 * @Version 1.0
 */
public class BatchProcessWrapper extends BaseControllerWarpper {

    public BatchProcessWrapper(Object obj) {
        super(obj);
    }

    @Override
    protected void warpTheMap(Map<String, Object> map) {
        map.put("statusName", ConstantFactory.me().getGenericStateName((Integer) map.get("status")));
        map.put("serviceName", BatchServiceEnum.instanceOf((Integer) map.get("service")).text);
        map.put("workStatusName", BatchProcessStatusEnum.instanceOf((Integer) map.get("workStatus")).text);
    }
}
