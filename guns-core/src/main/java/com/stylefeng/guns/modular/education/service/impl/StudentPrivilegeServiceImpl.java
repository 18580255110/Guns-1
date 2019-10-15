package com.stylefeng.guns.modular.education.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.classMGR.service.ICourseService;
import com.stylefeng.guns.modular.education.service.IStudentPrivilegeService;
import com.stylefeng.guns.modular.studentMGR.service.IStudentService;
import com.stylefeng.guns.modular.system.dao.StudentPrivilegeMapper;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.modular.system.model.Class;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/10/10 09:48
 * @Version 1.0
 */
@Service
public class StudentPrivilegeServiceImpl extends ServiceImpl<StudentPrivilegeMapper, StudentPrivilege> implements IStudentPrivilegeService {
    private static final Logger log = LoggerFactory.getLogger(ScheduleStudentServiceImpl.class);

    @Autowired
    private ICourseService courseService;

    @Autowired
    private IClassService classService;

    @Autowired
    private IStudentService studentService;

    @Override
    public boolean hasPrivilege(Student student, Class classInfo) {
        if(null == student)
            return false;

        if (null == classInfo)
            return false;

        Course courseInfo = courseService.get(classInfo.getCourseCode());

        if (null == courseInfo)
            return false;

        Integer existCount = selectCount(new EntityWrapper<StudentPrivilege>(){
            {
                eq("student_code", student.getCode());
                eq("academic_year", classInfo.getAcademicYear());
                eq("subject", Integer.parseInt(courseInfo.getSubject()));
                eq("grade", courseInfo.getGrade());
                eq("cycle", classInfo.getCycle());
                eq("ability", classInfo.getAbility());
                eq("status", GenericState.Valid.code);
                eq("type", 1);
            }
        });
        return 0 < existCount;
    }

    @Override
    public boolean hasPrivilege(StudentPrivilege studentPrivilege) {
        return 0 < selectCount(new EntityWrapper<StudentPrivilege>(){
            {
                eq("student_code", studentPrivilege.getStudentCode());
                eq("academic_year", studentPrivilege.getAcademicYear());
                eq("subject", studentPrivilege.getSubject());
                eq("grade", studentPrivilege.getGrade());
                eq("cycle", studentPrivilege.getCycle());
                eq("ability", studentPrivilege.getAbility());
                eq("type", 1);
                eq("status", GenericState.Valid.code);
            }
        });
    }

    @Override
    public void grantSignPrivileges(String studentCode, String classCode) {
        Student student = studentService.get(studentCode);
        log.info("Grant sign privilege student = {}, class = {}", studentCode, classCode);
        if (null == student)
            return;
        Class classInfo = classService.get(classCode);
        if (null == classInfo)
            return ;

        Course course = courseService.get(classInfo.getCourseCode());
        if (null == course)
            return;

        boolean hasPrivilege = hasPrivilege(student, classInfo);

        if (!hasPrivilege){
            StudentPrivilege studentPrivilege = new StudentPrivilege();
            studentPrivilege.setStudentCode(studentCode);
            studentPrivilege.setStudentName(student.getName());
            studentPrivilege.setAcademicYear(classInfo.getAcademicYear());
            studentPrivilege.setCycle(classInfo.getCycle());
            studentPrivilege.setGrade(course.getGrade());
            studentPrivilege.setAbility(classInfo.getAbility());
            studentPrivilege.setType(1);
            studentPrivilege.setStatus(GenericState.Valid.code);
            studentPrivilege.setComments(classInfo.getName());

            insert(studentPrivilege);
        }
    }
}
