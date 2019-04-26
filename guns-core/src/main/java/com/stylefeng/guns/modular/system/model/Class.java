package com.stylefeng.guns.modular.system.model;

import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import com.stylefeng.guns.common.constant.state.GenericState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 班级
 * </p>
 *
 * @author simple
 * @since 2018-12-09
 */
@TableName("tb_class")
@ApiModel(value = "Class", description = "班级")
public class Class extends Model<Class> {

    private static final long serialVersionUID = 1L;

    /**
     * 标示
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(hidden = true)
    private Long id;
    /**
     * 班级编码： BJ+6位序列码
     */
    @ApiModelProperty(name = "code", value = "编码", position = 0, example="BJ000001")
    private String code;
    /**
     * 班级名称
     */
    @ApiModelProperty(name = "name", value = "班级名称", position = 1, example="小学一年级数据入门班")
    private String name;
    /**
     * 年级 0 学年前 1 ~ 12 小学一年级 ~ 高三
     */
    @ApiModelProperty(name = "grade", value = "年级", position = 2, example="1")
    private Integer grade;

    @TableField("academic_year")
    @ApiModelProperty(name = "academicYear", value = "学年", position = 3, example = "2019")
    private Integer academicYear;

    /**
     * 学期 1 春季班； 2 秋季班； 3 寒假班； 4 短期班；5 暑假班 99 活动类
     */
    @ApiModelProperty(name = "cycle", value = "学期", position = 4, example="2")
    private Integer cycle;

    /**
     * 班次 1 启航； 2 敏学； 3 勤思； 4 创新； 5 诊断； 99 其他
     */
    @ApiModelProperty(name = "ability", value = "班次", position = 5, example="4")
    private Integer ability;
    /**
     * 开课起始日期
     */
    @TableField("begin_date")
    @ApiModelProperty(name = "beginDate", value = "开课起始日期", position = 6, example="2018-11-30")
    private Date beginDate;
    /**
     * 开课结束日期
     */
    @TableField("end_date")
    @ApiModelProperty(name = "beginDate", value = "开课结束日期", position = 7, example="2018-12-30")
    private Date endDate;
    /**
     * 开课时间描述
     */
    @TableField("study_time_desp")
    @ApiModelProperty(name = "beginDate", value = "上课事件描述", position = 8, example="每周六 上午10：30 ～ 11：30")
    private String studyTimeDesp;
    /**
     * 跨报开始时间
     */
    @TableField("cross_start_date")
    @ApiModelProperty(name = "crossStartDate", value = "跨报开始时间", position = 9, example="2019-02-01")
    private Date crossStartDate;
    /**
     * 跨报结束时间
     */
    @TableField("cross_end_date")
    @ApiModelProperty(name = "crossEndDate", value = "跨报结束时间", position = 10, example="2019-02-05")
    private Date crossEndDate;
    /**
     * 单位：分钟
     */
    private Integer duration;
    /**
     * 总课时数
     */
    private Integer period;
    /**
     * 教室编码
     */
    @TableField("class_room_code")
    private String classRoomCode;
    /**
     * 教室
     */
    @TableField("class_room")
    private String classRoom;
    /**
     * 教授课程
     */
    @TableField("course_code")
    private String courseCode;
    /**
     * 课程名称
     */
    @TableField("course_name")
    private String courseName;
    /**
     * 关注度
     */
    private Integer star;
    /**
     * 价格： 单位： 分
     */
    private Long price;
    /**
     * 开班人数
     */
    private Integer quato;

    /**
     * 报名开始日期
     */
    @TableField("sign_start_date")
    private Date signStartDate;
    /**
     * 报名截止日期
     */
    @TableField("sign_end_date")
    private Date signEndDate;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 主讲教师编码
     */
    @TableField("teacher_code")
    private String teacherCode;
    /**
     * 主讲教师名称
     */
    private String teacher;
    /**
     * 辅导教师编码
     */
    @TableField("teacher_second_code")
    private String teacherSecondCode;

    /**
     * 辅导教师名称
     */
    @TableField("teacher_second")
    private String teacherSecond;

    /**
     * 是否开放报名
     */
    private Integer signable;
    /**
     * 是否需要入学测试
     */
    private Integer examinable;

    /**
     * 是否允许跨报
     */
    private Integer crossable;

    @TableField("presign_source_class_code")
    @ApiModelProperty(name = "presignSourceClassCode", value = "续报班级编码", position = 11, example="BJ000001")
    private String presignSourceClassCode;
    /**
     * 预报开始时间
     */
    @TableField("presign_start_date")
    @ApiModelProperty(name = "presignStartDate", value = "预报开始时间", position = 12, example="2019-02-01")
    private Date presignStartDate;
    /**
     * 预报结束时间
     */
    @TableField("presign_end_date")
    @ApiModelProperty(name = "presignEndDate", value = "预报结束时间", position = 13, example="2019-02-05")
    private Date presignEndDate;

    /**
     * 预报状态
     * 0 未预报
     * 1 已预报
     */
    @TableField("presign_status")
    private Integer presignStatus = 0;

    @TableField("create_date")
    private Date createDate;

    private transient Integer signQuato;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public Integer getCycle() {
        return cycle;
    }

    public Integer getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(Integer academicYear) {
        this.academicYear = academicYear;
    }

    public void setCycle(Integer cycle) {
        this.cycle = cycle;
    }

