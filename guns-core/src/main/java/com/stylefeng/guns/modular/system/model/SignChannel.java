package com.stylefeng.guns.modular.system.model;

/**
 * 报名渠道
 *
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/10/8 10:34
 * @Version 1.0
 */
public enum SignChannel {

    Admin (1, "管理平台"),
    App (2, "APP")
    ;

    public int code ;
    public String text;

    SignChannel(int code, String text){
        this.code = code;
        this.text = text;
    }
}
