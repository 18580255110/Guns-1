package com.stylefeng.guns.modular.system.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.stylefeng.guns.modular.system.model.StudentPrivilege;

import java.util.List;
import java.util.Map;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/10/8 09:21
 * @Version 1.0
 */
public interface StudentPrivilegeMapper extends BaseMapper<StudentPrivilege> {
    /**
     *
     * @param queryParams
     * @return
     */
    List<Map<String, Object>> selectStudentPrivilegeStatistic(Map<String, Object> queryParams);
}
