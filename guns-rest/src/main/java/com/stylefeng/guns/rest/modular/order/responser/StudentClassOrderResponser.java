package com.stylefeng.guns.rest.modular.order.responser;

import com.stylefeng.guns.modular.system.model.Order;
import com.stylefeng.guns.modular.system.model.Student;
import com.stylefeng.guns.rest.modular.education.responser.ClassResponser;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2020/4/17 16:22
 * @Version 1.0
 */
public class StudentClassOrderResponser implements Comparable<StudentClassOrderResponser> {
    @ApiModelProperty(name = "student", value = "学员信息")
    private Student student;
    @ApiModelProperty(name = "order", value = "订单信息")
    private Order order;
    @ApiModelProperty(name = "classInfo", value = "班级信息")
    private ClassResponser classInfo;

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

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

    public static StudentClassOrderResponser me(Student student, ClassOrderResponser classOrderInfo) {
        StudentClassOrderResponser response = new StudentClassOrderResponser();
        response.setStudent(student);
        response.setOrder(classOrderInfo.getOrder());
        response.setClassInfo(classOrderInfo.getClassInfo());

        return response;
    }

    @Override
    public int compareTo(StudentClassOrderResponser target) {
        if (null == target)
            return 1;

        if (null == this.order || null == target.getOrder())
            return 1;

        if (null == this.student || null == target.getStudent())
            return 1;

        int cmpvalue = 0 - (this.order.getAcceptDate().compareTo(target.getOrder().getAcceptDate()));

        if (0 == cmpvalue)
            cmpvalue = 0 - this.student.getCode().compareTo(target.getStudent().getCode());

        return cmpvalue;
    }

}
