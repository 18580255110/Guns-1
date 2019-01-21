package com.stylefeng.guns.modular.questionMGR.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.common.constant.factory.PageFactory;
import com.stylefeng.guns.common.exception.BizExceptionEnum;
import com.stylefeng.guns.core.admin.Administrator;
import com.stylefeng.guns.core.base.controller.BaseController;
import com.stylefeng.guns.core.exception.GunsException;
import com.stylefeng.guns.core.shiro.ShiroKit;
import com.stylefeng.guns.log.LogObjectHolder;
import com.stylefeng.guns.modular.examineMGR.service.IQuestionService;
import com.stylefeng.guns.modular.questionMGR.warpper.QuestionWrapper;
import com.stylefeng.guns.modular.system.model.Question;
import com.stylefeng.guns.modular.system.model.QuestionItem;
import com.stylefeng.guns.util.CodeKit;
import com.stylefeng.guns.util.ToolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.stylefeng.guns.common.constant.factory.MutiStrFactory.*;

/**
 * 入学诊断控制器
 *
 * @author fengshuonan
 * @Date 2018-11-04 11:32:55
 */
@Controller
@RequestMapping("/question")
public class QuestionController extends BaseController {

    private String PREFIX = "/questionMGR/question/";

    @Autowired
    private IQuestionService questionService;

    /**
     * 跳转到入学诊断首页
     */
    @RequestMapping("")
    public String index() {
        return PREFIX + "question.html";
    }

    /**
     * 跳转到添加入学诊断
     */
    @RequestMapping("/question_add")
    public String questionAdd() {
        return PREFIX + "question_add.html";
    }

    /**
     * 跳转到修改入学诊断
     */
    @RequestMapping("/question_update/{questionId}")
    public String questionUpdate(@PathVariable Integer questionId, Model model) {
        Question question = questionService.selectById(questionId);
        model.addAttribute("item", question);
        LogObjectHolder.me().set(question);
        return PREFIX + "question_edit.html";
    }

    /**
     * 获取入学诊断列表
     */
    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list(String condition) {
        //分页查詢
        Page<Question> page = new PageFactory<Question>().defaultPage();
        Page<Map<String, Object>> pageMap = questionService.selectMapsPage(page, new EntityWrapper<Question>(){{
            if(StringUtils.isNotEmpty(condition)){
                like("code",condition);
            }
        }});
        //包装数据
        new QuestionWrapper(pageMap.getRecords()).warp();
        return super.packForBT(pageMap);
    }

    @RequestMapping(value = "/listObject")
    @ResponseBody
    public Object listObject(){
        return  questionService.selectList(null);
    }

    /**
     * 新增入学诊断
     */
    @RequestMapping(value = "/add")
    @ResponseBody
    public Object add(Question question, String answerItems) {
        if (ToolUtil.isOneEmpty(question, answerItems)) {
            throw new GunsException(BizExceptionEnum.REQUEST_NULL);
        }

        //解析dictValues
        List<Map<String, String>> items = parseKeyValue(answerItems);

        List<QuestionItem> questionItemList = new ArrayList<QuestionItem>();
        StringBuilder expectedAnswer = new StringBuilder();

        for(Map<String, String> item : items) {
            QuestionItem questionItem = new QuestionItem();
            questionItem.setText(item.get(MUTI_STR_NAME));
            questionItem.setValue(item.get(MUTI_STR_CODE));

            boolean isAnswer = Boolean.valueOf(item.get(MUTI_STR_NUM));
            questionItemList.add(questionItem);
            if (isAnswer)
                expectedAnswer.append(questionItem.getValue()).append(",");
        }

        if (questionItemList.isEmpty())
            throw new GunsException(BizExceptionEnum.REQUEST_NULL);

        Administrator currAdmin = ShiroKit.getUser();
        question.setExpactAnswer(expectedAnswer.substring(0, expectedAnswer.length() - 1));
        question.setTeacher(currAdmin.getAccount());
        question.setTeacherName(currAdmin.getName());
        questionService.create(question, questionItemList);

        return SUCCESS_TIP;
    }

    /**
     * 删除入学诊断
     */
    @RequestMapping(value = "/delete")
    @ResponseBody
    public Object delete(@RequestParam String questionCode) {
        questionService.delete(questionCode);
        return SUCCESS_TIP;
    }

    /**
     * 修改入学诊断
     */
    @RequestMapping(value = "/update")
    @ResponseBody
    public Object update(Question question) {
        questionService.updateById(question);
        return SUCCESS_TIP;
    }

    /**
     * 入学诊断详情
     */
    @RequestMapping(value = "/detail/{questionId}")
    @ResponseBody
    public Object detail(@PathVariable("questionId") Integer questionId) {
        return questionService.selectById(questionId);
    }
}
