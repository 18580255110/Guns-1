package com.stylefeng.guns.modular.system.model;

import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * <p>
 * 试题库
 * </p>
 *
 * @author stylefeng
 * @since 2018-12-22
 */
@TableName("tb_question")
@ApiModel(value = "Question", description = "试题")
public class Question extends Model<Question> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(hidden = true)
    private Long id;
    /**
     * 试题编码：ST + 8位序列码
     */
    @ApiModelProperty(value = "code", name = "试题编码", example = "ST00000001")
    private String code;
    /**
     * 试题题目： 题目以图片的方式存放在附件表
     */
    @ApiModelProperty(value = "question", name = "试题题目", example = "请选择一下说明正确的")
    private String question;
    /**
     * 试题类型： 1 单选题； 2 多选题； 3 填空题； 4 主观题
     */
    @ApiModelProperty(value = "type", name = "试题类型 : 1 单选题； 2 多选题； 3 填空题； 4 主观题", example = "1")
    private Integer type;
    /**
     * 试题学科：10数学 11语文 13化学
     */
    @ApiModelProperty(value = "subject", name = "学科", example = "5")
    private Integer subject;
    /**
     * 试题学科：10数学 11语文 13化学
     */
    @ApiModelProperty(value = "grade", name = "年级", example = "5")
    private String grade;
    /**
     * 状态
     */
    @ApiModelProperty(hidden = true)
    private Integer status;
    /**
     * 参考答案
     */
    @TableField("expact_answer")
    @ApiModelProperty(hidden = true)
    private String expactAnswer;
    /**
     * 是否自动批改
     */
    @TableField("auto_marking")
    @ApiModelProperty(hidden = true)
    private Integer autoMarking;

    @ApiModelProperty(value = "teacher", name = "出题老师编码", example = "JS000002")
    private String teacher;

    @TableField("teacher_name")
    @ApiModelProperty(value = "teacherName", name = "出题老师名称", example = "李明")
    private String teacherName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getSubject() {
        return subject;
    }

    public void setSubject(Integer subject) {
        this.subject = subject;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getExpactAnswer() {
        return expactAnswer;
    }

    public void setExpactAnswer(String expactAnswer) {
        this.expactAnswer = expactAnswer;
    }

    public Integer getAutoMarking() {
        return autoMarking;
    }

    public void setAutoMarking(Integer autoMarking) {
        this.autoMarking = autoMarking;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "Question{" +
        "id=" + id +
        ", code=" + code +
        ", question=" + question +
        ", type=" + type +
        ", subject=" + subject +
        ", grade=" + grade +
        ", status=" + status +
        ", expactAnswer=" + expactAnswer +
        ", autoMarking=" + autoMarking +
        ", teacher=" + teacher +
        ", teacherName=" + teacherName +
        "}";
    }
}
