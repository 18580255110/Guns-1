package com.stylefeng.guns;

import com.stylefeng.guns.modular.payMGR.sdk.SDKConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 *
 * @Modify 后台任务移动到kecui-task项目中了
 */
@SpringBootApplication
@EnableScheduling
public class GunsRestApplication {

    private final static Logger logger = LoggerFactory.getLogger(GunsRestApplication.class);

    public static void main(String[] args) {
        SDKConfig config = SDKConfig.getConfig();

        config.loadPropertiesFromPath(null); // 从ClassPath读取acp_sdk.properties

        SpringApplication.run(GunsRestApplication.class, args);
        logger.info("GunsRestApplication is success!");
    }

}
