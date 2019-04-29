package com.stylefeng.guns.modular.classMGR.service.impl;


import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.modular.classMGR.service.ICourseOutlineService;
import com.stylefeng.guns.modular.system.dao.CourseOutlineMapper;
import com.stylefeng.guns.modular.system.model.Course;
import com.stylefeng.guns.modular.system.model.CourseOutline;
import com.stylefeng.guns.util.CodeKit;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 课程大纲 服务实现类
 * </p>
 *
 * @author simple.song
 * @since 2018-11-17
 */
@Service("iCourseOutlineService")
public class CourseOutlineServiceImpl extends ServiceImpl<CourseOutlineMapper, CourseOutline> implements ICourseOutlineService {


    @Resource
    private CourseOutlineMapper courseOutlineMapper;

    @Override
    public void addCourseOutline(Course course, List<CourseOutline> outlineList) {
        if (null == course)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS);

        /* 先删除 */
        courseOutlineMapper.deleteByMap(new HashMap<String, Object>(8){{
            put("course_code",course.getCode());
        }});
        /* 后批量插入 */
        List<CourseOutline> courseOutlineList = new ArrayList<>();
        for(CourseOutline courseOutline : outlineList){
            CourseOutline newCourseOutline = new CourseOutline();
            newCourseOutline.setCode(CodeKit.generateOutline());
            newCourseOutline.setCourseCode(course.getCode());
            newCourseOutline.setOutline(courseOutline.getOutline());
            newCourseOutline.setDescription(courseOutline.getDescription());
            newCourseOutline.setSort(courseOutline.getSort());
            newCourseOutline.setStatus(GenericState.Valid.code);
            courseOutlineList.add(newCourseOutline);
        }
        insertBatch(courseOutlineList);
    }

    @Override
    public void addCourseOutline(Course course) {
        if (null == course)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"课程"});

        List<CourseOutline> courseOutlineList = new ArrayList<CourseOutline>();
        for(int idx = 0; idx < course.getPeriod(); idx++){
            CourseOutline outline = new CourseOutline();
            outline.setSort(idx + 1);
            outline.setCourseCode(course.getCode());
            outline.setOutline(getDefaultOutline(idx));
            outline.setDescription(getDefaultOutline(idx));
            courseOutlineList.add(outline);
        }

        addCourseOutline(course, courseOutlineList);
    }

    @Override
    public CourseOutline get(String code) {
        if (null == code)
            return null;

        return selectOne(new EntityWrapper<CourseOutline>().eq("code", code));
    }

    @Override
    public String getDefaultOutline(int index) {
        if (index < 0 || index >= 100)
            throw new ServiceException(MessageConstant.MessageCode.SYS_DATA_OVERTOP, new String[]{"最大只支持九十九个课时"});

        return DEFAULT_OUTLINES[index];
    }

    @Override
    public void refreshCourseOutline(Course course, int newPeriod) {
        if (null == course)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"课程编码"});

        Wrapper<CourseOutline> queryWrapper = new EntityWrapper<CourseOutline>();
        queryWrapper.eq("course_code", course.getCode());
        queryWrapper.eq("status", GenericState.Valid.code);

        List<CourseOutline> courseOutlineList = selectList(queryWrapper);

        if (courseOutlineList.isEmpty())
            addCourseOutline(course);
        else{
            int existSize = courseOutlineList.size();
            int idx = 0;
            List<CourseOutline> courseOutlineBatch = new ArrayList<>();
            for (; idx < newPeriod; idx++){
                if (existSize > idx)
                    continue;

                // 创建新增的课程大纲
                CourseOutline outline = new CourseOutline();
                outline.setSort(idx + 1);
                outline.setCode(CodeKit.generateOutline());
                outline.setCourseCode(course.getCode());
                outline.setOutline(getDefaultOutline(idx));
                outline.setDescription(getDefaultOutline(idx));
                outline.setStatus(GenericState.Valid.code);
                courseOutlineBatch.add(outline);
            }
            insertBatch(courseOutlineBatch);

            if (idx < existSize){
                // 有多余的大纲，需要删除
                Wrapper<CourseOutline> deleteWrapper = new EntityWrapper<CourseOutline>();
                deleteWrapper.eq("course_code", course.getCode());
                deleteWrapper.gt("sort", idx);

                delete(deleteWrapper);
            }
        }
    }

}
