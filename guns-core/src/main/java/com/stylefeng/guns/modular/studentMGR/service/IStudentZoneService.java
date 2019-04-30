package com.stylefeng.guns.modular.studentMGR.service;

import com.baomidou.mybatisplus.service.IService;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.modular.system.model.Student;
import com.stylefeng.guns.modular.system.model.StudentZone;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/4/30 06:06
 * @Version 1.0
 */
public interface IStudentZoneService extends IService<StudentZone> {
    /**
     * 是否老学员
     *
     * @param student
     * @param classInfo
     * @return
     */
    boolean isZoneStudent(Student student, Class classInfo);
}
