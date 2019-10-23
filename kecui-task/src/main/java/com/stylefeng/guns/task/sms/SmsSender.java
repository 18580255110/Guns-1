package com.stylefeng.guns.task.sms;

import com.aliyuncs.exceptions.ClientException;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.stylefeng.guns.core.support.SmsKit;
import com.stylefeng.guns.modular.system.model.Dict;
import com.stylefeng.guns.modular.system.model.SmsSequence;
import com.stylefeng.guns.modular.system.service.IDictService;
import com.stylefeng.guns.modular.system.service.ISmsSequenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;

/**
 * 短信发送
 *
 * 在 GunsTaskApplication 中创建Bean
 *
 * @Description //TODO
 * @Author 罗华
 * @Date 2018/11/12 17:10
 * @Version 1.0
 */
public class SmsSender {
    private static final Logger log = LoggerFactory.getLogger(SmsSender.class);

    @Autowired
    private IDictService dictService;

    @Autowired
    private ISmsSequenceService smsSequenceService;

    @Scheduled(fixedDelay = 5000)
    public void sendCaptchaMessage(){
        List<String> orderBy = new ArrayList<String>();
        orderBy.add("create_date");
        Wrapper<SmsSequence> queryWrapper = new EntityWrapper<SmsSequence>()
                .eq("status", 1)
                .eq("sent_status", 0)
                .orderAsc(orderBy)
                ;
        List<SmsSequence> queue = smsSequenceService.selectList(queryWrapper);

        log.info("Got <{}> sms sequence need to send!", queue.size());
        for(SmsSequence sequence : queue){
            sendSms(sequence);
        }
    }

    private void sendSms(SmsSequence sequence) {
        List<Dict> dictList = dictService.selectByParentCode("sms_send_parameter");

        Map<String, Object> properties = new HashMap<String, Object>();
        for(Dict dict : dictList){
            properties.put(dict.getName(), dict.getCode());
        }

        SmsKit sender = SmsKit.me().configProperties(properties);
        if (null != sequence.getBussId())
            sender.setBussId(sequence.getBussId());

        sender.setSmsTemplate(sequence.getTemplate());

        try {
            sender.sendSms(sequence.getNumber(), sequence.getContent());
            if (sender.sendOK()){
                sequence.setOutRequestId(sender.getOutRequestId());
                sequence.setOutResponseId(sender.getOutResponseId());
                sequence.setSentStatus(1);
                sequence.setSentResult("OK");
                sequence.setSentDate(new Date());
                sequence.setSentResultMessage("发送成功");
            }else{
                sequence.setSentStatus(2);
                sequence.setSentResult(sender.getResponseCode());
                sequence.setSentResultMessage(sender.getResponseMessage());
            }
            smsSequenceService.updateSentResult(sequence);
        } catch (ClientException e) {
            log.info("<{}> message send error!", e);
        }

        log.info("<{}> message send complete!", sequence.getNumber());
    }
}
