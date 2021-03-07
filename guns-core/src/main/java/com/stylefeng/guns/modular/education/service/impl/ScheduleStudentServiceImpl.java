package com.stylefeng.guns.modular.education.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.education.service.IScheduleClassService;
import com.stylefeng.guns.modular.education.service.IScheduleStudentService;
import com.stylefeng.guns.modular.education.transfer.StudentPlan;
import com.stylefeng.guns.modular.system.dao.ScheduleStudentMapper;
import com.stylefeng.guns.modular.system.dao.StudentClassMapper;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.util.CodeKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2018/12/17 14:42
 * @Version 1.0
 */
@Service
public class ScheduleStudentServiceImpl extends ServiceImpl<ScheduleStudentMapper, ScheduleStudent> implements IScheduleStudentService {
    private static final Logger log = LoggerFactory.getLogger(ScheduleStudentServiceImpl.class);

    @Autowired
    private ScheduleStudentMapper scheduleStudentMapper;

    @Autowired
    private IScheduleClassService scheduleClassService;

    @Autowired
    private StudentClassMapper studentClassMapper;

    @Autowired
    private IClassService classService;

    @Override
    public ScheduleStudent getAdjustedSchedule(String planCode) {

        if (null == planCode)
            return null;

        Wrapper<ScheduleStudent> queryMapper = new EntityWrapper<>();
        queryMapper.eq("fcode", planCode);
        queryMapper.eq("status", GenericState.Valid.code);

        return selectOne(queryMapper);
    }

