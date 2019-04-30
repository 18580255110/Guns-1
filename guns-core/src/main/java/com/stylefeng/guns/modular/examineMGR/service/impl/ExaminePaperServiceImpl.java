package com.stylefeng.guns.modular.examineMGR.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.modular.examineMGR.service.IExamineAnswerService;
import com.stylefeng.guns.modular.examineMGR.service.IExaminePaperItemService;
import com.stylefeng.guns.modular.examineMGR.service.IExaminePaperService;
import com.stylefeng.guns.modular.system.dao.ExaminePaperMapper;
import com.stylefeng.guns.modular.system.model.ExaminePaper;
import com.stylefeng.guns.modular.system.model.ExaminePaperItem;
import com.stylefeng.guns.modular.system.model.Question;
import com.stylefeng.guns.util.CodeKit;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2018/12/22 18:13
 * @Version 1.0
 */
@Service
public class ExaminePaperServiceImpl extends ServiceImpl<ExaminePaperMapper, ExaminePaper> implements IExaminePaperService {

    @Autowired
    private IExaminePaperItemService examinePaperItemService;

    @Autowired
    private IExamineAnswerService examineAnswerService;

    @Override
    public ExaminePaper get(String paperCode) {
        if (null == paperCode)
            return null;

        Wrapper<ExaminePaper> queryWrapper = new EntityWrapper<ExaminePaper>();
        queryWrapper.eq("code", paperCode);

        return selectOne(queryWrapper);
    }

    @Override
    public void create(ExaminePaper paper, Set<ExaminePaperItem> workingQuestionList) {
        if (null == paper)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"试卷"});

        if (null == workingQuestionList || workingQuestionList.isEmpty())
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"试卷题目"});

        Date now = new Date();
        paper.setCode(CodeKit.generatePaper());
        paper.setCreateDate(now);
        paper.setStatus(GenericState.Valid.code);

        int totalScore = calcTotalScore(workingQuestionList);

        if (totalScore <= 0)
            throw new ServiceException(MessageConstant.MessageCode.SYS_DATA_ILLEGAL, new String[]{"分数必须为正整数，范围是 1 ～ 100"});

        paper.setCount(workingQuestionList.size());
        paper.setTotalScore(totalScore);
        insert(paper);

        refreshPaperQuestionItems(paper, workingQuestionList);
    }

    @Override
    public void doPause(String code) {
        if (null == code)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"试卷"});

        ExaminePaper paper = get(code);
        if (null == paper)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"试卷"});

//        if (examineAnswerService.paperOnair(paper))
//            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_ONAIR, new String[]{"试卷"});

        paper.setStatus(GenericState.Invalid.code);
        updateById(paper);
    }

    @Override
    public void doResume(String code) {
        if (null == code)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"试卷"});

        ExaminePaper paper = get(code);
        if (null == paper)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"试卷"});

        paper.setStatus(GenericState.Valid.code);
        updateById(paper);
    }

    @Override
    public void update(ExaminePaper paper, Set<ExaminePaperItem> workingQuestionList) {
        if (null == paper || null == paper.getCode())
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"试卷"});

        ExaminePaper existPaper = get(paper.getCode());
        if (null == existPaper)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"试卷"});

        if (examineAnswerService.paperOnair(paper))// 试卷已有答卷了，就不能修改
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_ONAIR, new String[]{"试卷"});

        Wrapper<ExaminePaperItem> paperItemWrapper = new EntityWrapper<ExaminePaperItem>();
        paperItemWrapper.eq("paper_code", paper.getCode());

        examinePaperItemService.delete(paperItemWrapper);

        String[] ignoreProperties = new String[]{"id", "code", "createDate"};
        BeanUtils.copyProperties(paper, existPaper, ignoreProperties);

        int totalScore = calcTotalScore(workingQuestionList);

        if (totalScore <= 0)
            throw new ServiceException(MessageConstant.MessageCode.SYS_DATA_ILLEGAL, new String[]{"分数必须为正整数，范围是 1 ～ 100"});

        existPaper.setCount(workingQuestionList.size());
        existPaper.setTotalScore(totalScore);

        updateById(existPaper);

        refreshPaperQuestionItems(existPaper, workingQuestionList);
    }

    @Override
    public void copy(String code) {
        if (null == code)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"试卷"});

        ExaminePaper paper = get(code);
        if (null == paper)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"试卷"});

        ExaminePaper paperCopy = new ExaminePaper();
        String[] ignoreProperties = new String[]{"id", "code", "createDate", "status"};
        BeanUtils.copyProperties(paper, paperCopy, ignoreProperties);

        Date now = new Date();
        paperCopy.setCode(CodeKit.generatePaper());
        paperCopy.setCreateDate(now);
        paperCopy.setStatus(GenericState.Valid.code);

        insert(paperCopy);

        // 复制试卷题目
        Wrapper<ExaminePaperItem> queryWrapper = new EntityWrapper<ExaminePaperItem>();
        queryWrapper.eq("paper_code", paper.getCode());
        queryWrapper.eq("status", GenericState.Valid.code);
        List<ExaminePaperItem> paperItemList = examinePaperItemService.selectList(queryWrapper);

        String[] itemIgnoreProperties = new String[]{"id", "paperCode", "status"};

        for(ExaminePaperItem paperItem : paperItemList){
            ExaminePaperItem paperItemCopy = new ExaminePaperItem();
            BeanUtils.copyProperties(paperItem, paperItemCopy, itemIgnoreProperties);
            paperItemCopy.setPaperCode(paperCopy.getCode());

            examinePaperItemService.create(paperItemCopy);
        }
    }

    private int calcTotalScore(Set<ExaminePaperItem> workingQuestionList) {
        int totalScore = 0;
        for(ExaminePaperItem paperItem : workingQuestionList){
            int score = 0;
            try{
                score = Integer.parseInt(paperItem.getScore());
            }catch(Exception e){}
            totalScore += score;
        }
        return totalScore;
    }

    private void refreshPaperQuestionItems(ExaminePaper paper, Set<ExaminePaperItem> workingQuestionList) {
        for(ExaminePaperItem paperItem : workingQuestionList){
            try {
                paperItem.setPaperCode(paper.getCode());
                examinePaperItemService.create(paperItem);
            }catch(Exception e){
                throw new ServiceException();
            }
        }
    }
}
