package com.stylefeng.guns.modular.system.model;

/**
 * 报名类型
 *
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/10/8 10:25
 * @Version 1.0
 */
public enum SignType {

    Normal(11, "普通报名"),
    Inherit(12, "原班续报"),
    Cross(13, "原班跨报")
    ;

    public int code;
    public String text;

    SignType(int code, String text){
        this.code = code;
        this.text = text;
    }




}
