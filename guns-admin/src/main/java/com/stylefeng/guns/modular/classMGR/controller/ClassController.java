package com.stylefeng.guns.modular.classMGR.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.common.constant.factory.PageFactory;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.core.base.controller.BaseController;
import com.stylefeng.guns.log.LogObjectHolder;
import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.classMGR.service.ICourseOutlineService;
import com.stylefeng.guns.modular.classMGR.service.ICourseService;
import com.stylefeng.guns.modular.classMGR.warpper.ClassWrapper;
import com.stylefeng.guns.modular.classRoomMGR.service.IClassroomService;
import com.stylefeng.guns.modular.courseMGR.warpper.CourseWrapper;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.modular.system.model.Classroom;
import com.stylefeng.guns.modular.system.model.CourseOutline;
import com.stylefeng.guns.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * 课程管理控制器
 *
 * @author fengshuonan
 * @Date 2018-10-20 11:35:19
 */
@Controller
@RequestMapping("/class")
public class ClassController extends BaseController {

    private String PREFIX = "/classMGR/class/";

    @Autowired
    private IClassService classService;
    @Autowired
    private IClassroomService classroomService;
    @Autowired
    private ICourseOutlineService courseOutlineService;
    @Autowired
    private ICourseService courseService;

    /**
     * 跳转到课程管理首页
     */
    @RequestMapping("")
    public String index() {
        return PREFIX + "class.html";
    }

    /**
     * 跳转到添加课程管理
     */
    @RequestMapping("/class_add")
    public String classAdd() {
        return PREFIX + "class_add.html";
    }

    /**
     * 跳转到添加课程大纲管理
     */
    @RequestMapping("/class_add_kcdg/{classCode}/{courseCode}")
    public String classAddKCDG(@PathVariable String classCode,@PathVariable String courseCode, Model model) {

        List<CourseOutline> courseOutlines = courseOutlineService.selectList(new EntityWrapper<CourseOutline>(){{
            eq("class_code", classCode);
            eq("code", courseCode);
            orderAsc(new ArrayList<String>(1){{add("sort");}});
        }});
        model.addAttribute("item",new HashMap<String,String>(){{
            put("classCode",classCode);
            put("courseCode",courseCode);
        }});
        model.addAttribute("courseOutlines",courseOutlines);
        LogObjectHolder.me().set(classCode);
        return PREFIX + "class_add_kcdg.html";
    }

    /**
     * 跳转到修改课程管理
     */
    @RequestMapping("/class_update/{classCode}")
    public String classUpdate(@PathVariable("classCode") String code, Model model) {
        Map<String, Object> classInstanceMap = classService.getMap(code);
        new ClassWrapper(classInstanceMap).warp();

        Map<String, Object> courseInstanceMap = courseService.getMap((String) classInstanceMap.get("courseCode"));
        new CourseWrapper(courseInstanceMap).warp();


        model.addAttribute("classItem",classInstanceMap);
        model.addAttribute("courseItem",courseInstanceMap);

        LogObjectHolder.me().set(classInstanceMap);
        return PREFIX + "class_edit.html";
    }

    /**
     * 获取课程管理列表
     */
    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list(String condition) {
        //分页查詢
        Page<Class> page = new PageFactory<Class>().defaultPage();
        Page<Map<String, Object>> pageMap = classService.selectMapsPage(page, new EntityWrapper<Class>() {
            {
                //name条件分页
                if (StringUtils.isNotEmpty(condition)) {
                    like("name", condition);
                }

                eq("status", GenericState.Valid.code);

                orderBy("id", false);
            }
        });
        //包装数据
        new ClassWrapper(pageMap.getRecords()).warp();
        return super.packForBT(pageMap);
    }

    /**
     * 获取教室管理列表
     */
    @RequestMapping(value = "/listAll")
    @ResponseBody
    public Object listRoom(String condition) {

        Wrapper<Class> classQueryWrapper = new EntityWrapper<Class>();
        classQueryWrapper.eq("status", GenericState.Valid.code);

        if (null != condition && condition.length() > 0){
            classQueryWrapper.like("name", condition);
        }
        classQueryWrapper.orderBy("id", false);

        return classService.selectList(classQueryWrapper);
    }

    /**
     * 新增课程管理
     */
    @RequestMapping(value = "/add")
    @ResponseBody
    public Object add(Class classInstance) {

        Date beginDate = DateUtil.parse(DateUtil.getDays() + classInstance.getBeginTime(), "yyyyMMddHHmm");
        Date endDate = DateUtil.parse(DateUtil.getDays() + classInstance.getEndTime(), "yyyyMMddHHmm");

        classInstance.setDuration(DateUtil.getMinuteSub(beginDate, endDate));
        // 设置班级容量
        Classroom classroomEntity = classroomService.get(classInstance.getClassRoomCode());
        classInstance.setQuato(classroomEntity.getMaxCount());

        classService.createClass(classInstance);
        return SUCCESS_TIP;
    }

    /**
     * 删除课程管理
     */
    @RequestMapping(value = "/delete")
    @ResponseBody
    public Object delete(@RequestParam String classCode) {
        if (null == classCode)
            return SUCCESS_TIP;

        classService.deleteClass(classCode);
        return SUCCESS_TIP;
    }

    /**
     * 修改课程管理
     */
    @RequestMapping(value = "/update")
    @ResponseBody
    public Object update(Class classInstance) {
        classInstance.setPrice(classInstance.getPrice() * 100);

        Date beginDate = DateUtil.parse(DateUtil.getDays() + classInstance.getBeginTime(), "yyyyMMddHHmm");
        Date endDate = DateUtil.parse(DateUtil.getDays() + classInstance.getEndTime(), "yyyyMMddHHmm");

        classInstance.setDuration(DateUtil.getMinuteSub(beginDate, endDate));
        // 设置班级容量
        Class currClass = classService.get(classInstance.getCode());
        int currQuato = null == currClass ? 0 : currClass.getQuato();
        Classroom currClassroom = classroomService.get(currClass.getClassRoomCode());
        int maxQuato = currClassroom.getMaxCount();
        // 已报名人数
        int orderQuato = maxQuato - currQuato;

        Classroom classroomEntity = classroomService.get(classInstance.getClassRoomCode());
        // 设置当前班级的剩余报名人数
        classInstance.setQuato(classroomEntity.getMaxCount() - orderQuato);

        classService.updateClass(classInstance);
        return SUCCESS_TIP;
    }

    /**
     * 课程管理详情
     */
    @RequestMapping(value = "/detail/{classId}")
    @ResponseBody
    public Object detail(@PathVariable("classId") Integer classId) {
        return classService.selectById(classId);
    }
}
