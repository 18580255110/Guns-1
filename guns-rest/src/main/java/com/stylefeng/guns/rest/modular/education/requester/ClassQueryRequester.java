package com.stylefeng.guns.rest.modular.education.requester;

import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.rest.core.SimpleRequester;
import com.stylefeng.guns.rest.core.SimpleResponser;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import sun.net.www.content.text.Generic;

import javax.validation.constraints.NotBlank;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2018/12/3 19:42
 * @Version 1.0
 */
@ApiModel(value = "ClassQueryRequester", description = "班级查询")
public class ClassQueryRequester extends SimpleRequester {
    @ApiModelProperty(name = "name", value = "班级名称", required = false, position = 1, example = "数学")
    private String name;
    @ApiModelProperty(name = "subjects", value = "科目", required = false, position = 2, example = "语文,数学")
    private String subjects = "";
    @ApiModelProperty(name = "classCycles", value = "学期 1 春季班； 2 秋季班； 3 寒假班； 4 短期班； 99 活动类", required = false, position = 3, example = "2,3")
    private String classCycles = "";
    @ApiModelProperty(name = "abilities", value = "班次 1 启航； 2 敏学； 3 勤思； 4 创新； 9 诊断； 99 其他", required = false, position = 4, example = "1,4")
    private String abilities = "";
    @ApiModelProperty(name = "methods", value = "授课方式 1 面授 2 在线 3 双师", required = false, position = 5, example = "1,2")
    private String methods = "";
    @ApiModelProperty(name = "weekdays", value = "上课时间(星期) 1 周一  7 周日", required = false, position = 6, example = "6,7")
    private String weekdays = "";
    @ApiModelProperty(name = "grades", value = "年级", required = false, position = 7, example = "1,2,3")
    private String grades = "";
    @ApiModelProperty(name = "teachers", value = "教师", required = false, position = 8, example = "LS000004,LS000007")
    private String teachers = "";
    @ApiModelProperty(name = "teacherCode", value = "主讲老师", required = false, position = 9, example = "LS000001")
    private String teacherCode;
    @ApiModelProperty(name = "assisterCode", value = "辅导老师", required = false, position = 10, example = "LS000101")
    private String assisterCode;
    @ApiModelProperty(name = "classroomCode", value = "教室", required = false, position = 11, example = "JS000001")
    private String classroomCode;
    @ApiModelProperty(name = "signType", value = "报名方式: 1 正常报名； 2 跨报", required = false, position = 12, example = "1")
    private Integer signType;
    @ApiModelProperty(name = "page", value = "页数", required = false, position = 12, example = "1")
    private Integer page = 1;
    @ApiModelProperty(name = "size", value = "记录数", required = false, position = 12, example = "1")
    private Integer size = Integer.MAX_VALUE;
    @ApiModelProperty(name = "status", value = "状态", required = false, position = 13, example = "1")
    private Integer status = GenericState.Valid.code;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubjects() {
        return subjects;
    }

    public void setSubjects(String subjects) {
        this.subjects = subjects;
    }

    public String getClassCycles() {
        return classCycles;
    }

    public void setClassCycles(String classCycles) {
        this.classCycles = classCycles;
    }

    public String getAbilities() {
        return abilities;
    }

    public void setAbilities(String abilities) {
        this.abilities = abilities;
    }

    public String getMethods() {
        return methods;
    }

    public void setMethods(String methods) {
        this.methods = methods;
    }

    public String getWeekdays() {
        return weekdays;
    }

    public void setWeekdays(String weekdays) {
        this.weekdays = weekdays;
    }

    public String getGrades() {
        return grades;
    }

    public void setGrades(String grades) {
        this.grades = grades;
    }

    public String getTeachers() {
        return teachers;
    }

    public void setTeachers(String teachers) {
        this.teachers = teachers;
    }

    public String getTeacherCode() {
        return teacherCode;
    }

    public void setTeacherCode(String teacherCode) {
        this.teacherCode = teacherCode;
    }

    public String getAssisterCode() {
        return assisterCode;
    }

    public void setAssisterCode(String assisterCode) {
        this.assisterCode = assisterCode;
    }

    public String getClassroomCode() {
        return classroomCode;
    }

    public void setClassroomCode(String classroomCode) {
        this.classroomCode = classroomCode;
    }

    public Integer getSignType() {
        return signType;
    }

    public void setSignType(Integer signType) {
        this.signType = signType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    @Override
    public boolean checkValidate() {
        return false;
    }
}
