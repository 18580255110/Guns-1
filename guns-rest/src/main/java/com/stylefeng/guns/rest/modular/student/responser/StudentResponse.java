package com.stylefeng.guns.rest.modular.student.responser;

import com.stylefeng.guns.modular.system.model.Student;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/2/26 12:09
 * @Version 1.0
 */
@ApiModel(value = "StudentResponse", description = "学员")
public class StudentResponse extends Student {

    private String memberName;

    private String memberMobile;

    @ApiModelProperty(name = "changeOut", value = "是否转出", position = 0, example="true")
    private boolean changeOut;

    @ApiModelProperty(name = "changeIn", value = "是否转入", position = 0, example="true")
    private boolean changeIn;

    @ApiModelProperty(name = "adjustOut", value = "是否调出", position = 0, example="true")
    private boolean adjustOut;

    @ApiModelProperty(name = "adjustIn", value = "是否调入", position = 0, example="true")
    private boolean adjustIn;

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberMobile() {
        return memberMobile;
    }

    public void setMemberMobile(String memberMobile) {
        this.memberMobile = memberMobile;
    }

    public boolean isChangeOut() {
        return changeOut;
    }

    public void setChangeOut(boolean changeOut) {
        this.changeOut = changeOut;
    }

    public boolean isChangeIn() {
        return changeIn;
    }

    public void setChangeIn(boolean changeIn) {
        this.changeIn = changeIn;
    }

    public boolean isAdjustOut() {
        return adjustOut;
    }

    public void setAdjustOut(boolean adjustOut) {
        this.adjustOut = adjustOut;
    }

    public boolean isAdjustIn() {
        return adjustIn;
    }

    public void setAdjustIn(boolean adjustIn) {
        this.adjustIn = adjustIn;
    }
}
