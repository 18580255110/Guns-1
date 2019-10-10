package com.stylefeng.guns.modular.education.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.modular.classMGR.service.ICourseService;
import com.stylefeng.guns.modular.education.service.IStudentPrivilegeService;
import com.stylefeng.guns.modular.system.dao.StudentPrivilegeMapper;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.modular.system.model.Class;
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
    @Autowired
    private ICourseService courseService;

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
                eq("grade", courseInfo.getGrade());
                eq("cycle", classInfo.getCycle());
                eq("ability", classInfo.getAbility());
                eq("status", GenericState.Valid.code);
            }
        });
        return 0 < existCount;
    }
}
