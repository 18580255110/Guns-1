package com.stylefeng.guns.modular.education.transfer;

import com.stylefeng.guns.modular.system.model.Student;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/10/9 11:06
 * @Version 1.0
 */
public class StudentClassInfo extends Student {

    private boolean changeOut;

    private boolean changeIn;

    private boolean adjustOut;

    private boolean adjustIn;

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
