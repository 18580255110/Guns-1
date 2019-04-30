package com.stylefeng.guns.modular.adjustMGR.service;

import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.IService;
import com.stylefeng.guns.common.validator.Validator;
import com.stylefeng.guns.core.admin.IAdministratorAware;
import com.stylefeng.guns.modular.system.model.*;

import java.util.Map;

/**
 * <p>
 * 调课/班申请表(学生) 服务类
 * </p>
 *
 * @author simple.song
 * @since 2018-11-19
 */
public interface IAdjustStudentService extends IService<AdjustStudent>, IAdministratorAware {

    /**
     * 调课申请
     *
     * @param member
     * @param student
     * @param fromData
     * @param destData
     */
    @Validator(chain = {AdjustCourseValidator.class})
    void adjustCourse(Member member, Student student, Map<String, Object> fromData, Map<String, Object> destData);

    /**
     * 调班申请
     *
     * @param member
     * @param student
     * @param fromData
     * @param destData
     */
    @Validator(chain = {AdjustClassValidator.class})
    void adjustClass(Member member, Student student, Map<String, Object> fromData, Map<String, Object> destData);

    /**
     * 查询调课/转班申请记录
     *
     * @param page
     * @param adjust
     * @param queryMap
     * @return
     */
    Page<Map<String,Object>> selectApplyMapsPage(AdjustStudentTypeEnum type, Map<String, Object> queryMap);

    /**
     * 关闭申请
     *
     * @param applyId
     */
    AdjustStudent closeApply(Long applyId);

    /**
     * 调课审核
     *
     * @param applyId 申请ID
     * @param action  动作
     * @param remark  备注
     * @return
     */
    AdjustStudent doAdjustApprove(Long applyId, AdjustStudentApproveStateEnum action, String remark);

    /**
     * 转班审核
     *
     * @param applyId
     * @param action
     * @param remark
     * @return
     */
    AdjustStudent doChangeApprove(Long applyId, AdjustStudentApproveStateEnum action, String remark);

    /**
     * 计算已经成功调课/转班次数
     * @param classCode
     * @param student
     * @param student
     * @return
     */
    int countAdjust(String classCode, String student, AdjustStudentTypeEnum type);

    /**
     * 判断是否可以调课
     *
     * @param adjustApply
     * @return
     */
    boolean canAdjust(AdjustStudent adjustApply);

    /**
     * 判断是否可以转班
     *
     * @param adjustApply
     * @return
     */
    boolean canChange(AdjustStudent adjustApply);

    StudentClass getCurrentValidClass(StudentClass studentClass);
}
