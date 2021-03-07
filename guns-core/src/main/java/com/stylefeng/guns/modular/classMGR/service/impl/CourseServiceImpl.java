package com.stylefeng.guns.modular.classMGR.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.classMGR.service.ICourseOutlineService;
import com.stylefeng.guns.modular.classMGR.service.ICourseService;
import com.stylefeng.guns.modular.education.service.IScheduleClassService;
import com.stylefeng.guns.modular.system.dao.CourseMapper;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.modular.system.model.Course;
import com.stylefeng.guns.util.CodeKit;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2018/11/25 23:52
 * @Version 1.0
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements ICourseService {

    @Autowired
    private IClassService classService;

    @Autowired
    private ICourseOutlineService courseOutlineService;

    @Autowired
    private IScheduleClassService scheduleClassService;

    @Override
    public Course get(String code) {
        if (null == code)
            return null;

        return selectOne(new EntityWrapper<Course>().eq("code", code));
    }

    @Override
    public Map<String, Object> getMap(String code) {
        if (null == code)
            return new HashMap<String, Object>();

        return selectMap(new EntityWrapper<Course>().eq("code", code));
    }

    @Override
    public boolean doPause(String code) {
        if (null == code)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND);

        Course existCourse = get(code);

        if (null == existCourse)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND);

        if (!existCourse.isValid()){
            return false;
        }

        Wrapper<Class> classQueryWrapper = new EntityWrapper<>();
        classQueryWrapper.eq("course_code", code);
        classQueryWrapper.eq("status", GenericState.Valid.code);

        int classCount = classService.selectCount(classQueryWrapper);

//        if (classCount > 0)
//            // 对象正在使用
//            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_ONAIR, new String[]{"课程已有排班计划"});

        existCourse.setStatus(GenericState.Invalid.code);
        updateById(existCourse);

        return true;
    }

    @Override
    public boolean doResume(String code) {
        if (null == code)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND);

        Course existCourse = get(code);

        if (null == existCourse)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND);

        existCourse.setStatus(GenericState.Valid.code);
        updateById(existCourse);

        return true;
    }

    @Override
    public void create(Course course) {

        if (null == course)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS);

        course.setCode(CodeKit.generateCourse());
        course.setStatus(GenericState.Valid.code);

        insert(course);

        // 添加默认课程大纲
        courseOutlineService.addCourseOutline(course);
    }

    @Override
    public void update(Course course) {
        if (null == course)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS);

        Course existCourse = get(course.getCode());

        if (null == existCourse)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS);

        if (!existCourse.getId().equals(course.getId()))
            throw new ServiceException(MessageConstant.MessageCode.SYS_DATA_ILLEGAL);

        int newPeriod = course.getPeriod();
        int oldPeriod = existCourse.getPeriod();

        if (newPeriod != oldPeriod) {
            courseOutlineService.refreshCourseOutline(course, newPeriod);

            Wrapper<Class> queryWrapper = new EntityWrapper<Class>();
            queryWrapper.eq("course_code", existCourse.getCode());
            queryWrapper.eq("status", GenericState.Valid.code);
            int classRelationCount = classService.selectCount(queryWrapper);

            if (classRelationCount > 0){
                scheduleClassService.refreshClassPlan(course, newPeriod, classService.selectList(queryWrapper));
            }
        }

        String[] ignoreProperties = new String[]{"id", "code"};
        BeanUtils.copyProperties(course, existCourse, ignoreProperties);

        updateById(existCourse);
    }
}
