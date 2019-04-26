package com.stylefeng.guns.modular.questionMGR.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.common.constant.factory.PageFactory;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.constant.state.YesOrNoState;
import com.stylefeng.guns.common.enums.StatusEnum;
import com.stylefeng.guns.common.exception.BizExceptionEnum;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.admin.Administrator;
import com.stylefeng.guns.core.base.controller.BaseController;
import com.stylefeng.guns.core.exception.GunsException;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.core.shiro.ShiroKit;
import com.stylefeng.guns.log.LogObjectHolder;
import com.stylefeng.guns.modular.examineMGR.QuestionTypeEnum;
import com.stylefeng.guns.modular.examineMGR.service.IQuestionItemService;
import com.stylefeng.guns.modular.examineMGR.service.IQuestionService;
import com.stylefeng.guns.modular.questionMGR.warpper.QuestionWrapper;
import com.stylefeng.guns.modular.system.model.Question;
import com.stylefeng.guns.modular.system.model.QuestionItem;
import com.stylefeng.guns.util.CodeKit;
import com.stylefeng.guns.util.ToolUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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

    @Autowired
    private IQuestionItemService questionItemService;

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
    @RequestMapping("/question_update/{code}")
    public String questionUpdate(@PathVariable String code, Model model) {
        Question question = questionService.get(code);

        if (null == question)
            throw new GunsException(BizExceptionEnum.REQUEST_NULL);

        List<QuestionItem> questionItemList = questionItemService.selectList(new EntityWrapper<QuestionItem>().eq("question_code", question.getCode()).eq("status", GenericState.Valid.code));
        if( question != null && question.getQuestion().contains("src=\"http://www.kecui.com.cn/download")){
            question.setQuestion(question.getQuestion().replace("\"","'"));
        }
        model.addAttribute("item", question);
        model.addAttribute("answerItemList", questionItemList);

        LogObjectHolder.me().set(question);
        return PREFIX + "question_edit.html";
    }

    /**
     * 跳转到修改入学诊断
     */
    @RequestMapping("/question_view/{code}")
    public String questionView(@PathVariable("code") String code, Model model) {

        if ( null != code ){
            Question question = questionService.get(code);

            if (null != question){
                List<QuestionItem> questionItemList = questionItemService.findAll(code);

                model.addAttribute("question", question);
                model.addAttribute("itemList", questionItemList);
            }
        }

        return PREFIX + "question_view.html";
    }

    /**
     * 跳转到修改入学诊断
     */
    @RequestMapping("/question/view")
    public String questionView2(Question question, String answerItems, Model model) {
        StringBuilder expectedAnswer = new StringBuilder();
        List<QuestionItem> questionItemList = parseQuestionItems(answerItems, expectedAnswer);

        model.addAttribute("question", question);
        model.addAttribute("itemList", questionItemList);

        return PREFIX + "question_view.html";
    }

    /**
     * 获取入学诊断列表
     */
    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list(@RequestParam Map<String, String> queryParams) {
        //分页查詢
        Page<Question> page = new PageFactory<Question>().defaultPage();
        Page<Map<String, Object>> pageMap = questionService.selectMapsPage(page, new EntityWrapper<Question>() {{
            if (queryParams.containsKey("condition") && StringUtils.isNotEmpty(queryParams.get("condition"))) {
                like("question", queryParams.get("condition"));
                or();
                eq("code", queryParams.get("condition"));
            }
            if (StringUtils.isNotEmpty(queryParams.get("status"))) {
                try {
                    int status = Integer.parseInt(queryParams.get("status"));
                    eq("status", status);
                } catch (Exception e) {
                }
            }
            if (StringUtils.isNotEmpty(queryParams.get("subject"))) {
                try {
                    int subject = Integer.parseInt(queryParams.get("subject"));
                    eq("subject", subject);
                } catch (Exception e) {
                }
            }
            if (StringUtils.isNotEmpty(queryParams.get("grade"))) {
                try {
                    String grade = queryParams.get("grade");
                    eq("grade", grade);
                } catch (Exception e) {
                }
            }
            ne("status",3);
            orderBy("id",false);
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

        StringBuilder expectedAnswer = new StringBuilder();
        List<QuestionItem> questionItemList = parseQuestionItems(answerItems, expectedAnswer);

        Administrator currAdmin = ShiroKit.getUser();
        question.setExpactAnswer(expectedAnswer.substring(0, expectedAnswer.length() - 1));
        question.setTeacher(currAdmin.getAccount());
        question.setTeacherName(currAdmin.getName());
        questionService.create(question, questionItemList);

        return SUCCESS_TIP;
    }
    /**
     * 删除入学诊断题目
     */
    @RequestMapping(value = "/delete")
    @ResponseBody
    public Object delete(String questionId) {
        if(StringUtils.isNotEmpty(questionId)){
            Question question = new Question();
            question.setId(Long.parseLong(questionId));
            question.setStatus(StatusEnum.STATUS_DEL.getCode());
            questionService.updateById(question);
        }

        return SUCCESS_TIP;
    }

    private List<QuestionItem> parseQuestionItems(String answerItems, StringBuilder expectedAnswer) {
        List<QuestionItem> questionItemList = new ArrayList<QuestionItem>();
        //解析dictValues
        List<Map<String, String>> items = parseKeyValue(answerItems);
        for(Map<String, String> item : items) {
            QuestionItem questionItem = parseQuestionItem(item);

            if (null == questionItem)
                continue;

            boolean isAnswer = Boolean.valueOf(item.get(MUTI_STR_NUM));
            if (isAnswer) {
                questionItem.setExpect(YesOrNoState.Yes.code);
                expectedAnswer.append(questionItem.getValue()).append(",");
            }
            questionItemList.add(questionItem);
        }

        if (questionItemList.isEmpty())
            throw new GunsException(BizExceptionEnum.REQUEST_NULL);

        if (0 == expectedAnswer.length()){
            // 没有设置期望答案
            throw new ServiceException(MessageConstant.MessageCode.QUESTION_NO_EXPECT_ANSWER);
        }
        return questionItemList;
    }

    private QuestionItem parseQuestionItem(Map<String, String> item) {
        String itemText = parseItemText(item.get(MUTI_STR_NAME));

        if (null == itemText)
            return null;

        QuestionItem questionItem = new QuestionItem();
        questionItem.setText(itemText);
        questionItem.setValue(item.get(MUTI_STR_CODE));
        questionItem.setExpect(YesOrNoState.No.code);
        return questionItem;
    }

    private String parseItemText(String text) {
        String parsedText = null;
        try {
            parsedText = new String(Base64.decodeBase64(text), "UTF-8");
            parsedText = URLDecoder.decode(parsedText);
        } catch (UnsupportedEncodingException e) {
        }
        return parsedText;
    }

    /**
     * 停用题目
     */
    @RequestMapping(value = "/pause")
    @ResponseBody
    public Object pause(@RequestParam String code) {
        questionService.doPause(code);
        return SUCCESS_TIP;
    }

    /**
     * 启用题目
     */
    @RequestMapping(value = "/resume")
    @ResponseBody
    public Object resume(@RequestParam String code) {
        questionService.doResume(code);
        return SUCCESS_TIP;
    }

    /**
     * 修改入学诊断
     */
    @RequestMapping(value = "/update")
    @ResponseBody
    public Object update(Question question, String answerItems) {
        if (ToolUtil.isOneEmpty(question, answerItems)) {
            throw new GunsException(BizExceptionEnum.REQUEST_NULL);
        }

//        //解析dictValues
//        List<Map<String, String>> items = parseKeyValue(answerItems);
//
//        List<QuestionItem> questionItemList = new ArrayList<QuestionItem>();
//        StringBuilder expectedAnswer = new StringBuilder();
//
//        for(Map<String, String> item : items) {
//            QuestionItem questionItem = new QuestionItem();
//
//            String itemText = parseItemText(item.get(MUTI_STR_NAME));
//
//            if (null == itemText)
//                continue;
//
//            questionItem.setText(itemText);
//            questionItem.setValue(item.get(MUTI_STR_CODE));
//            questionItem.setExpect(YesOrNoState.No.code);
//
//            boolean isAnswer = Boolean.valueOf(item.get(MUTI_STR_NUM));
//            if (isAnswer) {
//                questionItem.setExpect(YesOrNoState.Yes.code);
//                expectedAnswer.append(questionItem.getValue()).append(",");
//            }
//            questionItemList.add(questionItem);
//        }
//
//        if (questionItemList.isEmpty())
//            throw new GunsException(BizExceptionEnum.REQUEST_NULL);
        StringBuilder expectedAnswer = new StringBuilder();
        List<QuestionItem> questionItemList = parseQuestionItems(answerItems, expectedAnswer);

        Administrator currAdmin = ShiroKit.getUser();
        question.setExpactAnswer(expectedAnswer.substring(0, expectedAnswer.length() - 1));
        question.setTeacher(currAdmin.getAccount());
        question.setTeacherName(currAdmin.getName());
        questionService.update(question, questionItemList);

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
