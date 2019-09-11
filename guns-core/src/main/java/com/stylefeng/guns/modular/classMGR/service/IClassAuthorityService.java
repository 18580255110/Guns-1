package com.stylefeng.guns.modular.classMGR.service;

import com.baomidou.mybatisplus.service.IService;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.modular.system.model.ClassAuthority;
import com.stylefeng.guns.modular.system.model.Student;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/9/10 22:43
 * @Version 1.0
 */
public interface IClassAuthorityService extends IService<ClassAuthority> {
    /**
     * 授权
     *
     * @param currStudent
     * @param classInfo
     */
    void doAuthenticate(Student currStudent, Class classInfo);

    /**
     * 是否有特权
     *
     * @param student
     * @param classInfo
     * @return
     */
    boolean hasPrivilege(Student student, Class classInfo);
}