    public void setCycle(String cycle) {
        if (null == cycle)
            return;

        try{
            this.cycle = Integer.parseInt(cycle);
        }catch(Exception e){}
    }

    public Integer getAbility() {
        return ability;
    }

    public void setAbility(Integer ability) {
        this.ability = ability;
    }

    public void setAbility(String ability) {
        if (null == ability)
            return;

        try{
            this.ability = Integer.parseInt(ability);
        }catch(Exception e){}
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

    public String getStudyTimeDesp() {
        return studyTimeDesp;
    }

    public void setStudyTimeDesp(String studyTimeDesp) {
        this.studyTimeDesp = studyTimeDesp;
    }

    public Date getCrossStartDate() {
        return crossStartDate;
    }

    public void setCrossStartDate(Date crossStartDate) {
        this.crossStartDate = crossStartDate;
    }

    public Date getCrossEndDate() {
        return crossEndDate;
    }

    public void setCrossEndDate(Date crossEndDate) {
        this.crossEndDate = crossEndDate;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public String getClassRoomCode() {
        return classRoomCode;
    }

    public void setClassRoomCode(String classRoomCode) {
        this.classRoomCode = classRoomCode;
    }

    public String getClassRoom() {
        return classRoom;
    }

    public void setClassRoom(String classRoom) {
        this.classRoom = classRoom;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Integer getStar() {
        return star;
    }

    public void setStar(Integer star) {
        this.star = star;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Integer getQuato() {
        return quato;
    }

    public void setQuato(Integer quato) {
        this.quato = quato;
    }

    public Date getSignStartDate() {
        return signStartDate;
    }

    public void setSignStartDate(Date signStartDate) {
        this.signStartDate = signStartDate;
    }

    public Date getSignEndDate() {
        return signEndDate;
    }

    public void setSignEndDate(Date signEndDate) {
        this.signEndDate = signEndDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getTeacherCode() {
        return teacherCode;
    }

    public void setTeacherCode(String teacherCode) {
        this.teacherCode = teacherCode;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getTeacherSecondCode() {
        return teacherSecondCode;
    }

    public void setTeacherSecondCode(String teacherSecondCode) {
        this.teacherSecondCode = teacherSecondCode;
    }

    public String getTeacherSecond() {
        return teacherSecond;
    }

    public void setTeacherSecond(String teacherSecond) {
        this.teacherSecond = teacherSecond;
    }

    public Integer getSignable() {
        return signable;
    }

    public void setSignable(Integer signable) {
        this.signable = signable;
    }

    public Integer getExaminable() {
        return examinable;
    }

    public void setExaminable(Integer examinable) {
        this.examinable = examinable;
    }

    public Integer getCrossable() {
        return crossable;
    }

    public void setCrossable(Integer crossable) {
        this.crossable = crossable;
    }

    public String getPresignSourceClassCode() {
        return presignSourceClassCode;
    }

    public void setPresignSourceClassCode(String presignSourceClassCode) {
        this.presignSourceClassCode = presignSourceClassCode;
    }

    public Date getPresignStartDate() {
        return presignStartDate;
    }

    public void setPresignStartDate(Date presignStartDate) {
        this.presignStartDate = presignStartDate;
    }

    public Date getPresignEndDate() {
        return presignEndDate;
    }

    public void setPresignEndDate(Date presignEndDate) {
        this.presignEndDate = presignEndDate;
    }

    public Integer getPresignStatus() {
        return presignStatus;
    }

    public void setPresignStatus(Integer presignStatus) {
        this.presignStatus = presignStatus;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Integer getSignQuato() {
        return signQuato;
    }

    public void setSignQuato(Integer signQuato) {
        this.signQuato = signQuato;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "Class{" +
        "id=" + id +
        ", code=" + code +
        ", name=" + name +
        ", grade=" + grade +
        ", academicYear=" + academicYear +
        ", cycle=" + cycle +
        ", ability=" + ability +
        ", beginDate=" + beginDate +
        ", endDate=" + endDate +
        ", studyTimeDesp=" + studyTimeDesp +
        ", crossStartDate=" + crossStartDate +
        ", crossEndDate=" + crossEndDate +
        ", duration=" + duration +
        ", period=" + period +
        ", classRoomCode=" + classRoomCode +
        ", classRoom=" + classRoom +
        ", courseCode=" + courseCode +
        ", courseName=" + courseName +
        ", star=" + star +
        ", price=" + price +
        ", quato=" + quato +
        ", signStartDate=" + signStartDate +
        ", signEndDate=" + signEndDate +
        ", status=" + status +
        ", teacherCode=" + teacherCode +
        ", teacher=" + teacher +
        ", teacherSecondCode=" + teacherSecondCode +
        ", teacherSecond=" + teacherSecond +
        ", signable=" + signable +
        ", examinable=" + examinable +
        ", crossable=" + crossable +
        ", presignSourceClassCode=" + presignSourceClassCode +
        ", presignStartDate=" + presignStartDate +
        ", presignEndDate=" + presignEndDate +
        ", presignStatus=" + presignStatus +
        ", createDate=" + createDate +
        "}";
    }

    public boolean isValid() {
        if (null == this.status)
            return false;

        return GenericState.Valid.code == this.status;
    }

    public boolean needTest(){
        if (null == this.status)
            return false;

        return true;
    }
}
