package com.stylefeng.guns.task.education;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.stylefeng.guns.modular.examineMGR.service.IExamineAnswerService;
import com.stylefeng.guns.modular.system.model.ExamineAnswer;
import com.stylefeng.guns.modular.system.model.ExamineAnswerStateEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;


/**
 * 自动号卷
 *
 * 在 GunsTaskApplication 中创建Bean
 *
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/2/24 16:47
 * @Version 1.0
 */
public class ExamineCheckTask {
    private static final Logger log = LoggerFactory.getLogger(ExamineCheckTask.class);

    @Autowired
    private IExamineAnswerService examineAnswerService;

    @Scheduled(fixedDelay = 5000)
    public void handleExamineCheck(){
        log.info("<<< Examine check begin ");
        //
        Wrapper<ExamineAnswer> queryWrapper = new EntityWrapper<ExamineAnswer>();
        queryWrapper.eq("status", ExamineAnswerStateEnum.Submit.code);

        List<ExamineAnswer> examineAnswerList = examineAnswerService.selectList(queryWrapper);

        log.info("Got {} examine answer", examineAnswerList.size());
        for(ExamineAnswer examineAnswer : examineAnswerList){
            examineAnswerService.doAutoCheckPaper(examineAnswer);
        }
        log.info(">>> Examine check task complete!");
    }
}
