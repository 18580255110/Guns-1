package com.stylefeng.guns.task.cross;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.stylefeng.guns.GunsTaskApplication;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.orderMGR.service.ICourseCartService;
import com.stylefeng.guns.modular.orderMGR.service.IOrderService;
import com.stylefeng.guns.modular.system.model.Class;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.bouncycastle.jcajce.provider.symmetric.AES;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 预报
 *
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/4/11 02:15
 * @Version 1.0
 */
@Component
public class PreSignTask {
    private final static Logger log = LoggerFactory.getLogger(PreSignTask.class);

    @Autowired
    private IClassService classService;

    @Autowired
    private ICourseCartService courseCartService;

    /**
     * 每天凌晨1点执行
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void presign(){
        Wrapper<Class> queryWrapper = new EntityWrapper<Class>();
        Date now = new Date();

        queryWrapper.eq("status", GenericState.Valid.code);
        queryWrapper.eq("crossable", GenericState.Valid.code);
        queryWrapper.eq("presign_status", GenericState.Invalid.code);
        queryWrapper.le("presign_start_date", now);

        List<Class> presignClassQueue = classService.selectList(queryWrapper);

        log.info("Got {} class need presign", presignClassQueue.size());

        for(Class classInfo : presignClassQueue){
            log.info("班级{} 开始预报", classInfo.getCode());
            try {
                courseCartService.doAutoPreSign(classInfo);
            }catch(Exception e){
                log.error("班级{}预报失败", classInfo.getCode(), e);
            }

            log.info("班级{}预报成功", classInfo.getCode());
            classInfo.setPresignStatus(GenericState.Valid.code);
            classService.updateById(classInfo);
        }
    }
}
