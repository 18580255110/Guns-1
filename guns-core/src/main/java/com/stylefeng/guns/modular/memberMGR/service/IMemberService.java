package com.stylefeng.guns.modular.memberMGR.service;

import com.baomidou.mybatisplus.service.IService;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.modular.system.model.ClassSignAbility;
import com.stylefeng.guns.modular.system.model.Member;

import java.util.Map;
import java.util.Set;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author simple.song
 * @since 2018-10-09
 */
public interface IMemberService extends IService<Member> {

    /**
     * 创建用户
     * @param teacherMember
     */
    void createMember(Member teacherMember);
    /**
     * 新增用户
     *
     * 适用于用户注册方式
     * @param stringObjectMap
     * @param userName
     * @param password
     * @return
     */
    Member createMember(String userName, String password, Map<String, Object> extendParams);

    /**
     * 生成用户名
     *
     * @return
     */
    String generateUserName();

    /**
     * 创建用户
     *  @param userName
     * @param extraParams
     */
    Member createMember(String userName, Map<String, Object> extraParams);

    /**
     * 修改密码
     * @param userName
     * @param password
     */
    void changePassword(String userName, String password);

    /**
     * 根据用户名 获取用户
     *
     * @param userName
     * @return
     */
    Member get(String userName);

    /**
     * 查找我的报班信息
     *
     * @param userName
     * @param student
     * @return
     */
    Map<String, Set<Class>> findMyClasses(String userName, String student);

    /**
     * 查询我的班级信息
     *
     * @param userName
     * @param student
     * @param showHis 是否显示已完结的班级信息
     * @return
     */
    Map<String, Set<Class>> findMyClasses(String userName, String student, boolean showHis);

    /**
     * 判断用户报名类型
     *
     * @param member
     * @return
     */
    ClassSignAbility getSignAbility(Member member);

    /**
     * 停用会员
     *
     * 逻辑删除，置会员状态为失效
     *
     * @param userName
     * @return
     */
    boolean doPause(String userName);

    /**
     * 启用会员
     *
     * @param userName
     * @return
     */
    boolean doResume(String userName);

    /**
     * 根据电话号码查找用户
     *
     * @param mobileNumber
     * @return
     */
    Member getByMobile(String mobileNumber);

    /**
     * 获取用户或创建新用户
     *
     * 当
     * @param mobileNumber
     * @param name
     * @return
     */
    Member getOrCreateIfNotExists(String mobileNumber, String name);
}
