package com.stylefeng.guns.rest.modular.education.requester;

import com.stylefeng.guns.rest.core.SimpleRequester;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/10/24 23:18
 * @Version 1.0
 */
@ApiModel(value = "CrossQueryRequester", description = "跨报班级查询")
public class CrossQueryRequester extends SimpleRequester {

    @ApiModelProperty(name = "student", value = "学员编码", required = true, position = 0, example = "XY181220000001")
    private String student;

    public String getStudent() {
        return student;
    }

    public void setStudent(String student) {
        this.student = student;
    }

    @Override
    public boolean checkValidate() {
        return false;
    }
}
