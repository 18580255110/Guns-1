package com.stylefeng.guns.modular.education.service;

import com.baomidou.mybatisplus.service.IService;
import com.stylefeng.guns.modular.education.transfer.StudentPlan;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.modular.system.model.CourseOutline;
import com.stylefeng.guns.modular.system.model.ScheduleStudent;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2018/12/17 9:23
 * @Version 1.0
 */
public interface IScheduleStudentService extends IService<ScheduleStudent>{
    /**
     * 获取调整后的课程计划
     *
     * @param preCode
     * @return
     */
    ScheduleStudent getAdjustedSchedule(String preCode);

    /**
     * 调课
     * ˙
     * @param studentCode
     * @param outlineCode
     * @param sourceClass
     * @param targetClass
     */
    void doAdjust(String studentCode, String outlineCode, String sourceClass, String targetClass);

    /**
     * 转班
     *
     * @param studentCode
     * @param sourceClass
     * @param targetClass
     */
    void doChange(String studentCode, String sourceClass, String targetClass);

    /**
     * 查询学员课程表
     *
     * @param queryMap
     * @return
     */
    List<StudentPlan> selectPlanList(Map<String, Object> queryMap);

    /**
     * 撤销订单
     *
     * @param studentCode
     * @param classCode
     */
    void doReverse(String studentCode, String classCode);

    /**
     * 更新报班计划
     *  @param classInstance
     * @param outline
     * @param autoStudyDate
     */
    void doRefresh(Class classInstance, CourseOutline outline, Date autoStudyDate);
}
