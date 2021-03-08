package com.stylefeng.guns.modular.education.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.modular.classMGR.service.ICourseOutlineService;
import com.stylefeng.guns.modular.classMGR.transfer.ClassPlan;
import com.stylefeng.guns.modular.education.service.IScheduleClassService;
import com.stylefeng.guns.modular.education.service.IScheduleStudentService;
import com.stylefeng.guns.modular.system.dao.ScheduleClassMapper;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.util.DateUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2018/12/20 17:49
 * @Version 1.0
 */
@Service
public class ScheduleClassServiceImpl extends ServiceImpl<ScheduleClassMapper, ScheduleClass> implements IScheduleClassService {
    @Autowired
    private ICourseOutlineService courseOutlineService;

    @Autowired
    private ScheduleClassMapper scheduleClassMapper;

    @Autowired
    private IScheduleStudentService scheduleStudentService;

    @Override
    public void deleteClassSchedule(String code) {

        Wrapper<ScheduleClass> classScheduleWrapper = new EntityWrapper<>();
        classScheduleWrapper.eq("class_code", code);

        delete(classScheduleWrapper);
    }

    @Override
    public List<ClassPlan> selectPlanList(Map<String, Object> queryMap) {
        return scheduleClassMapper.selectPlanList(queryMap);
    }

    @Override
    public void scheduleClass(Class classInstance, List<ClassPlan> classPlanList) {
        Wrapper<CourseOutline> outlineQueryWrapper = new EntityWrapper<>();
        outlineQueryWrapper.eq("course_code", classInstance.getCourseCode());
        outlineQueryWrapper.eq("status", GenericState.Valid.code);
        outlineQueryWrapper.orderBy("sort, id");

        List<CourseOutline> outlineList = courseOutlineService.selectList(outlineQueryWrapper);
        Collections.sort(outlineList, new Comparator<CourseOutline>() {
            @Override
            public int compare(CourseOutline co1, CourseOutline co2) {
                return co1.getSort().compareTo(co2.getSort());
            }
        });

        List<ScheduleClass> scheduleClassList = new ArrayList<>();
        int planIndex = 0;
        for(CourseOutline outline : outlineList){
            ScheduleClass scheduleClass = new ScheduleClass();
            ClassPlan classPlan = classPlanList.get(planIndex++);

            scheduleClass.setClassCode(classInstance.getCode());
            scheduleClass.setClassTime(classPlan.getClassTime());
            scheduleClass.setEndTime(classPlan.getEndTime());
            scheduleClass.setOutline(outline.getOutline());
            scheduleClass.setOutlineCode(outline.getCode());
            scheduleClass.setStatus(GenericState.Valid.code);
            scheduleClass.setSort(outline.getSort());
            scheduleClass.setStudyDate(classPlan.getStudyDate());
            scheduleClass.setWeek(classPlan.getWeek());

            scheduleClassList.add(scheduleClass);
        }

        insertBatch(scheduleClassList);
    }

    @Override
    public void refreshClassPlan(Course courseInstance, int newPeriod, List<Class> classes) {
        Wrapper<CourseOutline> outlineQueryWrapper = new EntityWrapper<>();
        outlineQueryWrapper.eq("course_code", courseInstance.getCode());
        outlineQueryWrapper.eq("status", GenericState.Valid.code);
        outlineQueryWrapper.orderBy("sort, id");

        List<CourseOutline> outlineList = courseOutlineService.selectList(outlineQueryWrapper);
        Collections.sort(outlineList, new Comparator<CourseOutline>() {
            @Override
            public int compare(CourseOutline co1, CourseOutline co2) {
                return co1.getSort().compareTo(co2.getSort());
            }
        });

        Date FiveOne = DateUtil.parse("2021-05-01", "yyyy-MM-dd");
        Date FiveTwo = DateUtil.parse("2021-05-02", "yyyy-MM-dd");

        for(Class classInstance : classes) {

            Wrapper<ScheduleClass> classScheduleWrapper = new EntityWrapper<>();
            classScheduleWrapper.eq("class_code", classInstance.getCode());
            classScheduleWrapper.eq("sort", 3);

            List<ScheduleClass> existScheduleClassList = scheduleClassMapper.selectList(classScheduleWrapper);
            if (null == existScheduleClassList || existScheduleClassList.isEmpty()){
                continue;
            }
            ScheduleClass existScheduleClass = existScheduleClassList.get(0);

            List<ScheduleClass> scheduleClassList = new ArrayList<>();

            int planIndex = 0;
            Date autoStudyDate = DateUtils.truncate(DateUtil.add(existScheduleClass.getStudyDate(), Calendar.DAY_OF_MONTH, 7), Calendar.DAY_OF_MONTH);

            for(CourseOutline outline : outlineList){

                if (planIndex < existScheduleClass.getSort()){
                    planIndex++;
                    continue;
                }

                autoStudyDate = DateUtil.add(autoStudyDate, Calendar.DAY_OF_MONTH, 7);
                if (FiveOne.compareTo(autoStudyDate) == 1
                        || FiveTwo.compareTo(autoStudyDate) == 1){
                    autoStudyDate = DateUtil.add(autoStudyDate, Calendar.DAY_OF_MONTH, 7);
                }

                ScheduleClass scheduleClass = new ScheduleClass();

                scheduleClass.setClassCode(classInstance.getCode());
                scheduleClass.setClassTime(existScheduleClass.getClassTime());
                scheduleClass.setEndTime(existScheduleClass.getEndTime());
                scheduleClass.setOutline(outline.getOutline());
                scheduleClass.setOutlineCode(outline.getCode());
                scheduleClass.setStatus(GenericState.Valid.code);
                scheduleClass.setSort(outline.getSort());
                scheduleClass.setStudyDate(autoStudyDate);
                scheduleClass.setWeek(existScheduleClass.getWeek());

                scheduleClassList.add(scheduleClass);

                scheduleStudentService.doRefresh(classInstance, outline, autoStudyDate);
            }

//            System.out.println(scheduleClassList);
            insertBatch(scheduleClassList);


        }

    }
}
