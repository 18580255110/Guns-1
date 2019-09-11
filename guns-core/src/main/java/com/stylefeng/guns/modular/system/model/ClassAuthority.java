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
 * @Date 2019/9/10 16:00
 * @Version 1.0
 */
@TableName("tb_class_authority")
@ApiModel(value = "ClassAuthority", description = "班级权限")
public class ClassAuthority extends Model<ClassAuthority> {

    /**
     * 标示
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(hidden = true)
    private Long id;

    @TableField("class_code")
    @ApiModelProperty(name = "classCode", value = "班级编码", position = 0, example="BJ000001")
    private String classCode;

    @TableField("class_name")
    @ApiModelProperty(name = "className", value = "班级名称", position = 0, example="语文提高班")
    private String className;

    @TableField("student_code")
    @ApiModelProperty(name = "studentCode", value = "学员编码", position = 0, example="XY0000000001")
    private String studentCode;

    @TableField("student_name")
    @ApiModelProperty(name = "studentName", value = "学员名称", position = 0, example="小虹")
    private String studentName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    @Override
    protected Serializable pkVal() {
        return null;
    }
}
