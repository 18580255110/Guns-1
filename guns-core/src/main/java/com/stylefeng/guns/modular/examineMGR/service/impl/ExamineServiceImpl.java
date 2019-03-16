package com.stylefeng.guns.modular.examineMGR.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.stylefeng.guns.common.constant.factory.ConstantFactory;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.classMGR.service.ICourseService;
import com.stylefeng.guns.modular.examineMGR.service.*;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.util.ToolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2018/12/22 17:16
 * @Version 1.0
 */
@Service
public class ExamineServiceImpl implements IExamineService {

    @Autowired
    private IExaminePaperService examinePaperService;

    @Autowired
    private IExamineAnswerService examineAnswerService;

    @Autowired
    private IExamineAnswerDetailService examineAnswerDetailService;

    @Autowired
    private IExaminePaperItemService examinePaperItemService;

    @Autowired
    private IQuestionService questionService;

    @Autowired
    private IClassService classService;

    @Autowired
    private ICourseService courseService;

    @Override
    public ExaminePaper findExaminePaper(Map<String, Object> queryParams) {

        Class classInfo = classService.get((String)queryParams.get("classCode"));

        Course courseInfo = courseService.get(classInfo.getCourseCode());
        Wrapper<ExaminePaper> examinePaperQueryWrapper = new EntityWrapper<>();
        examinePaperQueryWrapper.eq("ability", classInfo.getAbility());
        examinePaperQueryWrapper.eq("grades", classInfo.getGrade());
        examinePaperQueryWrapper.eq("subject", courseInfo.getSubject());
        examinePaperQueryWrapper.eq("status", GenericState.Valid.code);

        return examinePaperService.selectOne(examinePaperQueryWrapper);
    }

    @Override
    public ExaminePaper getExaminePaper(String paperCode) {
        if (null == paperCode)
            return null;

        return examinePaperService.get(paperCode);
    }

    @Override
    public Map<String, Collection<Question>> doBeginExamine(Student student, ExaminePaper examinePaper) {

        // 生成答卷
        ExamineAnswer answerPaper = examineAnswerService.generatePaper(student, examinePaper);

        Wrapper<ExaminePaperItem> questionListQuery = new EntityWrapper<>();
        questionListQuery.eq("paper_code", examinePaper.getCode());
        List<ExaminePaperItem> examinePaperItemList = examinePaperItemService.selectList(questionListQuery);

        if (null == examinePaperItemList || examinePaperItemList.isEmpty()){
            // TODO 阻止业务进行
        }

        Set<Question> questionSet = new HashSet<>();
        for(ExaminePaperItem examinePaperItem : examinePaperItemList){
            questionSet.add(questionService.get(examinePaperItem.getQuestionCode()));
        }

        Map<String, Collection<Question>> beginResult = new HashMap<>();
        beginResult.put(answerPaper.getCode(), questionSet);
        return beginResult;
    }

    @Override
    public void doFinishExamine(String code, List<ExamineAnswerDetail> submitItems) {

        ExamineAnswer answerPaper = examineAnswerService.get(code);

        if (null == answerPaper)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS);

        examineAnswerDetailService.insertBatch(submitItems);

        Date now = new Date();
        answerPaper.setStatus(ExamineAnswerStateEnum.Submit.code);
        answerPaper.setEndDate(now);

        examineAnswerService.updateById(answerPaper);
    }

    @Override
    public Collection<Map<String, Object>> findExamineAnswerPaperList(String student) {
        if (null == student)
            return new ArrayList<>();


        Wrapper<ExamineAnswer> examineAnswerWrapper = new EntityWrapper<>();
        examineAnswerWrapper.eq("student_code", student);
        List<Map<String, Object>> resultList = examineAnswerService.selectMaps(examineAnswerWrapper);

        Set<Map<String, Object>> examineAnswerPaperList = new HashSet<>();
        for(Map<String, Object> result : resultList){
            ExaminePaper paper = examinePaperService.get((String)result.get("paperCode"));
            StringBuffer classNameBuffer = new StringBuffer();
            classNameBuffer.append(
                ConstantFactory.me().getGradeName(Integer.parseInt(paper.getGrades()))
            ).append(
                ConstantFactory.me().getsubjectName(Integer.parseInt(paper.getSubject()))
            );
//            Class classInfo = classService.get((String)result.get("classCode"));
//            if (null == classInfo)
//                continue;
//
            result.put("className", classNameBuffer.toString());
            result.put("ability", ConstantFactory.me().getAbilityName(paper.getAbility()));
            examineAnswerPaperList.add(result);
        }
        return examineAnswerPaperList;
    }

    @Override
    public ExamineAnswer getAnswerPaper(String code) {
        if (null == code)
            return null;

        return examineAnswerService.get(code);
    }

    @Override
    public int getQuestionScore(String paperCode, String questionCode) {
        if (ToolUtil.isAllEmpty(paperCode, questionCode))
            return 0;

        Wrapper<ExaminePaperItem> queryWrapper = new EntityWrapper<>();
        queryWrapper.eq("paper_code", paperCode);
        queryWrapper.eq("question_code", questionCode);
        queryWrapper.eq("status", GenericState.Valid.code);

        List<ExaminePaperItem> scoreList = examinePaperItemService.selectList(queryWrapper);

        if (null == scoreList || scoreList.isEmpty())
            return 0;

        ExaminePaperItem paperItem = scoreList.get(0);

        if (null == paperItem || null == paperItem.getScore())
            return 0;

        int score = 0;
        try {
            score = Integer.parseInt(paperItem.getScore());
        }catch(Exception e){}

        return score;
    }
}
