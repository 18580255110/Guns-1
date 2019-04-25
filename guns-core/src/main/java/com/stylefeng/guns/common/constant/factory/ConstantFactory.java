package com.stylefeng.guns.common.constant.factory;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.stylefeng.guns.common.constant.cache.Cache;
import com.stylefeng.guns.common.constant.cache.CacheKey;
import com.stylefeng.guns.common.constant.state.ManagerStatus;
import com.stylefeng.guns.common.constant.state.MenuStatus;
import com.stylefeng.guns.core.support.StrKit;
import com.stylefeng.guns.log.LogObjectHolder;
import com.stylefeng.guns.modular.system.dao.*;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.util.Convert;
import com.stylefeng.guns.util.SpringContextHolder;
import com.stylefeng.guns.util.ToolUtil;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 常量的生产工厂
 *
 * @author fengshuonan
 * @date 2017年2月13日 下午10:55:21
 */
@Component
@DependsOn("springContextHolder")
public class ConstantFactory implements IConstantFactory {
    private static final BigDecimal MONEY_TRANSFORM = new BigDecimal(100);

    private RoleMapper roleMapper = SpringContextHolder.getBean(RoleMapper.class);
    private DeptMapper deptMapper = SpringContextHolder.getBean(DeptMapper.class);
    private DictMapper dictMapper = SpringContextHolder.getBean(DictMapper.class);
    private UserMapper userMapper = SpringContextHolder.getBean(UserMapper.class);
    private MenuMapper menuMapper = SpringContextHolder.getBean(MenuMapper.class);
    private NoticeMapper noticeMapper = SpringContextHolder.getBean(NoticeMapper.class);
    private ClassroomMapper classroomMapper = SpringContextHolder.getBean(ClassroomMapper.class);
    private StudentMapper studentMapper = SpringContextHolder.getBean(StudentMapper.class);
    private ClassMapper classMapper = SpringContextHolder.getBean(ClassMapper.class);
    private AdjustStudentMapper adjustStudentMapper = SpringContextHolder.getBean(AdjustStudentMapper.class);
    private ColumnMapper columnMapper = SpringContextHolder.getBean(ColumnMapper.class);

    public static IConstantFactory me() {
        return SpringContextHolder.getBean("constantFactory");
    }

