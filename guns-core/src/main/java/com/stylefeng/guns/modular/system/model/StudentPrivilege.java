package com.stylefeng.guns.modular.system.model;

import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 学员报班权限
 *
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/10/8 09:09
 * @Version 1.0
 */
@TableName("tb_student_privilege")
@ApiModel(value = "StudentPrivilege", description = "学员报班权限")
public class StudentPrivilege extends Model<StudentPrivilege> {

    /**
     * 标示
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(hidden = true)
    private Long id;

    /**
     * 学员编码: XY+年月日（6位）+8位顺序码
     */
    @ApiModelProperty(name = "studentCode", value = "学员编码", position = 0, example="XY000001")
    @TableField("student_code")
    private String studentCode;

    /**
     * 学员名称
     */
    @ApiModelProperty(name = "studentName", value = "学员名称", position = 1, example="小明")
    @TableField("student_name")
    private String studentName;

    @ApiModelProperty(name = "academicYear", value = "学年", position = 2, example="2019")
    @TableField("academic_year")
    private Integer academicYear;

    @ApiModelProperty(name = "grade", value = "年级", position = 3, example="3")
    private Integer grade;

    @ApiModelProperty(name = "cycle", value = "学期", position = 4, example="1")
    private Integer cycle;

    @ApiModelProperty(name = "ability", value = "学员名称", position = 5, example="2")
    private Integer ability;

    @ApiModelProperty(name = "type", value = "类型", position = 6, example="1")
    private Integer type;

    @ApiModelProperty(name = "status", value = "状态", position = 7, example="1")
    private Integer status;

    @ApiModelProperty(name = "comments", value = "备注", position = 8, example="备注")
    private String comments;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Integer getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(Integer academicYear) {
        this.academicYear = academicYear;
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

    public void setCycle(Integer cycle) {
        this.cycle = cycle;
    }

    public Integer getAbility() {
        return ability;
    }

    public void setAbility(Integer ability) {
        this.ability = ability;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "StudentPrivilege{" +
                "id=" + id +
                ", studentCode='" + studentCode + '\'' +
                ", studentName='" + studentName + '\'' +
                ", academicYear=" + academicYear +
                ", grade=" + grade +
                ", cycle=" + cycle +
                ", ability=" + ability +
                ", type=" + type +
                ", status=" + status +
                ", comments='" + comments + '\'' +
                '}';
    }

    public String getKey() {
        return "_" + studentCode + "_" + academicYear + "_" + grade + "_" + cycle + "_" + ability + "_";
    }

    public StudentPrivilege next() {
        CircularCalculator calculator = new CircularCalculator(this.academicYear);

        calculator.next(this.cycle);

        this.academicYear = calculator.getAcademicYear();
        this.cycle = calculator.getCycle();

        return this;
    }

    class CircularCalculator {
        private ConcurrentLinkedQueue<Integer> list;
        private Iterator<Integer> iterator;
        private Integer academicYear;

        public CircularCalculator (int academicYear){
            list.add(1); // 春
            list.add(2); // 暑
            list.add(3); // 秋
            list.add(4); // 寒

            this.iterator = list.iterator();
            this.academicYear = academicYear;
        }

        public void next(int cycle){
            while(iterator.hasNext()){
                int val = iterator.next();
                if (val != cycle)
                    continue;

                if (!(iterator.hasNext())){
                    // 最后一个学期了
                    iterator = list.iterator();
                    this.academicYear ++;
                }
            }
        }

        public int getAcademicYear(){
            return this.academicYear;
        }

        public int getCycle(){
            return this.iterator.next();
        }
    }
}
