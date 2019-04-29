package com.stylefeng.guns.courseMGR;

import com.stylefeng.guns.base.BaseJunit;
import com.stylefeng.guns.modular.classMGR.service.ICourseOutlineService;
import com.stylefeng.guns.modular.classMGR.service.ICourseService;
import com.stylefeng.guns.modular.system.model.Course;
import com.stylefeng.guns.modular.system.model.CourseOutline;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: simple.song
 * Date: 2019/4/29 Time: 13:20
 */
public class CouserService extends BaseJunit {

    @Autowired
    private ICourseOutlineService courseOutlineService;

    @Test
    public void testInserBatch(){
        Course course =  new Course();
        course.setCode("111111");
        List<CourseOutline> outlineList = new ArrayList<>();
        CourseOutline courseOutline = new CourseOutline();
        courseOutline.setCode("2222");
        courseOutline.setOutline("test");
        courseOutline.setCourseCode("testCourse");
        courseOutline.setSort(1);
        outlineList.add(courseOutline);
        CourseOutline courseOutline2 = new CourseOutline();
        courseOutline2.setCode("2222");
        courseOutline2.setOutline("test");
        courseOutline2.setCourseCode("testCourse");
        courseOutline2.setSort(1);
        outlineList.add(courseOutline2);
        courseOutlineService.addCourseOutline(course,outlineList);
    }
}