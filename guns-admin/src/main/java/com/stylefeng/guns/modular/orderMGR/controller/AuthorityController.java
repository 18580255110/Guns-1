package com.stylefeng.guns.modular.orderMGR.controller;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/9/10 16:31
 * @Version 1.0
 */

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.base.controller.BaseController;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.modular.classMGR.service.IClassAuthorityService;
import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.classMGR.warpper.ClassWrapper;
import com.stylefeng.guns.modular.education.service.IStudentClassService;
import com.stylefeng.guns.modular.orderMGR.OrderAddList;
import com.stylefeng.guns.modular.studentMGR.service.IStudentService;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.modular.system.model.Class;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/order/authority")
public class AuthorityController extends BaseController {

    private String PREFIX = "/orderMGR/order/";

    @Autowired
    private IClassService classService;

    @Autowired
    private IStudentService studentService;

    @Autowired
    private IClassAuthorityService classAuthorityService;
    /**
     * 跳转到权限设置首页
     */
    @RequestMapping("")
    public String index() {
        return PREFIX + "authority.html";
    }

    @RequestMapping("/wizard/{classCode}")
    public String openSignDlg(@PathVariable("classCode") String code, Model model){

        if (null == code)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"班级信息"});

        Class classInfo = classService.get(code);

        if (null == classInfo)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"班级信息"});

        Map<String, Object> resultMap = toMap(classInfo);

        new ClassWrapper(resultMap).warp();
        model.addAttribute("classInfo", resultMap);
        return PREFIX + "auth_wizard.html";
    }


    /**
     * 授权
     */
    @RequestMapping(value = "/doAuth")
    @ResponseBody
    public Object doAuth( @RequestBody AuthRequest request) {

        Class classInfo = classService.get(request.getClassInfo().getCode());

        if (null == classInfo)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"班级信息"});

        Student student = request.getStudent();
        Student currStudent = null;
        currStudent = studentService.get(student.getCode());
        if (null == currStudent)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"学员信息"});

        classAuthorityService.doAuthenticate(currStudent, classInfo);

        return SUCCESS_TIP;
    }

    private Map<String, Object> toMap(Class classInfo) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(classInfo.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();

                // 过滤class属性
                if (!key.equals("class")) {
                    // 得到property对应的getter方法
                    Method getter = property.getReadMethod();
                    Object value = getter.invoke(classInfo);

                    map.put(key, value);
                }

            }
        } catch (Exception e) {
            System.out.println("transBean 2 Map Error " + e);
        }

        return map;
    }
}
