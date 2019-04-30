package com.stylefeng.guns.modular.orderMGR.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.common.constant.factory.PageFactory;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.base.controller.BaseController;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.classMGR.transfer.ClassPlan;
import com.stylefeng.guns.modular.classMGR.warpper.ClassWrapper;
import com.stylefeng.guns.modular.education.service.IScheduleClassService;
import com.stylefeng.guns.modular.memberMGR.service.IMemberService;
import com.stylefeng.guns.modular.orderMGR.OrderAddList;
import com.stylefeng.guns.modular.orderMGR.service.ICourseCartService;
import com.stylefeng.guns.modular.orderMGR.service.IOrderService;
import com.stylefeng.guns.modular.studentMGR.service.IStudentService;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.modular.system.service.IAttachmentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单管理控制器
 *
 * @author fengshuonan
 * @Date 2018-10-18 22:46:16
 */
@Controller
@RequestMapping("/order/sign")
public class SignController extends BaseController {

    private String PREFIX = "/orderMGR/order/";

    @Autowired
    private ICourseCartService courseCartService;

    @Autowired
    private IClassService classService;

    @Autowired
    private IAttachmentService attachmentService;

    @Autowired
    private IMemberService memberService;

    @Autowired
    private IStudentService studentService;

    @Autowired
    private IScheduleClassService scheduleClassService;

    @Autowired
    private IOrderService orderService;

    /**
     * 跳转到报名首页
     */
    @RequestMapping("")
    public String index() {
        return PREFIX + "sign.html";
    }

    @RequestMapping("/sign_import/{classCode}")
    public String openImportDlg(@PathVariable("classCode") String code, Model model){

        if (null == code)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"班级信息"});

        Class classInfo = classService.get(code);

        if (null == classInfo)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"班级信息"});

        model.addAttribute("item", classInfo);
        return PREFIX + "sign_import.html";
    }


    @RequestMapping("/sign_wizard/{classCode}")
    public String openSignDlg(@PathVariable("classCode") String code, Model model){

        if (null == code)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"班级信息"});

        Class classInfo = classService.get(code);

        if (null == classInfo)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"班级信息"});

        Map<String, Object> resultMap = toMap(classInfo);

        new ClassWrapper(resultMap).warp();
        model.addAttribute("classInfo", resultMap);
        return PREFIX + "sign_wizard.html";
    }


    @RequestMapping("/plan/list")
    @ResponseBody
    public Object planList(String classCode){

        Map<String, Object> classPlanInfo = new HashMap<String, Object>();

        Map<String, Object> queryMap = new HashMap<String, Object>();
        queryMap.put("classCode", classCode);
        queryMap.put("status", GenericState.Valid.code);

        List<ClassPlan> planList = scheduleClassService.selectPlanList(queryMap);

        //包装数据
        Page<ClassPlan> page = new PageFactory<ClassPlan>().defaultPage();
        page.setSize(100);
        page.setTotal(planList.size());
        page.setRecords(planList);
        return super.packForBT(page);
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
            System.out.println("transBean2Map Error " + e);
        }

        return map;
    }

    /**
     * 获取可以报名的班级列表
     */
    @RequestMapping(value = "/classlist")
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

                gt("sign_start_date", new Date());
                eq("status", GenericState.Valid.code);

                orderBy("id", false);
            }
        });
        //包装数据
        new ClassWrapper(pageMap.getRecords()).warp();
        return super.packForBT(pageMap);
    }

    /**
     * 获取需要预报名的班级列表
     */
    @RequestMapping(value = "/doSign")
    @ResponseBody
    public Object sign( @RequestBody SignRequest request) {

        Class classInfo = classService.get(request.getClassInfo().getCode());

        if (null == classInfo)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"班级信息"});

        Member member = request.getMember();
        Map<String, Object> memberRegistMap = new HashMap<>();
        memberRegistMap.put("number", member.getMobileNumber());
        memberRegistMap.put("name", member.getName());

        Member currMember = null;
        try {
            currMember = memberService.createMember(memberService.generateUserName(), memberRegistMap);
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
        if (null == currStudent){
            Wrapper<Student> queryWrapper = new EntityWrapper<Student>();
            queryWrapper.eq("name", request.getStudent().getName().trim());
            queryWrapper.eq("user_name", currMember.getUserName());
            queryWrapper.eq("status", GenericState.Valid.code);
            currStudent = studentService.selectOne(queryWrapper);

            if (null == currStudent)
                currStudent = studentService.addStudent(currMember.getUserName(), student);
        }

        if (null == currMember)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"家长信息"});

        // 下订单
        String courseCartCode = courseCartService.doJoin(currMember, currStudent, classInfo, true);

        PayTypeEnum paytype = PayTypeEnum.AppPay;

        try {
            paytype = PayTypeEnum.instanceOf(request.getPayType());
        }catch(Exception e){}

        if (PayTypeEnum.ClassicPay.equals(paytype)){
            OrderItem orderItem = new OrderItem();
            orderItem.setCourseCartCode(courseCartCode);
            orderItem.setItemObject(OrderItemTypeEnum.Course.code);
            orderItem.setItemObjectCode(classInfo.getCode());
            orderItem.setItemAmount(classInfo.getPrice());
            OrderAddList orderAddList = new OrderAddList();
            orderAddList.add(orderItem);

            Map<String, Object> extendInfo = new HashMap<>();
            Order signOrder = orderService.order(currMember, orderAddList, PayMethodEnum.classic, extendInfo);
            orderService.completePay(signOrder.getAcceptNo());
        }

        return SUCCESS_TIP;
    }

    @RequestMapping(value = "/signable/{classCode}", method = RequestMethod.POST)
    @ResponseBody
    public Object precheck(@PathVariable("classCode") String classCode) {
        if (null == classCode)
            return "NO";

        Class classInfo = classService.get(classCode);

        if (null == classInfo)
            return "NO";

        if (GenericState.Invalid.code == classInfo.getStatus())
            return "NO";

        if (ClassSignableEnum.YES.code != classInfo.getSignable())
            return "NO";

/*        Date now = new Date();
        if ( now.before(classInfo.getSignStartDate()) || now.after(classInfo.getSignEndDate()))
            return "NO";*/

        return "YES";
    }

    /**
     * 获取需要预报名的班级列表
     */
    @RequestMapping(value = "/import/student")
    @ResponseBody
    public Object importStudents(String classCode, String masterName, String masterCode) {
/*
        Attachment file = null;
        List<Attachment> attachmentList = attachmentService.listAttachment(masterName, masterCode);
        if (null != attachmentList && attachmentList.size() > 0){
            file = attachmentList.get(0);
        }

        Class classInfo = classService.get(classCode);

        if (null == classInfo)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"班级信息"});

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File(file.getPath())));

            String line = reader.readLine();
            while(null != line){
                StringTokenizer tokenizer = new StringTokenizer(line, "|");
                int idx = 0;
                Member member = null;
                Student student = null;
                while(tokenizer.hasMoreTokens()){
                    switch(idx){
                        case 0:
                            member = memberService.get(tokenizer.nextToken());
                            idx++;
                            break;
                        case 1:
                            student = studentService.get(tokenizer.nextToken());
                            idx++;
                            break;
                    }
                }

                line = reader.readLine();

                if (null == student || null == member)
                    continue;

                try {
                    courseCartService.doJoin(member, student, classInfo);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != reader)
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        */
        return SUCCESS_TIP;

    }
}
