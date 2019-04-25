package com.stylefeng.guns.common.enums;

/**
 * @author: simple.song
 * Date: 2018/11/17 Time: 20:51
 */
public enum StatusEnum {
    STATUS_VALID(1,"启用"),
    STATUS_INVALID(2,"禁用"),
    STATUS_DEL(3,"删除"),
    ;
    private Integer code;
    private String msg;


    StatusEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }


    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}