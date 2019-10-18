package com.stylefeng.guns.modular.studentMGR.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.common.constant.factory.PageFactory;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.base.controller.BaseController;
import com.stylefeng.guns.core.base.tips.ErrorTip;
import com.stylefeng.guns.core.base.tips.SuccessTip;
import com.stylefeng.guns.core.base.tips.Tip;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.log.LogObjectHolder;
import com.stylefeng.guns.modular.education.service.IStudentPrivilegeService;
import com.stylefeng.guns.modular.memberMGR.service.IMemberService;
import com.stylefeng.guns.modular.studentMGR.service.IStudentService;
import com.stylefeng.guns.modular.studentMGR.warpper.StudentWrapper;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.modular.system.service.IAttachmentService;
import com.stylefeng.guns.util.CodeKit;
import com.stylefeng.guns.util.PathUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 学生管理控制器
 *
 * @author fengshuonan
 * @Date 2018-10-07 10:12:17
 */
@Controller
@RequestMapping("/student")
public class StudentController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(StudentController.class);

    private String PREFIX = "/studentMGR/student/";

    @Autowired
    private IStudentService studentService;
    @Autowired
    private IMemberService memberService;

    @Autowired
    private IAttachmentService attachmentService;

    @Autowired
    private IStudentPrivilegeService studentPrivilegeService;

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
        Map<String, Object> map = studentService.getMap(studentId);

        if (map.isEmpty())
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"学员信息"});

        String studentCode = (String) map.get("code");
        List<Attachment> avatarList = attachmentService.listAttachment(Student.class.getSimpleName(), studentCode);
        if (null != avatarList && avatarList.size() > 0){
            map.put("avatar",String.valueOf(avatarList.get(0).getId()));
        }

        Member member = memberService.selectOne(new EntityWrapper<Member>().eq("user_name", map.get("userName")));
        if( member != null){
            map.put("parentPhone",member.getMobileNumber());
        }
        model.addAttribute("item", map);

        // 学员报名权限
        List<StudentPrivilege> privilegeList = studentPrivilegeService.selectList(new EntityWrapper<StudentPrivilege>(){
            {
                eq("student_code", studentCode);
                eq("status", GenericState.Valid.code);
                eq("type", 1);
            }
        });
        model.addAttribute("privilegeList", privilegeList);

        LogObjectHolder.me().set(map);
        return PREFIX + "student_edit.html";
    }

    /**
     * 获取学生管理列表
     */
    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list(@RequestParam Map<String, String> queryParams) {

        //分页查詢
        Page<Student> page = new PageFactory<Student>().defaultPage();
        Page<Map<String, Object>> pageMap = studentService.selectMapsPage(page, new EntityWrapper<Student>() {
            {
                //condition条件分页
                if (queryParams.containsKey("condition") && StringUtils.isNotEmpty(queryParams.get("condition"))) {
                    like("name", queryParams.get("condition"));
                    or();
                    eq("code", queryParams.get("condition"));
                }

                if (StringUtils.isNotEmpty(queryParams.get("status"))){
                    try{
                        int status = Integer.parseInt(queryParams.get("status"));
                        eq("status", status);
                    }catch(Exception e){}
                }
            }
        });
        //包装数据
        List<Map<String, Object>> stuRecords = pageMap.getRecords();
        if(!stuRecords.isEmpty()){
            for (Map<String, Object> stuRecord : stuRecords) {
                Member member = memberService.selectOne(new EntityWrapper<Member>().eq("user_name", stuRecord.get("userName")));
                if(member != null){
                    stuRecord.put("parentPhone",member.getMobileNumber());
                }
            }
        }
        new StudentWrapper(stuRecords).warp();
        return super.packForBT(pageMap);
    }

    /**
     * 新增学生管理
     */
    @RequestMapping(value = "/add")
    @ResponseBody
    public Object add(Student student,String parentPhone) {
        student.setCode(CodeKit.generateStudent());
        studentService.insert(student);
        if(StringUtils.isNotEmpty(parentPhone)){
            Member member = memberService.selectOne(new EntityWrapper<Member>() {{
                setAttr("user_name", parentPhone);
            }});
            member.setMobileNumber(parentPhone);
            memberService.updateById(member);
        }

        return SUCCESS_TIP;
    }

    /**
     * 停用学生
     */
    @RequestMapping(value = "/pause")
    @ResponseBody
    public Object pause(@RequestParam String code) {
        studentService.doPause(code);
        return SUCCESS_TIP;
    }

    /**
     * 恢复学生
     */
    @RequestMapping(value = "/resume")
    @ResponseBody
    public Object resume(@RequestParam String code) {
        studentService.doResume(code);
        return SUCCESS_TIP;
    }

    /**
     * 修改学生管理
     */
    @RequestMapping(value = "/update")
    @ResponseBody
    public Object update(Student student, String masterName, String masterCode, String parentPhone, String privilegeItems) {

        Attachment icon = null;
        List<Attachment> attachmentList = attachmentService.listAttachment(masterName, masterCode);
        if (null != attachmentList && attachmentList.size() > 0){
            icon = attachmentList.get(0);
            student.setAvatar(PathUtil.generate(iconVisitURL, String.valueOf(icon.getId())));
        }

        studentService.updateById(student);
        if(StringUtils.isNotEmpty(parentPhone)){
            Member member = memberService.selectOne(new EntityWrapper<Member>() {{
                eq("user_name", student.getUserName());
            }});
            if( member != null ){
                member.setMobileNumber(parentPhone);
                memberService.updateById(member);
            }
        }
        // 更新ICON资源
        if (null != icon && null != icon.getId())
            try {
                icon.setMasterName(Student.class.getSimpleName());
                icon.setMasterCode(student.getCode());

                attachmentService.updateAndRemoveOther(icon);
            }catch(Exception e){
                log.warn("更新图标失败");
                log.warn("更新图标失败");
            }

        List<StudentPrivilege> studentPrivilegeList = null;
        try {
            studentPrivilegeList = JSON.parseArray(privilegeItems, StudentPrivilege.class);
        }catch(Exception e){}
        List<StudentPrivilege> existPrivilegeList = studentPrivilegeService.selectList(new EntityWrapper<StudentPrivilege>(){
            {
                eq("student_code", student.getCode());
                eq("status", GenericState.Valid.code);
                eq("type", 1);
            }
        });
        if (existPrivilegeList.isEmpty()){
            for(StudentPrivilege studentPrivilege: studentPrivilegeList){
                studentPrivilege.setStudentName(student.getName());
                studentPrivilege.setStatus(GenericState.Valid.code);
                studentPrivilege.setType(1);
                studentPrivilege.setId(null);
                studentPrivilegeService.grantSignPrivileges(studentPrivilege);
            }
        }else if (null != studentPrivilegeList && !(studentPrivilegeList.isEmpty())){
            Iterator<StudentPrivilege> existIterator = existPrivilegeList.iterator();
            while(existIterator.hasNext()){
                StudentPrivilege studentPrivilege = existIterator.next();
                studentPrivilegeService.deleteById(studentPrivilege.getId());
            }

            for(StudentPrivilege studentPrivilege : studentPrivilegeList){
                studentPrivilege.setStudentName(student.getName());
                studentPrivilege.setStatus(GenericState.Valid.code);
                studentPrivilege.setType(1);
                studentPrivilege.setId(null);

                studentPrivilegeService.grantSignPrivileges(studentPrivilege);
            }
        }
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

    /**
     * 学生管理详情
     */
    @RequestMapping(value = "/get/{code}")
    @ResponseBody
    public Object get(@PathVariable("code") String studentCode) {
        Student student = studentService.get(studentCode);

        if (null == student) {
            Tip tip = new ErrorTip(500, "无效的学员编码");
            return tip;
        }else
            return student;
    }

}
