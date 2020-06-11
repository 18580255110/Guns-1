package com.stylefeng.guns.common.constant.factory;

import com.stylefeng.guns.modular.system.model.Dict;

import java.util.List;
import java.util.Map;

/**
 * 常量生产工厂的接口
 *
 * @author fengshuonan
 * @date 2017-06-14 21:12
 */
public interface IConstantFactory {

    /**
     * 根据用户id获取用户名称
     *
     * @author stylefeng
     * @Date 2017/5/9 23:41
     */
    String getUserNameById(Integer userId);

    /**
     * 根据用户id获取用户账号
     *
     * @author stylefeng
     * @date 2017年5月16日21:55:371
     */
    String getUserAccountById(Integer userId);

    /**
     * 通过角色ids获取角色名称
     */
    String getRoleName(String roleIds);

    /**
     * 通过角色id获取角色名称
     */
    String getSingleRoleName(Integer roleId);

    /**
     * 通过角色id获取角色英文名称
     */
    String getSingleRoleTip(Integer roleId);

    /**
     * 获取部门名称
     */
    String getDeptName(Integer deptId);

    /**
     * 获取菜单的名称们(多个)
     */
    String getMenuNames(String menuIds);

    /**
     * 获取菜单名称
     */
    String getMenuName(Long menuId);

    /**
     * 获取菜单名称通过编号
     */
    String getMenuNameByCode(String code);

    /**
     * 获取字典名称
     */
    String getDictName(Integer dictId);
    /**
     * 获取字典名称
     */
    String getDictNameByCode(String dictCode);

    /**
     * 获取通知标题
     */
    String getNoticeTitle(Integer dictId);

    /**
     * 根据字典名称和字典中的值获取对应的名称
     */
    String getDictsByName(String name, Integer val);

    /**
     * 根据字典码和字典中的值获取对应的名称
     */
    String getDictsByCode(String code, String val);

    /**
     * 根据字典码和字典中的值获取对应的名称
     */
    String getDictsByCode(String code, String val, String defaultValue);

    /**
     * 获取性别名称
     */
    String getTeacherTypeName(Integer teacherType);

    /**
     * 获取授课年级名称
     */
    String getGradeName(Integer grade);

    /**
     * 获取性别名称
     */
    String getSexName(Integer sex);

    /**
     * 获取用户登录状态
     */
    String getStatusName(Integer status);

    /**
     * 获取菜单状态
     */
    String getMenuStatusName(Integer status);

    /**
     * 查询字典
     */
    List<Dict> findInDict(Integer id);

    /**
     * 获取被缓存的对象(用户删除业务)
     */
    String getCacheObject(String para);

    /**
     * 获取子部门id
     */
    List<Integer> getSubDeptId(Integer deptid);

    /**
     * 获取所有父部门id
     */
    List<Integer> getParentDeptIds(Integer deptid);

    /**
     * 获取支付状态
     */
    String getPayStatusName(Integer payStatus);

    /**
     * 支付方式
     */
    String getPayMethodName(Integer payMethod);

    /**
     * 支付结果
     */
    String getPayResultName(Integer payResult);

    /**
     * 获取校区地址
     */
    String getSchoolAdressName(Integer classCode);

    /**
     * 开课类型
     */
    String getStudyTimeTypeName(Integer studyTimeType);

    /**
     * 资讯类型
     * @param type
     * @return
     */
    String getContentTypeName(Integer type);

    /**
     * 试题类型
     * @param type
     * @return
     */
    String getQuestionTypeName(Integer type);

    /**
     * 学科类型
     * @param subject
     * @return
     */
    String getsubjectName(Integer subject);


    /**
     * 获取教室类型
     * @param type
     * @return
     */
    String getClassRoomTypeName(Integer type);

    /**
     * 获取学生名称
     * @param studentCode
     * @return
     */
    String getStudentName(String studentCode);

    /**
     * 获取班级名称
     * @param classCode
     * @return
     */
    String getClassName(String classCode);

    /**
     *  查询调课中枚举
     * @param type
     * @return
     */
    String getAdjustTypeName(Integer type);

    /**
     * 班次
     * @param ability
     * @return
     */
    String getAbilityName(Integer ability);

    /**
     * 学期
     * @param cycle
     * @return
     */
    String getCycleName(Integer cycle);

    /**
     * 获取栏目名称
     * @param pcodes
     * @return
     */
    String getColumnName(String pcodes);

    /**
     * 栏目行为类型
     * @param type
     * @return
     */
    String getColumnTypeName(Integer type);

    /**
     * 获取用户状态
     * @param status
     * @return
     */
    String getMemberStatusName(Integer status);

    /**
     * 获取通用状态名称
     *
     * @param status
     * @return
     */
    Object getGenericStateName(Integer status);

    /**
     * 获取授课方式
     *
     * @param method
     * @return
     */
    Object getCourseMethodname(Integer method);

    /**
     *
     * @param amount
     * @return
     */
    String fenToYuan(String amount);

    /**
     * 获取字典Map
     *
     * @param dictCode
     * @return
     */
    Map<String,Object> getdictsMap(String dictCode);


    /**
     * 获取老师名称
     * class表中的teacher字段 不在使用
     * */
    String getTeacheName(String code);
}