    /**
     * 根据用户id获取用户名称
     *
     * @author stylefeng
     * @Date 2017/5/9 23:41
     */
    @Override
    public String getUserNameById(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            return user.getName();
        } else {
            return "--";
        }
    }

    /**
     * 根据用户id获取用户账号
     *
     * @author stylefeng
     * @date 2017年5月16日21:55:371
     */
    @Override
    public String getUserAccountById(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            return user.getAccount();
        } else {
            return "--";
        }
    }

    /**
     * 通过角色ids获取角色名称
     */
    @Override
    @Cacheable(value = Cache.DICT_CONSTANT, key = "'" + CacheKey.ROLES_NAME + "'+#roleIds")
    public String getRoleName(String roleIds) {
        Integer[] roles = Convert.toIntArray(roleIds);
        StringBuilder sb = new StringBuilder();
        for (int role : roles) {
            Role roleObj = roleMapper.selectById(role);
            if (ToolUtil.isNotEmpty(roleObj) && ToolUtil.isNotEmpty(roleObj.getName())) {
                sb.append(roleObj.getName()).append(",");
            }
        }
        return StrKit.removeSuffix(sb.toString(), ",");
    }

    /**
     * 通过角色id获取角色名称
     */
    @Override
    @Cacheable(value = Cache.DICT_CONSTANT, key = "'" + CacheKey.SINGLE_ROLE_NAME + "'+#roleId")
    public String getSingleRoleName(Integer roleId) {
        if (0 == roleId) {
            return "--";
        }
        Role roleObj = roleMapper.selectById(roleId);
        if (ToolUtil.isNotEmpty(roleObj) && ToolUtil.isNotEmpty(roleObj.getName())) {
            return roleObj.getName();
        }
        return "";
    }

    /**
     * 通过角色id获取角色英文名称
     */
    @Override
    @Cacheable(value = Cache.DICT_CONSTANT, key = "'" + CacheKey.SINGLE_ROLE_TIP + "'+#roleId")
    public String getSingleRoleTip(Integer roleId) {
        if (0 == roleId) {
            return "--";
        }
        Role roleObj = roleMapper.selectById(roleId);
        if (ToolUtil.isNotEmpty(roleObj) && ToolUtil.isNotEmpty(roleObj.getName())) {
            return roleObj.getTips();
        }
        return "";
    }

    /**
     * 获取部门名称
     */
    @Override
    @Cacheable(value = Cache.CONSTANT, key = "'" + CacheKey.DEPT_NAME + "'+#deptId")
    public String getDeptName(Integer deptId) {
        Dept dept = deptMapper.selectById(deptId);
        if (ToolUtil.isNotEmpty(dept) && ToolUtil.isNotEmpty(dept.getFullname())) {
            return dept.getFullname();
        }
        return "";
    }

    /**
     * 获取菜单的名称们(多个)
     */
    @Override
    public String getMenuNames(String menuIds) {
        Integer[] menus = Convert.toIntArray(menuIds);
        StringBuilder sb = new StringBuilder();
        for (int menu : menus) {
            Menu menuObj = menuMapper.selectById(menu);
            if (ToolUtil.isNotEmpty(menuObj) && ToolUtil.isNotEmpty(menuObj.getName())) {
                sb.append(menuObj.getName()).append(",");
            }
        }
        return StrKit.removeSuffix(sb.toString(), ",");
    }

    /**
     * 获取菜单名称
     */
    @Override
    public String getMenuName(Long menuId) {
        if (ToolUtil.isEmpty(menuId)) {
            return "";
        } else {
            Menu menu = menuMapper.selectById(menuId);
            if (menu == null) {
                return "";
            } else {
                return menu.getName();
            }
        }
    }

    /**
     * 获取菜单名称通过编号
     */
    @Override
    public String getMenuNameByCode(String code) {
        if (ToolUtil.isEmpty(code)) {
            return "";
        } else {
            Menu param = new Menu();
            param.setCode(code);
            Menu menu = menuMapper.selectOne(param);
            if (menu == null) {
                return "";
            } else {
                return menu.getName();
            }
        }
    }

    /**
     * 获取字典名称
     */
    @Override
    public String getDictName(Integer dictId) {
        if (ToolUtil.isEmpty(dictId)) {
            return "";
        } else {
            Dict dict = dictMapper.selectById(dictId);
            if (dict == null) {
                return "";
            } else {
                return dict.getName();
            }
        }
    }

    /**
     * 获取字典名称
     * @param dictCode
     * @return
     */
    @Override
    @Cacheable(value = Cache.DICT_CONSTANT, key = "'" + CacheKey.DICT_CODE + "'+#dictCode")
    public String getDictNameByCode(String dictCode) {
        List<Dict> dicts = dictMapper.selectByCode(dictCode);
        return CollectionUtils.isEmpty(dicts)?"":dicts.get(0).getName();
    }

    /**
     * 获取通知标题
     */
    @Override
    public String getNoticeTitle(Integer dictId) {
        if (ToolUtil.isEmpty(dictId)) {
            return "";
        } else {
            Notice notice = noticeMapper.selectById(dictId);
            if (notice == null) {
                return "";
            } else {
                return notice.getTitle();
            }
        }
    }

    /**
     * 根据字典名称和字典中的值获取对应的名称
     */
    @Override
    public String getDictsByName(String name, Integer val) {
        Dict temp = new Dict();
        temp.setName(name);
        Dict dict = dictMapper.selectOne(temp);
        if (dict == null) {
            return "";
        } else {
            Wrapper<Dict> wrapper = new EntityWrapper<>();
            wrapper = wrapper.eq("pid", dict.getId());
            List<Dict> dicts = dictMapper.selectList(wrapper);
            for (Dict item : dicts) {
                if (item.getNum() != null && item.getNum().equals(val)) {
                    return item.getName();
                }
            }
            return "";
        }
    }

    @Override
    @Cacheable(value = Cache.DICT_CONSTANT, key = "'" + CacheKey.DICT_CODE + "'+#code+#val")
    public String getDictsByCode(String code, String val) {
        return getDictsByCode(code, val, "");
    }

    @Override
    @Cacheable(value = Cache.DICT_CONSTANT, key = "'" + CacheKey.DICT_CODE + "'+#code+#val")
    public String getDictsByCode(String code, String val, String defaultValue) {
        Dict temp = new Dict();
        temp.setCode(code);
        Dict dict = dictMapper.selectOne(temp);
        if (dict == null) {
            return null == defaultValue ? "" : defaultValue;
        } else {
            Wrapper<Dict> wrapper = new EntityWrapper<>();
            wrapper = wrapper.eq("pid", dict.getId());
            List<Dict> dicts = dictMapper.selectList(wrapper);
            for (Dict item : dicts) {
                if (item.getCode() != null && item.getCode().equals(val)) {
                    return item.getName();
                }
            }
            return null == defaultValue ? "" : defaultValue;
        }
    }

    /**
     * 获取老师类型名称
     * @param teacherType
     * @return
     */
    @Override
    @Cacheable(value = Cache.DICT_CONSTANT, key = "'" + CacheKey.TEACHER_TYPE + "'+#teacherType")
    public String getTeacherTypeName(Integer teacherType) {
        return getDictsByName("教师类型", teacherType);
    }

    /**
     * 获取授课年级名称
     * @param grade
     * @return
     */
    @Override
    @Cacheable(value = Cache.DICT_CONSTANT, key = "'" + CacheKey.DICT_CODE + "'+#grade")
    public String getGradeName(Integer grade) {
        return getDictsByName("授课年级", grade);
    }

    /**
     * 获取性别名称
     */
    @Override
    @Cacheable(value = Cache.DICT_CONSTANT, key = "'" + CacheKey.GENDER_TYPE + "'+#sex")
    public String getSexName(Integer sex) {
        return getDictsByName("性别", sex);
    }

    /**
     * 获取用户登录状态
     */
    @Override
    public String getStatusName(Integer status) {
        return ManagerStatus.valueOf(status);
    }

    /**
     * 获取菜单状态
     */
    @Override
    @Cacheable(value = Cache.DICT_CONSTANT, key = "'" + CacheKey.MENU_STATUS + "'+#status")
    public String getMenuStatusName(Integer status) {
        return MenuStatus.valueOf(status);
    }

    /**
     * 查询字典
     */
    @Override
    @Cacheable(value = Cache.DICT_CONSTANT, key = "'" + CacheKey.DICT_LIST + "'+#id")
    public List<Dict> findInDict(Integer id) {
        if (ToolUtil.isEmpty(id)) {
            return null;
        } else {
            EntityWrapper<Dict> wrapper = new EntityWrapper<>();
            List<Dict> dicts = dictMapper.selectList(wrapper.eq("pid", id));
            if (dicts == null || dicts.size() == 0) {
                return null;
            } else {
                return dicts;
            }
        }
    }

    /**
     * 获取被缓存的对象(用户删除业务)
     */
    @Override
    public String getCacheObject(String para) {
        return LogObjectHolder.me().get().toString();
    }

    /**
     * 获取子部门id
     */
    @Override
    public List<Integer> getSubDeptId(Integer deptid) {
        Wrapper<Dept> wrapper = new EntityWrapper<>();
        wrapper = wrapper.like("pids", "%[" + deptid + "]%");
        List<Dept> depts = this.deptMapper.selectList(wrapper);

        ArrayList<Integer> deptids = new ArrayList<>();

        if(depts != null && depts.size() > 0){
            for (Dept dept : depts) {
                deptids.add(dept.getId());
            }
        }

        return deptids;
    }

    /**
     * 获取所有父部门id
     */
    @Override
    public List<Integer> getParentDeptIds(Integer deptid) {
        Dept dept = deptMapper.selectById(deptid);
        String pids = dept.getPids();
        String[] split = pids.split(",");
        ArrayList<Integer> parentDeptIds = new ArrayList<>();
        for (String s : split) {
            parentDeptIds.add(Integer.valueOf(StrKit.removeSuffix(StrKit.removePrefix(s, "["), "]")));
        }
        return parentDeptIds;
    }

    @Override
    public String getPayStatusName(Integer payStatus) {
        return getDictsByName("订单状态", payStatus);
    }

    @Override
    public String getPayMethodName(Integer payMethod) {
        return getDictsByName("订单状态", payMethod);
    }

    @Override
    public String getPayResultName(Integer payResult) {
        return getDictsByName("订单状态", payResult);
    }

    @Override
    public String getSchoolAdressName(Integer classCode) {
        // 当前只有一个校区
        if(ObjectUtils.isEmpty(classCode)){
            return getDictsByName("校区", 1);
        }
        return getDictsByName("校区", classCode);
    }

    @Override
    public String getStudyTimeTypeName(Integer studyTimeType) {
        return getDictsByName("开课时间类型", studyTimeType);
    }

    @Override
    public String getContentTypeName(Integer type) {
        return getDictsByName("资讯类型", type);
    }

    @Override
    public String getQuestionTypeName(Integer type) {
        return getDictsByName("试题类型", type);
    }

    @Override
    @Cacheable(value = Cache.DICT_CONSTANT, key = "'" + CacheKey.SUBJECT_TYPE + "'+#subject")
    public String getsubjectName(Integer subject) {
        return getDictsByCode("subject_type", String.valueOf(subject));
//        return getDictsByName("学科类型", subject);
    }

    @Override
    @Cacheable(value = Cache.DICT_CONSTANT, key = "'" + CacheKey.ROOM_TYPE + "'+#type")
    public String getClassRoomTypeName(Integer type) {
        return getDictsByCode("classroom_type", String.valueOf(type));
    }

    @Override
    public String getStudentName(String studentCode) {
        Student student = studentMapper.selectOne(new Student() {{
            setCode(studentCode);
        }});
        return student == null?null:student.getName();
    }

    @Override
    public String getClassName(String classCode) {
        Class aClass = classMapper.selectOne(new Class() {{
            setCode(classCode);
        }});
        return aClass == null ?null:aClass.getName();
    }

    @Override
    @Cacheable(value = Cache.DICT_CONSTANT, key = "'" + CacheKey.ADJUST_TYPE + "'+#type")
    public String getAdjustTypeName(Integer type) {
        return getDictsByName("调课类型", type);
    }

    @Override
    @Cacheable(value = Cache.DICT_CONSTANT, key = "'" + CacheKey.CLASS_ABILITY + "'+#ability")
    public String getAbilityName(Integer ability) {
        return getDictsByCode("ability", String.valueOf(ability));
    }

    @Override
    @Cacheable(value = Cache.DICT_CONSTANT, key = "'" + CacheKey.CLASS_CYCLE + "'+#cycle")
    public String getCycleName(Integer cycle) {
        return getDictsByName("学期", cycle);
    }

    @Override
    public String getColumnName(String pcodes) {
        Column column = columnMapper.selectOne(new Column() {{
            setCode(pcodes);
        }});
        return column==null?column.getName():null;
    }

    @Override
    @Cacheable(value = Cache.DICT_CONSTANT, key = "'" + CacheKey.COLUMN_TYPE + "'+#type")
    public String getColumnTypeName(Integer type) {
        return getDictsByName("栏目行为类型", type);
    }

    @Override
    @Cacheable(value = Cache.DICT_CONSTANT, key = "'" + CacheKey.GENERIC_STATE + "'+#status")
    public String getMemberStatusName(Integer status) {
        return getDictsByCode("account_state", String.valueOf(status));
    }

    @Override
    @Cacheable(value = Cache.DICT_CONSTANT, key = "'" + CacheKey.GENERIC_STATE + "'+#status")
    public Object getGenericStateName(Integer status) {
        return getDictsByCode("generic_state", String.valueOf(status));
    }

    @Override
    @Cacheable(value = Cache.DICT_CONSTANT, key = "'" + CacheKey.COURSE_METHOD + "'+#method")
    public Object getCourseMethodname(Integer method) {
        return getDictsByCode("course_method", String.valueOf(method));
    }

    @Override
    public String fenToYuan(String amount) {
        return new BigDecimal(amount).divide(MONEY_TRANSFORM).setScale(2).toString();
    }

    @Override
    @Cacheable(value = Cache.DICT_CONSTANT, key = "'" + CacheKey.DICT_MAP + "'+#dictCode")
    public Map<String, Object> getdictsMap(String dictCode) {
        Dict temp = new Dict();
        temp.setCode(dictCode);
        Dict dict = dictMapper.selectOne(temp);

        Map<String, Object> resultMap = new HashMap<String, Object>();
        if (dict == null) {
            return new HashMap<String, Object>();
        } else {
            Wrapper<Dict> wrapper = new EntityWrapper<>();
            wrapper = wrapper.eq("pid", dict.getId());
            List<Dict> dicts = dictMapper.selectList(wrapper);
            for (Dict item : dicts) {
                resultMap.put(item.getName(), item.getCode());
            }
        }
        return resultMap;
    }
}
