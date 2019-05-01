package com.stylefeng.guns.rest.modular.examine.responser;

import com.stylefeng.guns.rest.core.SimpleResponser;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/2/18 00:38
 * @Version 1.0
 */
public class AnswerResponse extends SimpleResponser {

    private Integer score;

    private Integer examineTime;

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getExamineTime() {
        return examineTime;
    }

    public void setExamineTime(Integer examineTime) {
        this.examineTime = examineTime;
    }
}
