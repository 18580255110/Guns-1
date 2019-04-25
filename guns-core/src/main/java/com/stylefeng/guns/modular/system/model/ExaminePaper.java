package com.stylefeng.guns.modular.system.model;

import com.baomidou.mybatisplus.enums.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * <p>
 * 试卷
 * </p>
 *
 * @author stylefeng
 * @since 2018-12-22
 */
@TableName("tb_examine_paper")
@ApiModel(value = "ExaminePaper", description = "试卷")
public class ExaminePaper extends Model<ExaminePaper> {

    private static final long serialVersionUID = 1L;

    /**
     * 标示
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(hidden = true)
    private Long id;
    /**
     * 试卷编码： SJ + 8位序列码
     */
    @ApiModelProperty(name = "code", value = "编码", position = 0, example = "SJ00000001")
    private String code;
    /**
     * 适应年级： 多年级以逗号分隔
     */
    @ApiModelProperty(name = "grades", value = "适应年纪", position = 1, example = "1,2")
    private String grades;
    /**
     * 学科
     */
    @ApiModelProperty(name = "subject", value = "适应学科", position = 2, example = "1")
    private String subject;

    /**
     * 题目数量
     */
    @ApiModelProperty(name = "count", value = "题目数量", position = 4, example = "20")
    private Integer count;

    /**
     * 总分数
     */
    @ApiModelProperty(name = "totalScore", value = "总分数", position = 6, example = "100")
    @TableField("total_score")
    private Integer totalScore;

    /**
     * 出题人
     */
    @ApiModelProperty(name = "teacher", value = "出题老师", position = 8, example = "LS000001")
    private String teacher;
    /**
     * 出题时间
     */
    @TableField("create_date")
    @ApiModelProperty(hidden = true)
    private Date createDate;
    /**
     * 状态： 0 无效  1 有效
     */
    @ApiModelProperty(hidden = true)
    private Integer status;


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

    public String getGrades() {
        return grades;
    }

    public void setGrades(String grades) {
        this.grades = grades;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "ExaminePaper{" +
        "id=" + id +
        ", code=" + code +
        ", grades=" + grades +
        ", subject=" + subject +
        ", count=" + count +
        ", totalScore=" + totalScore +
        ", teacher=" + teacher +
        ", createDate=" + createDate +
        ", status=" + status +
        "}";
    }
}
