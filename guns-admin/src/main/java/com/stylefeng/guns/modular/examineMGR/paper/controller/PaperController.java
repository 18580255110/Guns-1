package com.stylefeng.guns.modular.examineMGR.paper.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.common.annotion.BussinessLog;
import com.stylefeng.guns.common.annotion.Permission;
import com.stylefeng.guns.common.constant.Const;
import com.stylefeng.guns.common.constant.dictmap.DictMap;
import com.stylefeng.guns.common.constant.factory.ConstantFactory;
import com.stylefeng.guns.common.constant.factory.PageFactory;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.exception.BizExceptionEnum;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.admin.Administrator;
import com.stylefeng.guns.core.base.controller.BaseController;
import com.stylefeng.guns.core.exception.GunsException;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.core.shiro.ShiroKit;
import com.stylefeng.guns.log.LogObjectHolder;
import com.stylefeng.guns.modular.examineMGR.paper.warpper.PaperUseWrapper;
import com.stylefeng.guns.modular.examineMGR.paper.warpper.PaperWrapper;
import com.stylefeng.guns.modular.examineMGR.service.*;
import com.stylefeng.guns.modular.questionMGR.warpper.QuestionWrapper;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.util.ToolUtil;
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
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/1/22 15:54
 * @Version 1.0
 */
@Controller
@RequestMapping("/examine/paper")
public class PaperController extends BaseController {
    private String PREFIX = "/examineMGR/paper/";

    @Autowired
    private IExaminePaperService examinePaperService;

    @Autowired
    private IExaminePaperItemService examinePaperItemService;

    @Autowired
    private IQuestionService questionService;

    @Autowired
    private IQuestionItemService questionItemService;

    @Autowired
    private IExamineApplyService examineApplyService;

    /**
     * 跳转到试卷首页
     */
    @RequestMapping("")
    public String index() {
        return PREFIX + "paper.html";
    }

    /**
     * 跳转到添加入学诊断
     */
    @RequestMapping("/wizard")
    public String wizard( String code, Model model ) {
        ExaminePaper paper = examinePaperService.get(code);
        Set<String> questionCodes = new HashSet<String>();
        Map<String, String> questionScores = new HashMap<String, String>();

        String operator = "add";

        if (null != paper) {
            operator = "update";
            Wrapper<ExaminePaperItem> questionItemQuery = new EntityWrapper<ExaminePaperItem>();
            questionItemQuery.eq("paper_code", code);
            questionItemQuery.eq("status", GenericState.Valid.code);

            List<ExaminePaperItem> questionItemList = examinePaperItemService.selectList(questionItemQuery);

            for (ExaminePaperItem examinePaperItem : questionItemList) {
                String questionCode = examinePaperItem.getQuestionCode();
                String questionScore = examinePaperItem.getScore();

                questionCodes.add(questionCode);
                questionScores.put(questionCode, questionScore);
            }
        }

        model.addAttribute("item", paper);
        model.addAttribute("operator", operator);
        model.addAttribute("questionCodes", JSON.toJSONString(questionCodes));
        model.addAttribute("questionScores", JSON.toJSONString(questionScores));
        LogObjectHolder.me().set(paper);
        return PREFIX + "paper_wizard.html";
    }

    @RequestMapping("/wizard/review")
    public String wizardReview(String questionItemExp, Model model){
        List<Map<String, Object>> viewList = new ArrayList<>();
        try {
            Iterator<Object> codeIter = JSON.parseArray(questionItemExp).iterator();
            int index = 1;
            while (codeIter.hasNext()) {
                String code = codeIter.next().toString();

                Question question = questionService.get(code);

                if (null != question){
                    List<QuestionItem> questionItemList = questionItemService.findAll(code);

                    Map<String, Object> questionViewer = new HashMap<>();
                    questionViewer.put("question", question.getQuestion());
                    questionViewer.put("title", String.format("第 %s 题", index++));
                    questionViewer.put("answerList", questionItemList);

                    viewList.add(questionViewer);
                }
            }
        }catch(Exception e){}

        model.addAttribute("questionList", viewList);
        return PREFIX + "paper_wizard_review.html";
    }


    @RequestMapping("/open_use")
    public String openUse(String code, Model model){

        ExaminePaper paper = examinePaperService.get(code);
        Map<String, Object> paperMap = new HashMap<String, Object>();
        paperMap.put("code", paper.getCode());
        paperMap.put("grades", paper.getGrades());
        paperMap.put("gradeName", (new ConstantFactory()).getGradeName(Integer.parseInt(paper.getGrades())));
        paperMap.put("subject", paper.getSubject());
        paperMap.put("subjectName", (new ConstantFactory()).getsubjectName(Integer.parseInt(paper.getSubject())));
        paperMap.put("totalScore", paper.getTotalScore());
        paperMap.put("teacher", paper.getTeacher());
        paperMap.put("count", paper.getCount());

        List<Map<String, Object>> paperUseList = examineApplyService.listPaperUse(code);

        model.addAttribute("paper", paperMap);

        new PaperUseWrapper(paperUseList).warp();

        model.addAttribute("itemList", paperUseList);
        return PREFIX + "paper_use.html";
    }


