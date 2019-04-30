package com.stylefeng.guns.modular.system.model;

import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/4/30 06:02
 * @Version 1.0
 */
@TableName("tb_student_zone")
@ApiModel(value = "StudentZone", description = "学员保留")
public class StudentZone  extends Model<Student> {

    private static final long serialVersionUID = 1L;

    /**
     * 标示
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(hidden = true)
    private Long id;
    /**
     * 学员编码: XY+年月日（6位）+8位顺序码
     */
    @ApiModelProperty(name = "code", value = "学员编码", position = 0, example = "XY000001")
    private String code;
    /**
     * 学员名称
     */
    @ApiModelProperty(name = "name", value = "学员姓名", position = 1, example = "小明")
    private String name;
    /**
     * 头像
     */
    @ApiModelProperty(name = "avatar", value = "学员头像", position = 2, example = "http://192.168.10.1/deed.jpg")
    private String avatar;
    /**
     * 性别
     * 这个字段单词写错了，最后要纠正
     */
    @ApiModelProperty(name = "gender", value = "性别", position = 3, example = "1")
    private Integer gender;

    /**
     * 年龄
     */
    @ApiModelProperty(name = "age", value = "年龄", position = 4, example = "12")
    private Integer age;
    /**
     * 在读年级
     */
    @ApiModelProperty(name = "grade", value = "在读年级", position = 5, example = "4")
    private Integer grade;
    /**
     * 在读学校
     */
    @ApiModelProperty(name = "school", value = "在读学校", position = 6, example = "谢家湾小学")
    private String school;
    /**
     * 目标学校
     */
    @TableField("target_school")
    @ApiModelProperty(name = "targetSchool", value = "目标学校", position = 7, example = "重庆三中")
    private String targetSchool;
    /**
     * 用户名
     */
    @TableField("user_name")
    @ApiModelProperty(hidden = true)
    private String userName;
    /**
     * 状态
     */
    @ApiModelProperty(name = "status", value = "状态", position = 8, example = "1")
    private Integer status;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public void setGender(String gender) {
        try {
            setGender(Integer.parseInt(gender));
        } catch (Exception e) {
        }
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setAge(String age) {
        try {
            setAge(Integer.parseInt(age));
        } catch (Exception e) {
        }
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public void setGrade(String grade) {
        try {
            setGrade(Integer.parseInt(grade));
        } catch (Exception e) {
        }
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", code=" + code +
                ", name=" + name +
                ", avatar=" + avatar +
                ", gender=" + gender +
                ", age=" + age +
                ", grade=" + grade +
                ", school=" + school +
                ", targetSchool=" + targetSchool +
                ", userName=" + userName +
                ", status=" + status +
                "}";
    }

    public boolean isValid() {
        if (null == this.status)
            return false;

        return StudentStateEnum.Valid.code == this.status;
    }
}
