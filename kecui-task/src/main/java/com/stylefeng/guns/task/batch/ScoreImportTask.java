package com.stylefeng.guns.task.batch;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.modular.batchMGR.service.IBatchProcessDetailService;
import com.stylefeng.guns.modular.batchMGR.service.IBatchProcessService;
import com.stylefeng.guns.modular.memberMGR.service.IScoreService;
import com.stylefeng.guns.modular.system.model.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 * 批量成绩导入
 *
 * 在 GunsTaskApplication 中创建Bean
 *
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/3/25 22:00
 * @Version 1.0
 */
public class ScoreImportTask extends ImportTaskSupport {
    private static final Logger log = LoggerFactory.getLogger(ScoreImportTask.class);
    private static final int MIN_COLUMN_SIZE = 8;
    @Autowired
    private IBatchProcessService batchProcessService;

    @Autowired
    private IBatchProcessDetailService batchProcessDetailService;

    @Autowired
    private IScoreService scoreService;

    @Scheduled(fixedDelay = 60000)
    public void handleExamineCheck(){
        log.info("<<< Examine check begin ");
        //
        Wrapper<BatchProcess> queryWrapper = new EntityWrapper<BatchProcess>();
        queryWrapper.eq("status", GenericState.Valid.code);
        queryWrapper.eq("service", BatchServiceEnum.Score.code);
        queryWrapper.eq("work_status", BatchProcessStatusEnum.Prepare.code);

        BatchProcess preparedProcess = batchProcessService.selectOne(queryWrapper);

        if (null == preparedProcess) {
            log.warn("没有需处理的任务");
            return;
        }
        Wrapper<BatchProcessDetail> detailQueryWrapper = new EntityWrapper<BatchProcessDetail>();
        detailQueryWrapper.eq("batch_code", preparedProcess.getCode());
        detailQueryWrapper.eq("status", GenericState.Valid.code);
        detailQueryWrapper.eq("work_status", BatchProcessDetailStatusEnum.Create.code);

        List<BatchProcessDetail> processDetailList = batchProcessDetailService.selectList(detailQueryWrapper);

        if (null == processDetailList || processDetailList.isEmpty()){
            log.warn("批处理任务{}没有需要处理的数据", preparedProcess.getCode());
            preparedProcess.setWorkStatus(BatchProcessStatusEnum.Pass.code);
            batchProcessService.updateById(preparedProcess);
            return;
        }

        preparedProcess.setWorkStatus(BatchProcessStatusEnum.Working.code);
        batchProcessService.updateById(preparedProcess);
        int successCompleteCount = 0;

        for (BatchProcessDetail processDetail : processDetailList){
            Date handleBeginDate  = new Date();
            processDetail.setWorkStatus(BatchProcessDetailStatusEnum.Working.code);
            batchProcessDetailService.updateById(processDetail);

            Score score = assembleScoreData(processDetail.getData());
            try {
                scoreService.insert(score);
                processDetail.setWorkStatus(BatchProcessDetailStatusEnum.Pass.code);
                processDetail.setRemark(BatchProcessDetailStatusEnum.Pass.text);
                successCompleteCount++;
            }catch(Exception e){
                processDetail.setWorkStatus(BatchProcessDetailStatusEnum.Reject.code);
                processDetail.setRemark(e.getMessage());
            }finally{
                Date handleEndDate = new Date();
                processDetail.setDuration(handleEndDate.getTime() - handleBeginDate.getTime());
                processDetail.setCompleteDate(handleEndDate);
            }
            batchProcessDetailService.updateById(processDetail);
        }

        preparedProcess.setWorkStatus(BatchProcessStatusEnum.Pass.code);
        preparedProcess.setCompleteCount(successCompleteCount);
        preparedProcess.setCompleteDate(new Date());
        batchProcessService.updateById(preparedProcess);
        log.info(">>> Import score task complete!");
    }

    private Score assembleScoreData(String data) {
        if (null == data || StringUtils.isEmpty(data))
            throw new RuntimeException("没有导入数据");

        if (data.indexOf("@_@") == -1 )
            throw new RuntimeException("导入数据无效");

        String[] datas = data.split("@_@");

        String[] requiredCols = datas[0].split(",");
        if (MIN_COLUMN_SIZE > requiredCols.length)
            throw new RuntimeException("缺少必要的导入数据");

        Score score = new Score();
        score.setStudentCode(getString(requiredCols, 0));
        score.setStudent(getString(requiredCols, 1));
        score.setMobileNumber(getString(requiredCols, 2));
        score.setExamineName(getString(requiredCols, 3));
        score.setRound(getString(requiredCols, 4));
        score.setScore(getDouble(requiredCols, 5));
        score.setTotalScore(getDouble(requiredCols, 6));
        score.setRank(getInteger(requiredCols, 7));
        score.setRemark(getString(requiredCols, 8));
        score.setImportDate(new Date());

        if (datas.length > 1 && StringUtils.isNotEmpty(datas[1])){
            String[] classInfo = assembleClassInfo(datas[1]);
            score.setClassCodes(classInfo[0]);
            score.setClassNames(classInfo[1]);
        }

        return score;
    }

    private String[] assembleClassInfo(String classInfoStr) {
        StringTokenizer classInfoToken = new StringTokenizer(classInfoStr, ",");

        StringBuffer codeBuff = new StringBuffer();
        StringBuffer nameBuff = new StringBuffer();

        while(classInfoToken.hasMoreTokens()){
            String classInfo = classInfoToken.nextToken();
            if (classInfo.indexOf("=") != -1){
                String[] infos = classInfo.split("=");
                codeBuff.append(infos[0]).append(",");
                nameBuff.append(infos[1]).append(",");
            }
        }

        return new String[]{
                codeBuff.substring(0, codeBuff.length() - 1),
                nameBuff.substring(0, nameBuff.length() - 1)
        };
    }
}
