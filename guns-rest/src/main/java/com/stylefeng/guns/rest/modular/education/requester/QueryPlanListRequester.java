package com.stylefeng.guns.rest.modular.education.requester;

import com.stylefeng.guns.rest.core.SimpleRequester;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/1/28 9:07
 * @Version 1.0
 */
@ApiModel(value = "QueryPlanListRequester", description = "课程表列表查询")
public class QueryPlanListRequester extends SimpleRequester {
    @ApiModelProperty(name = "studentCode", value = "学员编码", required = false, position = 0, example = "XY000001")
    private String student;
    @ApiModelProperty(name = "classCode", value = "班级编码", required = false, position = 1, example = "XY000001")
    private String classCode;
    @ApiModelProperty(name = "month", value = "月份", required = false, position = 2, example = "201901")
    private String month;
    @ApiModelProperty(name = "target", value = "目标类型: 1 学生 99 老师", required = false, position = 3, example = "1")
    private Integer target;

    public String getStudent() {
        return student;
    }

    public void setStudent(String student) {
        this.student = student;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Integer getTarget() {
        return target;
    }

    public void setTarget(Integer target) {
        this.target = target;
    }

    @Override
    public boolean checkValidate() {
        return false;
    }
}