    /**
     * 获取入学诊断列表
     */
    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list(@RequestParam Map<String, String> queryParams) {
        //分页查詢
        Page<ExaminePaper> page = new PageFactory<ExaminePaper>().defaultPage();
        Page<Map<String, Object>> pageMap = examinePaperService.selectMapsPage(page, new EntityWrapper<ExaminePaper>() {{
            if (queryParams.containsKey("condition") && StringUtils.isNotEmpty(queryParams.get("condition"))) {
                eq("code", queryParams.get("condition"));
                or();
                like("question",queryParams.get("condition"));
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
        }});
        //包装数据
        new PaperWrapper(pageMap.getRecords()).warp();
        return super.packForBT(pageMap);
    }


    /**
     * 获取入学诊断列表
     *
     * workingCodes 已选中的题目编码
     */
    @RequestMapping(value = "/question/list")
    @ResponseBody
    public Object questionList(@RequestParam Map<String, Object> conditionMap, String workingCodes) {

        Set<String> workingQuestionList = new HashSet<String>();
        if (ToolUtil.isNotEmpty(workingCodes)){
            StringTokenizer codeIter = new StringTokenizer(workingCodes, ",");
            while(codeIter.hasMoreTokens()){
                workingQuestionList.add(codeIter.nextToken());
            }
        }

        conditionMap.put("status", GenericState.Valid.code);

        //分页查詢
        Page<Map<String, Object>> pageMap = questionService.selectMapsPage(conditionMap, workingQuestionList);
        //包装数据
        new QuestionWrapper(pageMap.getRecords()).warp();
        return super.packForBT(pageMap);
    }

    /**
     * 新增试卷
     */
    @RequestMapping(value = "/add")
    @ResponseBody
    public Object add(ExaminePaper paper, String paperItems) {
        if (ToolUtil.isOneEmpty(paper, paperItems)) {
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"请检查数据"});
        }

        //解析
        Set<ExaminePaperItem> workingQuestionList = reassembleExaminePaperItemSet(paperItems);

        Administrator currAdmin = ShiroKit.getUser();
        paper.setTeacher(currAdmin.getName());

        examinePaperService.create
                (paper, workingQuestionList);

        return SUCCESS_TIP;
    }

    private Set<ExaminePaperItem> reassembleExaminePaperItemSet(String paperItems) {
        Set<ExaminePaperItem> workingQuestionList = new HashSet<ExaminePaperItem>();
        if (ToolUtil.isNotEmpty(paperItems)){
            StringTokenizer codeIter = new StringTokenizer(paperItems, ";");
            while(codeIter.hasMoreTokens()){
                ExaminePaperItem paperItem = new ExaminePaperItem();
                String[] itemMap = codeIter.nextToken().split("=");
                paperItem.setQuestionCode(itemMap[0]);
                paperItem.setScore(itemMap[1]);
                workingQuestionList.add(paperItem);
            }
        }

        return workingQuestionList;
    }

    /**
     * 复制试卷
     */
    @RequestMapping(value = "/copy")
    @ResponseBody
    public Object copy(@RequestParam String code) {
        if (ToolUtil.isOneEmpty(code)) {
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"请选择需要复制的试卷 "});
        }
        // 复制试卷
        examinePaperService.copy(code);
        return SUCCESS_TIP;
    }

    /**
     * 删除试卷
     */
    @RequestMapping(value = "/pause")
    @ResponseBody
    public Object pause(@RequestParam String code) {
        // 只能逻辑删
        examinePaperService.doPause(code);
        return SUCCESS_TIP;
    }

    /**
     * 恢复试卷
     */
    @RequestMapping(value = "/resume")
    @ResponseBody
    public Object resume(@RequestParam String code) {
        // 只能逻辑删
        examinePaperService.doResume(code);
        return SUCCESS_TIP;
    }

    /**
     * 修改入学诊断
     */
    @RequestMapping(value = "/update")
    @ResponseBody
    public Object update(ExaminePaper paper, String paperItems) {
        if (ToolUtil.isOneEmpty(paper, paperItems)) {
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"请检查数据"});
        }

        //解析
        Set<ExaminePaperItem> workingQuestionList = reassembleExaminePaperItemSet(paperItems);

        Administrator currAdmin = ShiroKit.getUser();
        paper.setTeacher(currAdmin.getName());

        examinePaperService.update(paper, workingQuestionList);

        return SUCCESS_TIP;
    }

    /**
     * 新增字典
     *
     * @param
     */
    @RequestMapping(value = "/use")
    @ResponseBody
    public Object use(String paperCode,String applyItems) {
        if (ToolUtil.isOneEmpty(paperCode,applyItems)) {
            throw new GunsException(BizExceptionEnum.REQUEST_NULL);
        }

        List<ExamineApply> examineApplyList = new ArrayList<ExamineApply>();
        try {
            examineApplyList = JSON.parseArray(applyItems, ExamineApply.class);
        }catch(Exception e){}

        if (examineApplyList.isEmpty())
            throw new GunsException(BizExceptionEnum.REQUEST_NULL);

        examineApplyService.doUse(paperCode, examineApplyList);
        return SUCCESS_TIP;
    }
}
