package com.stylefeng.guns.modular.studentMGR.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.education.service.IStudentClassService;
import com.stylefeng.guns.modular.studentMGR.service.IStudentService;
import com.stylefeng.guns.modular.system.dao.ScheduleStudentMapper;
import com.stylefeng.guns.modular.system.dao.StudentMapper;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.util.CodeKit;
import com.stylefeng.guns.util.DateUtil;
import com.stylefeng.guns.util.PathUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * <p>
 * 学生表 服务实现类
 * </p>
 *
 * @author simple.song
 * @since 2018-10-07
 */
@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements IStudentService {
    @Resource
    private StudentMapper studentMapper;

    @Autowired
    private IStudentClassService studentClassService;

    @Autowired
    private IClassService classService;

    @Value("${application.attachment.visit-url}")
    private String attachmentVisitURL;

    @Autowired
    private ScheduleStudentMapper scheduleStudentMapper;

    public Student getOne(Student student){
        return studentMapper.selectOne(student);
    }

    public Map<String, Object> getMap(Integer id) {
        if (null == id)
            return new HashMap<String, Object>();

        return selectMap(new EntityWrapper<Student>().eq("id", id));
    }

    @Override
    public Student addStudent(String userName, Student student) {
        if (null == userName)
            throw new ServiceException (MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS);

        student.setCode(CodeKit.generateStudent());
        student.setUserName(userName);
        student.setStatus(GenericState.Valid.code);
        insert(student);

        return student;
    }

    @Override
    public Student addStudent(String userName, Map<String, Object> studentInfo) {

        if (null == userName)
            throw new ServiceException (MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS);

        Student student = buildStudent(studentInfo);
        student.setUserName(userName);

        insert(student);

        return student;
    }

    @Override
    public String getAvatarViewUrl(Long avatorId) {
        if (null == avatorId || avatorId <= 0)
            return null;

        return PathUtil.generate(attachmentVisitURL, String.valueOf(avatorId));
    }

    @Override
    public void updateStudent(String code, Student newStudent) {
        if (null == code)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND);

        Student existStudent = get(code);

        if (null == existStudent)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND);

        Wrapper<StudentClass> queryWrapper = new EntityWrapper<StudentClass>();
        queryWrapper.eq("student_code", code);
        queryWrapper.eq("status", GenericState.Valid.code);

        List<StudentClass> studentClassList = studentClassService.selectList(queryWrapper);
        Date now = new Date();
        boolean updatable = true;

        for(StudentClass studentClassInfo : studentClassList){
            Class classInfo = classService.get(studentClassInfo.getClassCode());
            if (null == classInfo)
                continue;

            Date beginDate = classInfo.getBeginDate();
            Date endDate = classInfo.getEndDate();
            if (now.compareTo(beginDate) < 0 || now.compareTo(endDate) >= 0 ){
                updatable = false;
                break;
            }
        }

        if (!updatable)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_STATE, new String[]{"学员已报班"});

        String[] ignoreProperties = new String[]{"id", "code", "userName"};
        BeanUtils.copyProperties(newStudent, existStudent, ignoreProperties);

        updateById(existStudent);
    }

    @Override
    public Student get(String code) {
        if (null == code)
            return null;

        return selectOne(new EntityWrapper<Student>().eq("code", code));
    }

    @Override
    public List<Student> listStudents(String userName) {
        if (null == userName)
            return new ArrayList<Student>();

        return selectList(new EntityWrapper<Student>().eq("user_name", userName).eq("status", GenericState.Valid.code));
    }

    @Override
    public List<ScheduleStudent> listCoursePlan(String student, Date day, Integer... states) {

        Wrapper<ScheduleStudent> queryWrapper = new EntityWrapper<ScheduleStudent>();
        queryWrapper.eq("student_code", student);
        queryWrapper.eq("study_date", DateUtil.format(day, "yyyy-MM-dd"));
        queryWrapper.in("status", states);

        return scheduleStudentMapper.selectList(queryWrapper);
    }

    @Override
    public List<ScheduleStudent> listCoursePlan(String classCode, String studentCode, Integer... states) {
        if (null == classCode)
            return new ArrayList<>();

        if (null == studentCode)
            return new ArrayList<>();

        Wrapper<ScheduleStudent> queryWrapper = new EntityWrapper<>();
        queryWrapper.eq("class_code", classCode);
        queryWrapper.eq("student_code", studentCode);
        queryWrapper.in("status", states);

        return scheduleStudentMapper.selectList(queryWrapper);
    }

    @Override
    public boolean doPause(String code) {
        if (null == code)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"学员"});

        Student student = get(code);

        if (null == student)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"学员"});

        student.setStatus(GenericState.Invalid.code);
        updateStudent(code, student);

        return true;
    }

    @Override
    public boolean doResume(String code) {
        if (null == code)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"学员"});

        Student student = get(code);

        if (null == student)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"学员"});

        student.setStatus(GenericState.Valid.code);
        updateStudent(code, student);

        return true;
    }

    private Student buildStudent(Map<String, Object> studentInfo) {

        if (null == studentInfo || studentInfo.isEmpty())
            return null;

        Student newStudent = new Student();
        try {
            org.apache.commons.beanutils.BeanUtils.populate(newStudent, studentInfo);
        } catch (Exception e) {
            // 任何异常，都作为数据非法操作
            throw new ServiceException(MessageConstant.MessageCode.SYS_DATA_ILLEGAL);
        }

        newStudent.setCode(CodeKit.generateStudent());
        newStudent.setStatus(GenericState.Valid.code);

        long avatarId = (long) studentInfo.getOrDefault("avatarId", 0L);
        if (avatarId > 0) {
            newStudent.setAvatar(PathUtil.generate(attachmentVisitURL, String.valueOf(avatarId)));
        }

        return newStudent;
    }


}
