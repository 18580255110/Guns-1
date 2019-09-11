package com.stylefeng.guns.modular.orderMGR.controller;

import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.modular.system.model.Student;

import java.io.Serializable;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/9/10 22:38
 * @Version 1.0
 */
public class AuthRequest implements Serializable {

    private Class classInfo;

    private Student student;

    public Class getClassInfo() {
        return classInfo;
    }

    public void setClassInfo(Class classInfo) {
        this.classInfo = classInfo;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }
}
