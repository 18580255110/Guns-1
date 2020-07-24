package com.stylefeng.guns.rest.core.exception.handler;

import com.stylefeng.guns.common.exception.IndependenceException;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.aop.BaseControllerExceptionHandler;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.rest.core.Responser;
import com.stylefeng.guns.rest.core.exception.ServiceExceptionResponser;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Locale;

/**
 * 全局的的异常拦截器（拦截所有的控制器）（带有@RequestMapping注解的方法上都会拦截）
 *
 * @author 罗华
 * @date 2016年11月12日 下午3:19:56
 */
@ControllerAdvice
public class GlobalExceptionHandler extends BaseControllerExceptionHandler {

    private Logger log = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private MessageSource messageSource;

    /**
     * 拦截jwt相关异常
     */
    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Responser jwtException(JwtException e) {
//        return new ErrorTip(BussExceptionEnum.TOKEN_ERROR.getCode(), BussExceptionEnum.TOKEN_ERROR.getMessage());
        ServiceExceptionResponser responser = new ServiceExceptionResponser();

        responser.setCode(MessageConstant.MessageCode.SYS_TOKEN_ERROR);
        String message = messageSource.getMessage("exception." + MessageConstant.MessageCode.SYS_TOKEN_ERROR, new Object[0], Locale.CHINA);
        responser.setMessage(message);

        log.error(message);
        return responser;
    }

    /**
     * 拦截ServiceException相关异常
     */
    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Responser serviceException(ServiceException e) {

        ServiceExceptionResponser responser = new ServiceExceptionResponser();

        responser.setCode(e.getMessageCode());
        String message = messageSource.getMessage("exception." + e.getMessageCode(), e.getMessageArgs(), Locale.CHINA);
        responser.setMessage(message);

        log.error(message, e);

        return responser;
    }


    /**
     * 拦截ServiceException相关异常
     */
    @ExceptionHandler(IndependenceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String independenceException(IndependenceException e) {

        ServiceExceptionResponser responser = new ServiceExceptionResponser();

        responser.setCode(e.getMessageCode());
        String message = messageSource.getMessage("exception." + e.getMessageCode(), e.getMessageArgs(), Locale.CHINA);
        responser.setMessage(message);

        log.error(message, e);

        return message;
    }

    /**
     * 拦截ServiceException相关异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Responser constraintViolationExceptionException(MethodArgumentNotValidException e) {

        ServiceExceptionResponser responser = new ServiceExceptionResponser();

        responser.setCode(MessageConstant.MessageCode.SYS_EXCEPTION);

        BindingResult result = e.getBindingResult();

        StringBuffer message = new StringBuffer();
        for(ObjectError error : result.getAllErrors()){
            message.append(error.getDefaultMessage());
        }

        log.error(message.toString());
        responser.setMessage(message.toString());

        return responser;
    }

}
