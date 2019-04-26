package com.stylefeng.guns.rest.modular.examine.responser;

import com.stylefeng.guns.modular.system.model.ExamineApply;
import com.stylefeng.guns.modular.system.model.ExaminePaper;
import com.stylefeng.guns.rest.core.Responser;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.beans.BeanUtils;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/2/20 01:43
 * @Version 1.0
 */
@ApiModel(value = "ExaminePaperDetail", description = "试卷详情")
public class ExaminePaperDetail extends ExaminePaper {

    @ApiModelProperty(name = "teacherName", value = "出题老师名称", position = 8, example = "李敏")
    private String teacherName;

    @ApiModelProperty(name = "applyId", value = "应用ID", position = 9, example = "1")
    private Long applyId;

    @ApiModelProperty(name = "ability", value = "班型", position = 9, example = "1")
    private Integer ability;

    @ApiModelProperty(name = "cycle", value = "学期", position = 10, example = "1")
    private Integer cycle;

    @ApiModelProperty(name = "examTime", value = "测试时间", position = 11, example = "20")
    private Integer examTime;

    @ApiModelProperty(name = "passScore", value = "及格分数", position = 12, example = "60")
    private Integer passScore;

    public Long getApplyId() {
        return applyId;
    }

    public void setApplyId(Long applyId) {
        this.applyId = applyId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public Integer getAbility() {
        return ability;
    }

    public void setAbility(Integer ability) {
        this.ability = ability;
    }

    public Integer getCycle() {
        return cycle;
    }

    public void setCycle(Integer cycle) {
        this.cycle = cycle;
    }

    public Integer getExamTime() {
        return examTime;
    }

    public void setExamTime(Integer examTime) {
        this.examTime = examTime;
    }

    public Integer getPassScore() {
        return passScore;
    }

    public void setPassScore(Integer passScore) {
        this.passScore = passScore;
    }

    public static ExaminePaperDetail me(ExaminePaper examinePaper, ExamineApply apply) {
        ExaminePaperDetail detail = new ExaminePaperDetail();

        BeanUtils.copyProperties(examinePaper, detail);

        detail.setApplyId(apply.getId());
        detail.setAbility(apply.getAbility());
        detail.setCycle(apply.getCycle());
        detail.setExamTime(apply.getExamTime());
        detail.setPassScore(apply.getPassScore());

        return detail;
    }
}
