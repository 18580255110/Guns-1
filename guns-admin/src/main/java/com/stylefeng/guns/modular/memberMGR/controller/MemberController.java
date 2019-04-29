package com.stylefeng.guns.modular.memberMGR.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.common.constant.factory.PageFactory;
import com.stylefeng.guns.core.base.controller.BaseController;
import com.stylefeng.guns.log.LogObjectHolder;
import com.stylefeng.guns.modular.member.MemberStarEnum;
import com.stylefeng.guns.modular.memberMGR.service.IMemberService;
import com.stylefeng.guns.modular.memberMGR.warpper.MemberWrapper;
import com.stylefeng.guns.modular.system.model.Member;
import com.stylefeng.guns.modular.system.model.Teacher;
import com.stylefeng.guns.modular.teacherMGR.service.TeacherService;
import com.stylefeng.guns.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * 会员管理控制器
 *
 * @author fengshuonan
 * @Date 2018-10-09 22:53:21
 */
@Controller
@RequestMapping("/member")
public class MemberController extends BaseController {

    private String PREFIX = "/memberMGR/member/";

    @Autowired
    private IMemberService memberService;
    @Autowired
    private TeacherService teacherService;

    /**
     * 跳转到会员管理首页
     */
    @RequestMapping("")
    public String index() {
        return PREFIX + "member.html";
    }

    /**
     * 跳转到添加会员管理
     */
    @RequestMapping("/member_add")
    public String memberAdd() {
        return PREFIX + "member_add.html";
    }

    /**
     * 跳转到修改会员管理
     */
    @RequestMapping("/member_update/{memberId}")
    public String memberUpdate(@PathVariable Integer memberId, Model model) {
        Member member = memberService.selectById(memberId);
        model.addAttribute("item", member);
        LogObjectHolder.me().set(member);
        return PREFIX + "member_edit.html";
    }

    /**
     * 获取会员管理列表
     */
    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list(@RequestParam Map<String, String> queryMap) {
        //分页查詢
        Page<Member> page = new PageFactory<Member>().defaultPage();
        Page<Map<String, Object>> pageMap = memberService.selectMapsPage(page, new EntityWrapper<Member>() {
            {
                //name条件分页
                if (queryMap.containsKey("condition") && StringUtils.isNotEmpty(queryMap.get("condition"))) {
                    like("name", queryMap.get("condition").toString());
                    or();
                    like("user_name", queryMap.get("condition").toString());
                    or();
                    like("nickname", queryMap.get("condition").toString());
                }

                Date beginQueryDate = DateUtil.parseDate(queryMap.get("beginQueryDate"));
                if (null != beginQueryDate) {
                    ge("join_date", DateUtils.truncate(beginQueryDate, Calendar.DAY_OF_MONTH));
                }

                Date endQueryDate = DateUtil.parseDate(queryMap.get("endQueryDate"));
                if (null != endQueryDate) {
                    lt("join_date", DateUtils.addDays(DateUtils.truncate(endQueryDate, Calendar.DAY_OF_MONTH), 1));
                }

                if (StringUtils.isNotEmpty(queryMap.get("status"))){
                    try{
                        int status = Integer.parseInt(queryMap.get("status"));
                        eq("status", status);
                    }catch(Exception e){}
                }
            }
        });
        //包装数据
        new MemberWrapper(pageMap.getRecords()).warp();
        return super.packForBT(pageMap);

    }

    /**
     * 新增会员管理
     */
    @RequestMapping(value = "/add")
    @ResponseBody
    public Object add(Member member) {
        memberService.insert(member);
        return SUCCESS_TIP;
    }

    /**
     * 停用会员管理
     */
    @RequestMapping(value = "/pause")
    @ResponseBody
    public Object pause(@RequestParam String userName) {

        memberService.doPause(userName);

        return SUCCESS_TIP;
    }


    /**
     * 启用会员管理
     */
    @RequestMapping(value = "/resume")
    @ResponseBody
    public Object resume(@RequestParam String userName) {

        memberService.doResume(userName);

        return SUCCESS_TIP;
    }

    /**
     * 修改会员管理
     */
    @RequestMapping(value = "/update")
    @ResponseBody
    public Object update(Member member) {
        /* 会员是老师-同步老师信息*/
        Member oldMemBer = memberService.selectById(member.getId());
        if(MemberStarEnum.Star_99.code == oldMemBer.getStar()){
            Teacher teacher = teacherService.selectOne(new EntityWrapper<Teacher>() {{
                eq("mobile", oldMemBer.getMobileNumber());
            }});
            if( teacher != null && !teacher.getMobile().equals(member.getMobileNumber())){
                teacher.setMobile(member.getMobileNumber());
                teacherService.updateById(teacher);
            }
        }
        /*普通会员*/
        memberService.updateById(member);
        return SUCCESS_TIP;
    }

    /**
     * 会员管理详情
     */
    @RequestMapping(value = "/detail/{memberId}")
    @ResponseBody
    public Object detail(@PathVariable("memberId") Integer memberId) {
        return memberService.selectById(memberId);
    }

    /**
     * 会员管理详情
     */
    @RequestMapping(value = "/get/{userName}")
    @ResponseBody
    public Object get(@PathVariable("userName") String userName) {
        return memberService.get(userName);
    }
}
