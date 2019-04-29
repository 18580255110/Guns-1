package com.stylefeng.guns.modular.member;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2018/11/24 14:51
 * @Version 1.0
 */
public enum MemberStarEnum {
    Star_0 (0, "游客"),
    Star_1 (1, "一星会员"),
    Star_2 (2, "二星会员"),
    Star_3 (3, "三星会员"),
    Star_99 (99, "老师会员")
    ;

    public int code;
    public String text;

    MemberStarEnum(int code, String text){
        this.code = code;
        this.text = text;
    }
}
