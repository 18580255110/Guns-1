package com.stylefeng.guns.rest.modular.education.controller;

import java.util.Date;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/11/27 22:18
 * @Version 1.0
 */
public class CrossWindow implements Comparable<CrossWindow> {

    private Date beginDate;

    private Date endDate;

    public CrossWindow(Date crossStartDate, Date crossEndDate) {
        this.beginDate = crossStartDate;
        this.endDate = crossEndDate;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public int compareTo(CrossWindow target) {
        if ( null == target )
            return 1;

        Date beginDate = target.getBeginDate();
        Date endDate = target.getEndDate();

        if (0 == this.beginDate.compareTo(beginDate)
                && 0 == this.endDate.compareTo(endDate)) {
            return 0;
        }else{
            return -1;
        }
    }
}
