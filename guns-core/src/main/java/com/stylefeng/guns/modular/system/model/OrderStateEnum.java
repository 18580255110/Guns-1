package com.stylefeng.guns.modular.system.model;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2018/11/15 11:23
 * @Version 1.0
 */
public enum OrderStateEnum {
    PreCreate(0, "预生成"),
    Valid(1, "有效"),
    Reverse(2, "已退费"),
    InValid(-1, "失效"),
    Expire(-2, "过期失效")
    ;

    public int code;
    public String text;

    OrderStateEnum(int code, String text){
        this.code = code;
        this.text = text;
    }

}
