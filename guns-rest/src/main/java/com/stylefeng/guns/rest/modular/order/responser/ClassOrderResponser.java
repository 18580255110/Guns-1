package com.stylefeng.guns.rest.modular.order.responser;

import com.stylefeng.guns.modular.system.model.Order;
import com.stylefeng.guns.rest.modular.education.responser.ClassResponser;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/1/28 21:31
 * @Version 1.0
 */
@ApiModel(value = "ClassOrderResponser", description = "班级订单信息")
public class ClassOrderResponser implements Comparable<ClassOrderResponser> {

    @ApiModelProperty(name = "order", value = "订单信息")
    private Order order;
    @ApiModelProperty(name = "classInfo", value = "班级信息")
    private ClassResponser classInfo;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public ClassResponser getClassInfo() {
        return classInfo;
    }

    public void setClassInfo(ClassResponser classInfo) {
        this.classInfo = classInfo;
    }

    public static ClassOrderResponser me(Order order, ClassResponser classInfo) {
        ClassOrderResponser response = new ClassOrderResponser();
        response.setOrder(order);
        response.setClassInfo(classInfo);
        return response;
    }

    @Override
    public int compareTo(ClassOrderResponser target) {
        if (null == target)
            return 1;

        if (null == this.order || null == target.getOrder())
            return 1;

        int cmpvalue = 0 - (this.order.getAcceptDate().compareTo(target.getOrder().getAcceptDate()));
        return cmpvalue;
    }
}
