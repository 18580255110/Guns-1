package com.stylefeng.guns.task.education;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.core.admin.Administrator;
import com.stylefeng.guns.modular.adjustMGR.service.IAdjustStudentService;
import com.stylefeng.guns.modular.system.model.AdjustStudent;
import com.stylefeng.guns.modular.system.model.AdjustStudentApproveStateEnum;
import com.stylefeng.guns.modular.system.model.AdjustStudentTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/2/1 10:50
 * @Version 1.0
 */
public class AdjustTask {
    private static final Logger log = LoggerFactory.getLogger(AdjustTask.class);

    @Autowired
    private IAdjustStudentService adjustStudentService;

    @Scheduled(fixedDelay = 60000)
    public void handleAdjustApply(){
        Administrator administrator = new Administrator();
        administrator.setAccount("1");
        administrator.setId(1);
        administrator.setName("科萃教育");
        adjustStudentService.setAdministrator(administrator);
        Wrapper<AdjustStudent> queryWrapper = new EntityWrapper<AdjustStudent>()
                .eq("type", AdjustStudentTypeEnum.Adjust.code)
                .eq("status", GenericState.Valid.code)
                .eq("work_status", AdjustStudentApproveStateEnum.Create.code)
                ;
        List<AdjustStudent> adjustList = adjustStudentService.selectList(queryWrapper);


        log.info("Got <{}> adjust apply need to handle!", adjustList.size());
        for(AdjustStudent adjustApply : adjustList){
            AdjustStudentApproveStateEnum action = AdjustStudentApproveStateEnum.Refuse;
            String remark = "";
            if (adjustStudentService.canAdjust(adjustApply)){
                action = AdjustStudentApproveStateEnum.Appove;
                remark = "审核通过";
            }
            adjustStudentService.doAdjustApprove(adjustApply.getId(), action, remark);
        }
    }


    /**
     * @deprecated  直接由controller处理
     */
//    @Scheduled(fixedDelay = 60000)
    public void handleChangeApply(){
        Administrator administrator = new Administrator();
        administrator.setAccount("1");
        administrator.setId(1);
        administrator.setName("科萃教育");
        adjustStudentService.setAdministrator(administrator);
        Wrapper<AdjustStudent> queryWrapper = new EntityWrapper<AdjustStudent>()
                .eq("type", AdjustStudentTypeEnum.Change.code)
                .eq("status", GenericState.Valid.code)
                .eq("work_status", AdjustStudentApproveStateEnum.Create.code)
                ;
        List<AdjustStudent> adjustList = adjustStudentService.selectList(queryWrapper);

        log.info("Got <{}> adjust apply need to handle!", adjustList.size());
        for(AdjustStudent adjustApply : adjustList){
            AdjustStudentApproveStateEnum action = AdjustStudentApproveStateEnum.Refuse;
            String remark = "";
            if (adjustStudentService.canChange(adjustApply)){
                action = AdjustStudentApproveStateEnum.Appove;
                remark = "审核通过";
            }
            adjustStudentService.doChangeApprove(adjustApply.getId(), action, remark);
        }
    }
}
