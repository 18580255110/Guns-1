package com.stylefeng.guns.modular.classAuthorityMGR.controller;

import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.base.controller.BaseController;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.log.LogObjectHolder;
import com.stylefeng.guns.modular.classAuthorityMGR.service.IClassAuthorityService;
import com.stylefeng.guns.modular.memberMGR.service.IMemberService;
import com.stylefeng.guns.modular.orderMGR.OrderAddList;
import com.stylefeng.guns.modular.studentMGR.service.IStudentService;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.modular.system.model.Class;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 控制器
 *
 * @author fengshuonan
 * @Date 2019-07-30 12:58:24
 */
@Controller
@RequestMapping("/classAuthority")
public class ClassAuthorityController extends BaseController {

    private String PREFIX = "/classAuthorityMGR/";

    @Autowired
    private IClassAuthorityService classAuthorityService;
    @Autowired
    private IMemberService memberService;
    @Autowired
    private IStudentService studentService;


    /**
     * 跳转到首页
     */
    @RequestMapping("")
    public String index() {
        return PREFIX + "classAuthority.html";
    }

    /**
     * 跳转到添加课程
     */
    @RequestMapping("/classAuthority_add")
    public String classAuthorityAdd() {
//        return PREFIX + "classAuthority_add.html";
        return PREFIX + "sign_wizard.html";
    }
    /**
     * 跳转到添加关联学生
     */
    @RequestMapping("/classAuthority_add_student")
    public String classAuthorityAddStudent() {
        return PREFIX + "classAuthority_add_student.html";
    }


    /**
     * 获取列表
     */
    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list(String condition) {
        return classAuthorityService.selectList(null);
    }

    /**
     * 新增
     */
    @RequestMapping(value = "/add")
    @ResponseBody
    public Object add( @RequestBody ClassAuthorityRequest request) {

        Member member = request.getMember();
        Map<String, Object> memberRegistMap = new HashMap<>();
        memberRegistMap.put("number", member.getMobileNumber());
        memberRegistMap.put("name", member.getName());

        Member currMember = null;
        try {
            currMember = memberService.createMember(member.getMobileNumber(), memberRegistMap);
        }catch(ServiceException sere){
            if (!MessageConstant.MessageCode.SYS_SUBJECT_DUPLICATE.equals(sere.getMessageCode()))
                throw sere;
            else
                currMember = memberService.getByMobile(member.getMobileNumber());
        }

        if (null == currMember)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"用户信息"});

        Student student = request.getStudent();
        Student currStudent = null;
        currStudent = studentService.get(student.getCode());
        if (null == currStudent)
            currStudent = studentService.addStudent(currMember.getUserName(), student);

        if (null == currMember)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"学员信息"});

        List<Class> classInfoList = request.getClassInfoList();
        if (null == classInfoList || CollectionUtils.isEmpty(classInfoList))
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"班级信息"});

        List<ClassAuthority> insertList = new ArrayList<>();
        for (Class aClass : classInfoList) {
            ClassAuthority classAuthority = new ClassAuthority();
            classAuthority.setClassCode(aClass.getCode());
            classAuthority.setClassName(aClass.getName());
            classAuthority.setStudentCode(currStudent.getCode());
            classAuthority.setStudentName(currStudent.getName());
            insertList.add(classAuthority);
        }
        classAuthorityService.insertBatch(insertList);


        return SUCCESS_TIP;
    }

    /**
     * 删除
     */
    @RequestMapping(value = "/delete")
    @ResponseBody
    public Object delete(@RequestParam Integer classAuthorityId) {
        classAuthorityService.deleteById(classAuthorityId);
        return SUCCESS_TIP;
    }


}
