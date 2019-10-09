package com.stylefeng.guns.rest.modular.message.controller;

import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.modular.system.service.ISmsSequenceService;
import com.stylefeng.guns.rest.core.Responser;
import com.stylefeng.guns.rest.core.SimpleResponser;
import com.stylefeng.guns.rest.core.SucceedResponser;
import com.stylefeng.guns.util.RegexUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by 罗华.
 */
@RestController
@RequestMapping("/message")
@Api(value = "MessageController", tags = "消息接口")
public class MessageController {

    @Autowired
    private ISmsSequenceService smsSequenceService;

    @RequestMapping(value = "/captcha/send", method = {RequestMethod.POST})
    @ApiOperation(value="发送验证码", httpMethod = "POST", response = SimpleResponser.class)
    @ApiImplicitParam(name = "number", value = "目标号码", required = true, dataType = "String", example = "18580255110")
    public Responser sendCaptcha(String number){

        if (null == number)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS);

        if (!RegexUtil.isMobile(number))
            throw new ServiceException(MessageConstant.MessageCode.SYS_DATA_ILLEGAL);

        Long queueId = smsSequenceService.addSmsCaptchSequence(number);
        return SucceedResponser.response(queueId);
    }
}
