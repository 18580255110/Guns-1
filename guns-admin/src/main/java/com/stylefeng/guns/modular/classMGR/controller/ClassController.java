package com.stylefeng.guns.modular.classMGR.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.common.constant.factory.PageFactory;
import com.stylefeng.guns.core.base.controller.BaseController;
import com.stylefeng.guns.log.LogObjectHolder;
import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.classMGR.warpper.ClassWrapper;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.modular.system.model.Student;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

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
     * 跳转到修改课程管理
     */
    @RequestMapping("/class_update/{classId}")
    public String classUpdate(@PathVariable Integer classId, Model model) {
        Class classInstance = classService.selectById(classId);
        model.addAttribute("item",classInstance);
        LogObjectHolder.me().set(classInstance);
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
            }
        });
        //包装数据
        new ClassWrapper(pageMap.getRecords()).warp();
        return super.packForBT(pageMap);
    }

    /**
     * 新增课程管理
     */
    @RequestMapping(value = "/add")
    @ResponseBody
    public Object add(Class classInstance) {
        classService.insert(classInstance);
        return SUCCESS_TIP;
    }

    /**
     * 删除课程管理
     */
    @RequestMapping(value = "/delete")
    @ResponseBody
    public Object delete(@RequestParam Integer classId) {
        classService.deleteById(classId);
        return SUCCESS_TIP;
    }

    /**
     * 修改课程管理
     */
    @RequestMapping(value = "/update")
    @ResponseBody
    public Object update(Class classInstance) {
        classService.updateById(classInstance);
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
