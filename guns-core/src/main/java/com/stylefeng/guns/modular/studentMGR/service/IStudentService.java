package com.stylefeng.guns.modular.studentMGR.service;

import com.baomidou.mybatisplus.service.IService;
import com.stylefeng.guns.modular.system.model.ScheduleStudent;
import com.stylefeng.guns.modular.system.model.Student;

import javax.validation.constraints.NotBlank;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 学生表 服务类
 * </p>
 *
 * @author simple.song
 * @since 2018-10-07
 */
public interface IStudentService extends IService<Student> {


    /** 获取map*/
    Map<String,Object> getMap(Integer id);

    /**
     *  获取Student类
     * @return
     */
    Student getOne(Student student);

    /**
     * 为会员添加学员
     *
     * @param userName
     * @param student
     * @return
     */
    Student addStudent(String userName, Student student);
    /**
     * 为会员添加学员
     *
     * @param userName
     * @param stringObjectMap
     * @return
     */
    Student addStudent(String userName, Map<String, Object> stringObjectMap);

    /**
     * 获取学员头像查看url
     *
     * @param avatorId
     * @return
     */
    String getAvatarViewUrl(Long avatorId);

    /**
     * 更新学员信息
     *
     * @param code
     * @param newStudent
     */
    void updateStudent(String code, Student newStudent);

    Student get(@NotBlank(message = "学员不能为空") String student);

    /**
     * 根据用户查找学员
     *
     * @param userName
     * @return
     */
    List<Student> listStudents( String userName );

    /**
     * 查询课程计划
     *
     * 查询学员在指定天的课程计划
     * @param student
     * @param day
     * @param ints
     * @return
     */
    List<ScheduleStudent> listCoursePlan(String student, Date day, Integer... states);

    /**
     * 查询课程计划
     *
     * 查询 学员 报名 指定班级的课程计划
     * @param classCode
     * @param code
     * @return
     */
    List<ScheduleStudent> listCoursePlan(String classCode, String studentCode, Integer... states);

    /**
     * 停用学生
     *
     * @param code
     * @return
     */
    boolean doPause(String code);

    /**
     * 恢复学生
     *
     * @param code
     * @return
     */
    boolean doResume(String code);
}
