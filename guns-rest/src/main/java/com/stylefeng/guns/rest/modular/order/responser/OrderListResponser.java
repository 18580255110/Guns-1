package com.stylefeng.guns.rest.modular.order.responser;

import com.stylefeng.guns.modular.system.model.Order;
import com.stylefeng.guns.rest.core.Responser;
import com.stylefeng.guns.rest.core.SimpleResponser;
import com.stylefeng.guns.rest.modular.education.responser.ClassResponser;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Collection;
import java.util.List;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2018/12/1 8:43
 * @Version 1.0
 */
@ApiModel(value = "OrderListResponser", description = "订单列表")
public class OrderListResponser extends SimpleResponser {
    @ApiModelProperty(name = "data", value = "订单所含课程")
    private Collection<ClassOrderResponser> data;

    public Collection<ClassOrderResponser> getData() {
        return data;
    }

    public void setData(Collection<ClassOrderResponser> data) {
        this.data = data;
    }

    public static Responser me(Collection<ClassOrderResponser> orderList) {
        OrderListResponser response = new OrderListResponser();
        response.setCode(SUCCEED);
        response.setMessage("查询成功");
        response.setData(orderList);
        return response;
    }
}
