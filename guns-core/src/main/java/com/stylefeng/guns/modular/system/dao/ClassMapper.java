package com.stylefeng.guns.modular.system.dao;

import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.modular.system.model.Class;
import com.baomidou.mybatisplus.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 班级 Mapper 接口
 * </p>
 *
 * @author simple.song
 * @since 2018-10-20
 */
public interface ClassMapper extends BaseMapper<Class> {
    /**
     * 根据查询条件查询
     *
     * @param queryParams
     * @return
     */
    List<Class> queryForList(Map<String, Object> queryParams);

    /**
     * 根据条件查询
     *
     * @param queryParams
     * @return
     */
    List<Map<String, Object>> selectPageList(Page<Map<String, Object>> page, Map<String, Object> queryParams);

    /**
     *
     * @param code
     * @return
     */
    Integer queryOrderingSignCount(String code);
}
