package com.stylefeng.guns.rest.core;

import io.swagger.annotations.ApiModelProperty;

/**
 * 结果超类
 *
 * Created by 罗华.
 */
public abstract class SimpleResponser implements Responser {
    private static final long serialVersionUID = 732617853520141701L;

    protected static String SUCCEED = "000000";

    @ApiModelProperty(value = "返回码", example = "000000")
    private String code;

    @ApiModelProperty(value = "返回信息", example = "成功")
    private String message;

    public String getCode(){
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean isSuccess() {
        return SUCCEED.equals(this.code);
    }

    public static SimpleResponser success() {
        SimpleResponser response = new SimpleResponser(){
        };

        response.setCode(SUCCEED);
        response.setMessage("成功");
        return response;
    }


}