    @Override
    public void doAdjust(String studentCode, String outlineCode, String sourceClass, String targetClass) {
        Wrapper<ScheduleStudent> loadWrapper = new EntityWrapper<ScheduleStudent>();
        loadWrapper.eq("student_code", studentCode);
        loadWrapper.eq("class_code", sourceClass);
        loadWrapper.eq("outline_code", outlineCode);
        loadWrapper.eq("status", GenericState.Valid.code);

        ScheduleStudent currPlan = selectOne(loadWrapper);

        if (null == currPlan){
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"课程表"});
        }

        currPlan.setStatus(GenericState.Invalid.code);
        currPlan.setRemark("调入班级["+targetClass+"]， 课时["+outlineCode+"]");
        updateById(currPlan);

        Wrapper<ScheduleClass> classQueryWrapper = new EntityWrapper<ScheduleClass>();
        classQueryWrapper.eq("class_code", targetClass);
        classQueryWrapper.eq("outline_code", outlineCode);
        classQueryWrapper.eq("status", GenericState.Valid.code);

        ScheduleClass targetScheduleClass = scheduleClassService.selectOne(classQueryWrapper);

        if (null == targetScheduleClass){
            throw new ServiceException(MessageConstant.MessageCode.ADJUST_TARGET_FAILED, new String[]{"班级排课信息无效"});
        }

        Class classInfo = classService.get(targetScheduleClass.getClassCode());

        String[] ignoreProperties = new String[]{"id", "code", "pcode", "fcode", "pcodes", "remark"};
        ScheduleStudent newPlan = new ScheduleStudent();
        BeanUtils.copyProperties(currPlan, newPlan, ignoreProperties);
        newPlan.setCode(CodeKit.generateStudentSchedule());
        newPlan.setClassCode(targetClass);
        newPlan.setClassName(classInfo.getName());
        newPlan.setStudyDate(targetScheduleClass.getStudyDate());
        newPlan.setPcode(currPlan.getCode());
        newPlan.setFcode(null == currPlan.getFcode() ? currPlan.getCode() : currPlan.getFcode());
        newPlan.setPcodes(null == currPlan.getPcodes() ? currPlan.getCode() : currPlan.getPcodes() + "," + currPlan.getCode());
        newPlan.setStatus(GenericState.Valid.code);

        insert(newPlan);
    }

    @Override
    public void doChange(String studentCode, String sourceClass, String targetClass) {
        Wrapper<ScheduleStudent> loadWrapper = new EntityWrapper<ScheduleStudent>();
        loadWrapper.eq("student_code", studentCode);
        loadWrapper.eq("class_code", sourceClass);
        loadWrapper.eq("status", GenericState.Valid.code);

        List<ScheduleStudent> currPlanList = selectList(loadWrapper);
        String studentName = "";

        if (null == currPlanList || currPlanList.isEmpty()){
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"课程表"});
        }

        for(ScheduleStudent plan : currPlanList) {
            // 转班不需要记录历史节点
            studentName = plan.getStudentName();
            plan.setStatus(GenericState.Invalid.code);
            plan.setRemark("转入班级[" + targetClass + "]");
        }
        updateBatchById(currPlanList);

        Wrapper<ScheduleClass> classQueryWrapper = new EntityWrapper<ScheduleClass>();
        classQueryWrapper.eq("class_code", targetClass);
        classQueryWrapper.eq("status", GenericState.Valid.code);

        List<ScheduleClass> targetScheduleClassList = scheduleClassService.selectList(classQueryWrapper);

        if (null == targetScheduleClassList || targetScheduleClassList.isEmpty()) {
            throw new ServiceException(MessageConstant.MessageCode.ADJUST_TARGET_FAILED, new String[]{"班级排课信息无效"});
        }

        Class classInfo = classService.get(targetClass);
        List<ScheduleStudent> planList = new ArrayList<ScheduleStudent>();
        for(ScheduleClass scheduleClass : targetScheduleClassList) {
            ScheduleStudent newPlan = new ScheduleStudent();

            newPlan.setCode(CodeKit.generateStudentSchedule());
            newPlan.setStudentCode(studentCode);
            newPlan.setStudentName(studentName);
            newPlan.setClassCode(targetClass);
            newPlan.setClassName(classInfo.getName());
            newPlan.setOutlineCode(scheduleClass.getOutlineCode());
            newPlan.setOutline(scheduleClass.getOutline());
            newPlan.setStudyDate(scheduleClass.getStudyDate());
            newPlan.setStatus(GenericState.Valid.code);

            planList.add(newPlan);
        }
        insertBatch(planList);
    }

    @Override
    public List<StudentPlan> selectPlanList(Map<String, Object> queryMap) {
        return scheduleStudentMapper.selectPlanList(queryMap);
    }

    @Override
    public void doReverse(String studentCode, String classCode) {
        Wrapper<ScheduleStudent> queryWrapper = new EntityWrapper<ScheduleStudent>();
        queryWrapper.eq("student_code", studentCode);
        queryWrapper.eq("class_code", classCode);
        queryWrapper.eq("status", GenericState.Valid.code);

        List<ScheduleStudent> studentPlanList = selectList(queryWrapper);

        for (ScheduleStudent plan : studentPlanList){
            plan.setStatus(GenericState.Invalid.code);
            plan.setRemark("已退费");
            updateById(plan);
        }
    }

    @Override
    public void doRefresh(Class classInstance, CourseOutline outline, Date autoStudyDate) {

        Wrapper<StudentClass> studentClassWrapper = new EntityWrapper<>();
        studentClassWrapper.eq("class_code", classInstance.getCode());
        studentClassWrapper.eq("status", 1);
        List<StudentClass> existStudentClass = studentClassMapper.selectList(studentClassWrapper);
        List<ScheduleStudent> scheduleStudentList = new ArrayList<>();

        for(StudentClass studentClass : existStudentClass) {

            Wrapper<ScheduleStudent> studentScheduleWrapper = new EntityWrapper<>();
            studentScheduleWrapper.eq("class_code", classInstance.getCode());
            studentScheduleWrapper.eq("student_code", studentClass.getStudentCode());
            studentScheduleWrapper.eq("status", 1);
            studentScheduleWrapper.orderBy("id", false);
            List<ScheduleStudent> existStudentScheduleList = scheduleStudentMapper.selectList(studentScheduleWrapper);

            if (null == existStudentScheduleList || existStudentScheduleList.isEmpty()){
                continue;
            }
            ScheduleStudent lastPlan = existStudentScheduleList.get(0);

            ScheduleStudent newPlan = new ScheduleStudent();
            newPlan.setCode(CodeKit.generateStudentSchedule());
            newPlan.setStudentCode(studentClass.getStudentCode());
            newPlan.setStudentName(lastPlan.getStudentName());
            newPlan.setClassCode(classInstance.getCode());
            newPlan.setClassName(lastPlan.getClassName());
            newPlan.setOutlineCode(outline.getCode());
            newPlan.setOutline(outline.getOutline());
            newPlan.setStudyDate(autoStudyDate);
            newPlan.setStatus(GenericState.Valid.code);

            scheduleStudentList.add(newPlan);
        }

        insertBatch(scheduleStudentList);
    }
}
