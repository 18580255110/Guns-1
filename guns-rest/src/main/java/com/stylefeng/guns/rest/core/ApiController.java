package com.stylefeng.guns.rest.core;

import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.modular.memberMGR.service.IMemberService;
import com.stylefeng.guns.modular.system.model.Member;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2018/12/17 1:30
 * @Version 1.0
 */
public abstract class ApiController {
    private static final String USER_NAME = "USER_NAME";
    private static final String CURR_TOKEN = "CURR_TOKEN";
    @Autowired
    private HttpServletRequest currRequest;

    @Autowired
    private IMemberService _memberService;

    protected String currToken(){
        return (String) currRequest.getAttribute(CURR_TOKEN);
    }

    protected Member currMember(){
        String userName = (String) currRequest.getAttribute(USER_NAME);

        if (null == userName){
            throw new ServiceException(MessageConstant.MessageCode.SYS_CREDENTIAL_UNKNOW);
        }

        Member member = _memberService.get(userName);

        if (null == member){
            throw new ServiceException(MessageConstant.MessageCode.SYS_CREDENTIAL_UNKNOW);
        }

        return member;
    }
}
