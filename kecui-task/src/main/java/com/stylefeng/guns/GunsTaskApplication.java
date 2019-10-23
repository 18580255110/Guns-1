package com.stylefeng.guns;

import com.stylefeng.guns.task.batch.ClassImportTask;
import com.stylefeng.guns.task.batch.ScoreImportTask;
import com.stylefeng.guns.task.batch.SignImportTask;
import com.stylefeng.guns.task.cross.PreSignTask;
import com.stylefeng.guns.task.education.AdjustTask;
import com.stylefeng.guns.task.education.ExamineCheckTask;
import com.stylefeng.guns.task.education.SignPrivilegeTask;
import com.stylefeng.guns.task.order.OrderRecycleTask;
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
    public ScoreImportTask createScoreImportWorker(){
        return new ScoreImportTask();
    }

    @Bean
    @ConditionalOnProperty(prefix = "application.task.import-class", name = "enable", havingValue = "true")
    public ClassImportTask createClassImportWorker(){
        return new ClassImportTask();
    }

    @Bean
    @ConditionalOnProperty(prefix = "application.task.import-sign", name = "enable", havingValue = "true")
    public SignImportTask createSignImportWorker(){
        return new SignImportTask();
    }

    @Bean
    @ConditionalOnProperty(prefix = "application.task.import-score", name = "enable", havingValue = "true")
    public ScoreImportTask createScoreImport(){
        return new ScoreImportTask();
    }

    @Bean
    @ConditionalOnProperty(prefix = "application.task.examine-auto-check", name = "enable", havingValue = "true")
    public ExamineCheckTask createExamineAutoCheckWorker(){
        return new ExamineCheckTask();
    }

    @Bean
    @ConditionalOnProperty(prefix = "application.task.adjust", name = "enable", havingValue = "true")
    public AdjustTask createApproveWorker(){
        return new AdjustTask();
    }

    @Bean
    @ConditionalOnProperty(prefix = "application.task.sms-sender", name = "enable", havingValue = "true")
    public SmsSender createSmsSender(){
        return new SmsSender();
    }

    @Bean
    @ConditionalOnProperty(prefix = "application.task.order-recycle", name = "enable", havingValue = "true")
    public OrderRecycleTask createOrderRecycle(){
        return new OrderRecycleTask();
    }

    @Bean
    @ConditionalOnProperty(prefix = "application.task.sign-privilege", name = "enable", havingValue = "true")
    public SignPrivilegeTask createSignPrivilege(){
        return new SignPrivilegeTask();
    }

    @Bean
    @ConditionalOnProperty(prefix = "application.task.sign-prepare", name = "enable", havingValue = "true")
    public PreSignTask createPreSign(){
        return new PreSignTask();
    }

    public static void main(String[] args) {
        SpringApplication.run(GunsTaskApplication.class, args);
        logger.info("GunsTaskApplication is success!");
    }
}
