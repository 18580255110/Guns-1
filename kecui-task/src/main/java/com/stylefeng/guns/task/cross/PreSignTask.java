package com.stylefeng.guns.task.cross;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.orderMGR.service.ICourseCartService;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;
import java.util.List;

/**
 * 预报
 *
 * 在 GunsTaskApplication 中创建Bean
 *
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/4/11 02:15
 * @Version 1.0
 */
public class PreSignTask {
    private final static Logger log = LoggerFactory.getLogger(PreSignTask.class);

    @Autowired
    private IClassService classService;

    @Autowired
    private ICourseCartService courseCartService;

    /**
     * 每天没1点执行
     */
    @Scheduled(cron = "0 0 10-18 * * ?")
    public void presign(){
        Wrapper<Class> queryWrapper = new EntityWrapper<Class>();
        Date now = new Date();

        queryWrapper.eq("status", GenericState.Valid.code);
        queryWrapper.eq("crossable", GenericState.Valid.code);
        queryWrapper.eq("presign_status", GenericState.Invalid.code);
        queryWrapper.le("presign_start_date", DateUtil.format(now, "yyyy-MM-dd"));

        List<Class> presignClassQueue = classService.selectList(queryWrapper);

        log.info("Got {} class need presign", presignClassQueue.size());

        for(Class classInfo : presignClassQueue){
            log.info("班级{} 开始预报, 原班级 {}", classInfo.getCode(), classInfo.getPresignSourceClassCode());
            try {
                courseCartService.doAutoPreSign(classInfo);

                classInfo.setPresignStatus(GenericState.Valid.code);
                classService.updateById(classInfo);
                log.info("班级{}预报完毕", classInfo.getCode());
            }catch(Exception e){
                log.error("班级{}预报失败", classInfo.getCode(), e);
            }
        }
    }
}
