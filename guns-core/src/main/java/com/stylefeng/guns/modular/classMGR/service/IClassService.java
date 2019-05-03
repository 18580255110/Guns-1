package com.stylefeng.guns.modular.classMGR.service;

import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.modular.classMGR.transfer.ClassPlan;
import com.stylefeng.guns.modular.system.model.Class;
import com.baomidou.mybatisplus.service.IService;
import com.stylefeng.guns.modular.system.model.Member;
import com.stylefeng.guns.modular.system.model.Student;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 班级 服务类
 * </p>
 *
 * @author simple.song
 * @since 2018-10-20
 */
public interface IClassService extends IService<Class> {
    /**
     * 根据条件查询班级列表
     *
     * @param queryParams
     * @return
     */
    Page<Map<String, Object>> selectMapsPage(Map<String, Object> queryParams);

    /**
     * 根据条件查询可报名班级列表
     *
     * @param queryMap
     * @return
     */
    List<Class> queryListForSign(Map<String, Object> queryMap);

    /**
     * 根据班级编码获取班级
     *
     * @param code
     * @return
     */
    Class get(String code);

    /**
     * 根据班级编码获取班级Map
     *
     * @param code
     * @return
     */
    Map<String,Object> getMap(String code);

    /**
     * 检查班级报名状态
     *
     * @param classInfo
     * @param member
     * @param student
     *
     * @Exception ServiceException
     */
    void checkJoinState(Class classInfo, Member member, Student student);

    /**
     * 创建班级
     *
     * @param classInstance
     * @param classPlanList
     */
    void createClass(Class classInstance, List<ClassPlan> classPlanList);

    /**
     * 更新班级信息
     *
     * @param classInstance
     * @param classPlanList
     */
    void updateClass(Class classInstance, List<ClassPlan> classPlanList);

    /**
     * 删除班级信息
     *
     * 逻辑删
     * @param classCode
     */
    void deleteClass(String classCode);

    /**
     * 停止报名
     *
     * @param classCode
     */
    void stopSign(String classCode);

    /**
     * 启用报名
     *
     * @param classCode
     */
    void resumeSign(String classCode);

    /**
     * 停用入学测试
     *
     * @param classCode
     */
    void stopExaminable(String classCode);

    /**
     * 启用入学测试
     *
     * @param classCode
     */
    void resumeExaminable(String classCode);

    /**
     * 查询老师班级列表
     *
     * @param userName
     * @param queryMap
     * @return
     */
    List<Class> queryListForTeacher(String userName, Map<String, Object> queryMap);

    /**
     * 查询可转入班级列表
     *
     * @param changeClassQuery
     * @return
     */
    List<Class> queryListForChange( Map<String, Object> changeClassQuery );

    /**
     *
     * @param changeClassQuery
     * @return
     */
    List<Class> queryListForCross(Map<String, Object> changeClassQuery);

    /**
     *
     * @param changeClassQuery
     * @return
     */
    List<Class> queryListForCrossChange(Map<String, Object> changeClassQuery);

    /**
     * 查询班级剩余可报名数
     *
     * @param code
     * @return
     */
    Integer queryOrderedCount(String code);
}
