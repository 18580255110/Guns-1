package com.stylefeng.guns.modular.examineMGR.answer.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.base.controller.BaseController;
import com.stylefeng.guns.core.base.tips.ErrorTip;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.log.LogObjectHolder;
import com.stylefeng.guns.modular.examineMGR.answer.warpper.AnswerPaperWrapper;
import com.stylefeng.guns.modular.examineMGR.paper.warpper.PaperWrapper;
import com.stylefeng.guns.modular.examineMGR.service.IExamineAnswerDetailService;
import com.stylefeng.guns.modular.examineMGR.service.IExamineAnswerService;
import com.stylefeng.guns.modular.examineMGR.service.IExaminePaperService;
import com.stylefeng.guns.modular.examineMGR.transfer.AnswerDetailDto;
import com.stylefeng.guns.modular.questionMGR.warpper.QuestionWrapper;
import com.stylefeng.guns.modular.system.dao.ExaminePaperMapper;
import com.stylefeng.guns.modular.system.model.ExamineAnswer;
import com.stylefeng.guns.modular.system.model.ExamineAnswerDetail;
import com.stylefeng.guns.modular.system.model.ExamineAnswerStateEnum;
import com.stylefeng.guns.modular.system.model.ExaminePaper;
import com.stylefeng.guns.util.ToolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/1/24 13:26
 * @Version 1.0
 */
@Controller
@RequestMapping("/examine/answer")
public class AnswerController extends BaseController {

    private String PREFIX = "/examineMGR/answer/";

    @Autowired
    private IExamineAnswerService examineAnswerService;

    @Autowired
    private IExamineAnswerDetailService examineAnswerDetailService;
    @Autowired
    private IExaminePaperService examinePaperService;
    /**
     * 跳转到试卷首页
     */
    @RequestMapping("")
    public String index() {
        return PREFIX + "answer.html";
    }


    /**
     * 跳转到添加成绩单管理
     */
    @RequestMapping("/examineAnswer_add")
    public String examineAnswerAdd() {
        return PREFIX + "examineAnswer_add.html";
    }

    /**
     * 跳转到修改成绩单管理
     */
    @RequestMapping("/examineAnswer_update/{examineAnswerId}")
    public String examineAnswerUpdate(@PathVariable Integer examineAnswerId, Model model) {
        ExamineAnswer examineAnswer = examineAnswerService.selectById(examineAnswerId);
        model.addAttribute("item",examineAnswer);
        LogObjectHolder.me().set(examineAnswer);
        return PREFIX + "examineAnswer_edit.html";
    }

    /**
     * 新增成绩单管理
     */
    @RequestMapping(value = "/add")
    @ResponseBody
    public Object add(ExamineAnswer examineAnswer) {
        ExaminePaper examinePaper = examinePaperService.selectOne(new EntityWrapper<ExaminePaper>() {{
            eq("code", examineAnswer.getPaperCode());
        }});
        if(examinePaper == null){
            return new ErrorTip(501,"未查到匹配试卷");
        }
        examineAnswer.setQuota(examinePaper.getCount());
        examineAnswer.setCreateDate(new Date());
        examineAnswer.setTotalScore(examinePaper.getTotalScore());
        examineAnswerService.insert(examineAnswer);
        return SUCCESS_TIP;
    }

    /**
     * 删除成绩单管理
     */
    @RequestMapping(value = "/delete")
    @ResponseBody
    public Object delete(@RequestParam Long examineAnswerId) {
        examineAnswerService.deleteById(examineAnswerId);
        return SUCCESS_TIP;
    }

    /**
     * 修改成绩单管理
     */
    @RequestMapping(value = "/update")
    @ResponseBody
    public Object update(ExamineAnswer examineAnswer) {
        examineAnswerService.updateById(examineAnswer);
        return SUCCESS_TIP;
    }


    /**
     * 跳转到修改入学诊断
     */
    @RequestMapping("/view/{code}")
    public String questionUpdate(@PathVariable String code, Model model) {
        if (null == code)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{""});

        ExamineAnswer examineAnswer = examineAnswerService.get(code);

        if (null == examineAnswer)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"试卷"});

        model.addAttribute("item", examineAnswer);

        Wrapper<ExamineAnswerDetail> answerDetailQuery = new EntityWrapper<ExamineAnswerDetail>();
        answerDetailQuery.eq("answer_code", code);
        answerDetailQuery.eq("status", GenericState.Valid.code);

        List<AnswerDetailDto> answerDetailList = examineAnswerDetailService.selectDetailList(answerDetailQuery);
        model.addAttribute("answerDetails", answerDetailList);

        LogObjectHolder.me().set(examineAnswer);
        return PREFIX + "paper_detail.html";
    }

    /**
     * 获取答卷列表
     */
    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list(@RequestParam Map<String, Object> conditionMap) {
        //分页查詢
        List<Integer> statusList = new ArrayList<>();
        if (conditionMap.containsKey("status")){
            if (ToolUtil.isNotEmpty(conditionMap.get("status"))){
                int status = 0;
                try {
                    status = Integer.parseInt(conditionMap.get("status").toString());
                }catch(Exception e){}

                switch(status){
                    case 1:
                        statusList.add(ExamineAnswerStateEnum.Create.code);
                        statusList.add(ExamineAnswerStateEnum.Pause.code);
                        statusList.add(ExamineAnswerStateEnum.Testing.code);
                        break;
                    default:
                        statusList.add(status);
                        break;
                }
            }

            conditionMap.remove("status");
        }
        conditionMap.put("statusList", statusList);

        Page<Map<String, Object>> pageMap = examineAnswerService.selectMapsPage(conditionMap);
        //包装数据
        new AnswerPaperWrapper(pageMap.getRecords()).warp();
        return super.packForBT(pageMap);
    }


    /**
     * 导出答卷汇总信息
     */
    @RequestMapping(value = "/export/gather")
    @ResponseBody
    public Object exportGather(@RequestParam Map<String, Object> conditionMap) {
        //分页查詢
        Page<Map<String, Object>> pageMap = examineAnswerService.selectMapsPage(conditionMap);
        //包装数据
        new QuestionWrapper(pageMap.getRecords()).warp();
        //包装数据
        return new PaperWrapper(pageMap.getRecords()).warp();
    }


    /**
     * 导出答卷明细信息
     */
    @RequestMapping(value = "/export/detail")
    @ResponseBody
    public Object exportDetail(@RequestParam Map<String, Object> conditionMap) {
        //分页查詢
        Page<Map<String, Object>> pageMap = examineAnswerService.selectMapsPage(conditionMap);
        //包装数据
        new QuestionWrapper(pageMap.getRecords()).warp();
        //包装数据
        return new PaperWrapper(pageMap.getRecords()).warp();
    }
}
