package com.stylefeng.guns.modular.classMGR.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.stylefeng.guns.modular.classMGR.service.IClassAuthorityService;
import com.stylefeng.guns.modular.system.dao.ClassAuthorityMapper;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.modular.system.model.ClassAuthority;
import com.stylefeng.guns.modular.system.model.Student;
import org.springframework.stereotype.Service;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/9/10 22:44
 * @Version 1.0
 */
@Service
public class ClassAuthorityServiceImpl extends ServiceImpl<ClassAuthorityMapper, ClassAuthority> implements IClassAuthorityService {

    @Override
    public void doAuthenticate(Student student, Class classInfo) {

        Integer existCount = selectCount(new EntityWrapper<ClassAuthority>(){
            {
                eq("class_code", classInfo.getCode());
                eq("student_code", student.getCode());
            }
        });

        if (existCount > 0)
            return;

        ClassAuthority classAuthority = new ClassAuthority();
        classAuthority.setClassCode(classInfo.getCode());
        classAuthority.setClassName(classInfo.getName());
        classAuthority.setStudentCode(student.getCode());
        classAuthority.setStudentName(student.getName());

        insert(classAuthority);
    }

    @Override
    public boolean hasPrivilege(Student student, Class classInfo) {
        if(null == student)
            return false;

        if (null == classInfo)
            return false;

        Integer existCount = selectCount(new EntityWrapper<ClassAuthority>(){
            {
                eq("student_code", student.getCode());
                eq("class_code", classInfo.getCode());
            }
        });
        return 0 < existCount;
    }
}
