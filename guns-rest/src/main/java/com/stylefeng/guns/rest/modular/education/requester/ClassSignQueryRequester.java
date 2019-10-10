package com.stylefeng.guns.rest.modular.education.requester;

import com.stylefeng.guns.rest.core.SimpleRequester;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/1/30 18:36
 * @Version 1.0
 */
@ApiModel(value = "ClassSignQueryRequester", description = "班级报名查询")
public class ClassSignQueryRequester extends SimpleRequester {

    @ApiModelProperty(name = "classCode", value = "班级编码", required = true, position = 0, example = "BJ000001")
    @NotBlank(message = "班级编码不能为空")
    private String classCode;
    @ApiModelProperty(name = "outlineCode", value = "课时编码", required = false, position = 1, example = "KS000001")
    private String outlineCode;
    @ApiModelProperty(name = "studentName", value = "学员名称", required = false, position = 2, example = "XY181230000001")
    private String studentName;
    @ApiModelProperty(name = "targetSchoolName", value = "目标学校关键字", required = false, position = 3, example = "树人")
    private String targetSchoolName;

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String getOutlineCode() {
        return outlineCode;
    }

    public void setOutlineCode(String outlineCode) {
        this.outlineCode = outlineCode;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getTargetSchoolName() {
        return targetSchoolName;
    }

    public void setTargetSchoolName(String targetSchoolName) {
        this.targetSchoolName = targetSchoolName;
    }

    @Override
    public boolean checkValidate() {
        return false;
    }
}
