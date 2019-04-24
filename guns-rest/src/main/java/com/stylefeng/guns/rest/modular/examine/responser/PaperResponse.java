package com.stylefeng.guns.rest.modular.examine.responser;

import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.modular.system.model.ClassAblilityEnum;
import com.stylefeng.guns.modular.system.model.ExaminePaper;
import com.stylefeng.guns.util.DateUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Collection;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2018/12/22 10:10
 * @Version 1.0
 */
@ApiModel(value = "PaperResponse", description = "试卷")
public class PaperResponse {
    @ApiModelProperty(name = "paperCode", value = "试卷编码", example = "SJ000001")
    private String paperCode;
    @ApiModelProperty(name = "className", value = "班级描述", example = "重庆2018寒假班小学三年级语文")
    private String className;
    @ApiModelProperty(name = "classAbility", value = "可报名班型", example = "敏学班、勤思班")
    private String classAbility;
    @ApiModelProperty(name = "examTime", value = "诊断时长，分钟", example = "40")
    private Integer examTime;
    @ApiModelProperty(name = "status", value = "状态： 0 从未测试； 1 正在答题； 2 暂停答题  3 已交卷  4 已批改", example = "1")
    private Integer status;
    @ApiModelProperty(name = "answerResponse", value = "历史答卷信息")
    private AnswerResponse answerResponse;
    @ApiModelProperty(name = "canTest", value = "能否开始诊断", example = "true")
    private boolean canTest;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassAbility() {
        return classAbility;
    }

    public void setClassAbility(String classAbility) {
        this.classAbility = classAbility;
    }

    public String getPaperCode() {
        return paperCode;
    }

    public void setPaperCode(String paperCode) {
        this.paperCode = paperCode;
    }

    public Integer getExamTime() {
        return examTime;
    }

    public void setExamTime(Integer examTime) {
        this.examTime = examTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public AnswerResponse getAnswerResponse() {
        return answerResponse;
    }

    public void setAnswerResponse(AnswerResponse answerResponse) {
        this.answerResponse = answerResponse;
    }

    public boolean isCanTest() {
        return canTest;
    }

    public void setCanTest(boolean canTest) {
        this.canTest = canTest;
    }

    public static PaperResponse me(ExaminePaper examinePaper, Collection<Class> classInfos) {
        PaperResponse response = new PaperResponse();

        response.setCanTest(true);

        String name = null;
        StringBuilder abilitiesDesp = new StringBuilder();

        if (null != classInfos) {
            for (Class classInfo : classInfos) {
                if (null == name) {
                    name = "重庆" + DateUtil.format(classInfo.getBeginDate(), "yyyy") + classInfo.getCourseName();
                }

                abilitiesDesp.append(ClassAblilityEnum.instanceOf(classInfo.getAbility()).text).append(",");
            }
        }

        response.setClassName(name);
        response.setClassAbility(abilitiesDesp.substring(0, abilitiesDesp.length() - 1));
        // TODO 0424 应该从PaperApply取得
        response.setExamTime(0);
        response.setPaperCode(examinePaper.getCode());
        return response;
    }
}
