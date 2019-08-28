package com.stylefeng.guns.modular.education.service;

import com.baomidou.mybatisplus.service.IService;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.modular.system.model.Class;

import java.util.List;
import java.util.Map;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2018/12/28 9:47
 * @Version 1.0
 */
public interface IStudentClassService extends IService<StudentClass> {
    /**
     * 转班
     * 
     * @param studentCode
     * @param sourceClass
     * @param targetClass
     */
    void doChange(String studentCode, String sourceClass, String targetClass);

    /**
     * 班级报班学员列表
     *
     * @param queryMap
     * @return
     */
    List<Student> listSignedStudent(Map<String, Object> queryMap);

    /**
     * 用户历史报班列表
     *
     * @param student
     * @param historyQueryMap
     * @return
     */
    List<Class> selectMemberHistorySignedClass(Student student, Map<String, Object> historyQueryMap);

    /**
     * 学员当前报班
     *
     * @param student
     * @return
     */
    List<StudentClass> selectCurrentClassInfo(Student student);

    /**
     * 获取学员报班订单号
     *
     * @param studentCode
     * @param sourceClass
     * @return
     */
    String getOrderNo(String studentCode, String sourceClass);

    /**
     * 撤销报名
     *
     * @param studentCode
     * @param classCode
     */
    void doReverse(String studentCode, String classCode);

    /**
     * 撤销报名
     *
     * @param orderNo
     */
    void doReverse(String orderNo);
}
