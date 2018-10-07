package com.stylefeng.guns.modular.studentMGR.controller;

import com.stylefeng.guns.core.base.controller.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;
import com.stylefeng.guns.core.log.LogObjectHolder;
import org.springframework.web.bind.annotation.RequestParam;
import com.stylefeng.guns.modular.system.model.Student;
import com.stylefeng.guns.modular.studentMGR.service.IStudentService;

/**
 * 学生管理控制器
 *
 * @author fengshuonan
 * @Date 2018-10-07 10:12:17
 */
@Controller
@RequestMapping("/student")
public class StudentController extends BaseController {

    private String PREFIX = "/studentMGR/student/";

    @Autowired
    private IStudentService studentService;

    /**
     * 跳转到学生管理首页
     */
    @RequestMapping("")
    public String index() {
        return PREFIX + "student.html";
    }

    /**
     * 跳转到添加学生管理
     */
    @RequestMapping("/student_add")
    public String studentAdd() {
        return PREFIX + "student_add.html";
    }

    /**
     * 跳转到修改学生管理
     */
    @RequestMapping("/student_update/{studentId}")
    public String studentUpdate(@PathVariable Integer studentId, Model model) {
        Student student = studentService.selectById(studentId);
        model.addAttribute("item",student);
        LogObjectHolder.me().set(student);
        return PREFIX + "student_edit.html";
    }

    /**
     * 获取学生管理列表
     */
    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list(String condition) {
        return studentService.selectList(null);
    }

    /**
     * 新增学生管理
     */
    @RequestMapping(value = "/add")
    @ResponseBody
    public Object add(Student student) {
        studentService.insert(student);
        return SUCCESS_TIP;
    }

    /**
     * 删除学生管理
     */
    @RequestMapping(value = "/delete")
    @ResponseBody
    public Object delete(@RequestParam Integer studentId) {
        studentService.deleteById(studentId);
        return SUCCESS_TIP;
    }

    /**
     * 修改学生管理
     */
    @RequestMapping(value = "/update")
    @ResponseBody
    public Object update(Student student) {
        studentService.updateById(student);
        return SUCCESS_TIP;
    }

    /**
     * 学生管理详情
     */
    @RequestMapping(value = "/detail/{studentId}")
    @ResponseBody
    public Object detail(@PathVariable("studentId") Integer studentId) {
        return studentService.selectById(studentId);
    }
}
