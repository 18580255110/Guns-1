package com.stylefeng.guns.rest.modular.order.responser;

import com.stylefeng.guns.rest.core.Responser;
import com.stylefeng.guns.rest.core.SimpleResponser;
import io.swagger.annotations.ApiModelProperty;

import java.util.Collection;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2020/4/17 16:29
 * @Version 1.0
 */
public class StudentOrderListResponser extends SimpleResponser {

    private static final long serialVersionUID = -1050961070768457292L;

    @ApiModelProperty(name = "data", value = "订单所含课程")
    private Collection<StudentClassOrderResponser> data;

    public Collection<StudentClassOrderResponser> getData() {
        return data;
    }

    public void setData(Collection<StudentClassOrderResponser> data) {
        this.data = data;
    }

    public static Responser me(Collection<StudentClassOrderResponser> orderList) {
        StudentOrderListResponser response = new StudentOrderListResponser();
        response.setCode(SUCCEED);
        response.setMessage("查询成功");
        response.setData(orderList);

        return response;
    }

}
