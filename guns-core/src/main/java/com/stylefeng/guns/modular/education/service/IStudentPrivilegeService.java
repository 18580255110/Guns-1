package com.stylefeng.guns.modular.education.service;

import com.baomidou.mybatisplus.service.IService;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.modular.system.model.Student;
import com.stylefeng.guns.modular.system.model.StudentPrivilege;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/10/10 09:46
 * @Version 1.0
 */
public interface IStudentPrivilegeService extends IService<StudentPrivilege> {
    /**
     * 是否有班级报名权限
     *
     * @param student
     * @param classInfo
     * @return
     */
    boolean hasPrivilege(Student student, Class classInfo);

    /**
     * 是否有班级报名权限
     *
     * @param studentPrivilege
     * @return
     */
    boolean hasPrivilege(StudentPrivilege studentPrivilege);
    /**
     * 授权
     *
     * 通过订购的方式不能调用该方法授权
     *
     * @param studentCode
     * @param classCode
     */
    void grantSignPrivileges(String studentCode, String classCode);

    /**
     * 授权
     *
     * @param studentPrivilege
     */
    void grantSignPrivileges(StudentPrivilege studentPrivilege);

    /**
     * 授权
     * @param studentCode
     * @param classCode
     */
    void grantNextSignPrivileges(String studentCode, String classCode);
}
