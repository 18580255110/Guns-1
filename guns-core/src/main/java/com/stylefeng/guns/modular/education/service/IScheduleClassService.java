package com.stylefeng.guns.modular.education.service;

import com.baomidou.mybatisplus.service.IService;
import com.stylefeng.guns.modular.classMGR.transfer.ClassPlan;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.modular.system.model.Course;
import com.stylefeng.guns.modular.system.model.ScheduleClass;

import java.util.List;
import java.util.Map;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2018/12/20 8:20
 * @Version 1.0
 */
public interface IScheduleClassService extends IService<ScheduleClass> {
    /**
     * 删除排班安排
     *
     * @param code
     */
    void deleteClassSchedule(String code);

    /**
     * 查询排课表
     *
     * @param queryMap
     * @return
     */
    List<ClassPlan> selectPlanList(Map<String, Object> queryMap);

    /**
     * 生成排班表
     *
     * @param classInstance
     * @param classPlanList
     */
    void scheduleClass(Class classInstance, List<ClassPlan> classPlanList);

    /**
     * 刷新排班表
     *  @param courseInstance
     * @param newPeriod
     * @param classes
     */
    void refreshClassPlan(Course courseInstance, int newPeriod, List<Class> classes);
}
