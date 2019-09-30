package com.stylefeng.guns;

import com.stylefeng.guns.task.education.AdjustTask;
import com.stylefeng.guns.task.education.ExamineCheckTask;
import com.stylefeng.guns.task.pay.PayMocker;
import com.stylefeng.guns.task.sms.SmsSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GunsTaskApplication {

    private final static Logger logger = LoggerFactory.getLogger(GunsTaskApplication.class);

    @Bean
    public ExamineCheckTask createExamineAutoCheckWorker(){
        return new ExamineCheckTask();
    }

    @Bean
    @ConditionalOnProperty(prefix = "application.adjust.auto", name = "enable", havingValue = "true")
    public AdjustTask createApproveWorker(){
        return new AdjustTask();
    }

    @Bean
    @ConditionalOnProperty(prefix = "application.message.sms.sender", name = "enable", havingValue = "true")
    public SmsSender createSmsSender(){
        return new SmsSender();
    }

    @Bean
    @ConditionalOnProperty(prefix = "application.pay.mock", name = "enable", havingValue = "true")
    public PayMocker createPayMocker(){
        return new PayMocker();
    }

    public static void main(String[] args) {
        logger.info("test");
        SpringApplication.run(GunsTaskApplication.class, args);
        logger.info("GunsTaskApplication is success!");
    }
}
