package com.stylefeng.guns.rest.modular.education.requester;

import com.stylefeng.guns.modular.system.model.Attachment;
import com.stylefeng.guns.rest.core.SimpleRequester;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by 罗华.
 */
@ApiModel(value = "StudentAddRequester", description = "添加学员")
public class StudentAddRequester extends SimpleRequester {

    private static final long serialVersionUID = -1976690924147728427L;
    @ApiModelProperty(name = "userName", value = "用户名", required = true, position = 0, example = "18580255110")
    private String userName;
    @ApiModelProperty(name = "name", value = "学员姓名", required = true, position = 1, example = "小明")
    private String name;
    @ApiModelProperty(name = "gendar", value = "学员性别", required = true, position = 2, example = "1")
    private Integer gendar;
    @ApiModelProperty(name = "grade", value = "在读年级", required = true, position = 3, example = "4")
    private Integer grade;
    @ApiModelProperty(name = "school", value = "在读学校", position = 4, example = "重庆谢家湾小学")
    private String school;
    @ApiModelProperty(name = "targetSchool", value = "目标学校", position = 5, example = "重庆三中")
    private String targetSchool;
    @ApiModelProperty(name = "avator", value = "头像", position = 6)
    private Attachment avator;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getGendar() {
        return gendar;
    }

    public void setGendar(Integer gendar) {
        this.gendar = gendar;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getTargetSchool() {
        return targetSchool;
    }

    public void setTargetSchool(String targetSchool) {
        this.targetSchool = targetSchool;
    }

    public Attachment getAvator() {
        return avator;
    }

    public void setAvator(Attachment avator) {
        this.avator = avator;
    }

    @Override
    public boolean checkValidate() {
        return false;
    }
}
