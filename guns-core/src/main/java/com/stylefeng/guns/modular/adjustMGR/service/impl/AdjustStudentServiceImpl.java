package com.stylefeng.guns.modular.adjustMGR.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.stylefeng.guns.common.constant.factory.PageFactory;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.admin.Administrator;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.modular.adjustMGR.service.IAdjustStudentService;
import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.education.service.IScheduleClassService;
import com.stylefeng.guns.modular.education.service.IScheduleStudentService;
import com.stylefeng.guns.modular.education.service.IStudentClassService;
import com.stylefeng.guns.modular.orderMGR.service.IOrderService;
import com.stylefeng.guns.modular.system.dao.AdjustStudentMapper;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.modular.system.model.Class;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <p>
 * 调课/班申请表(学生) 服务实现类
 * </p>
 *
 * @author simple.song
 * @since 2018-11-19
 */
@Service
public class AdjustStudentServiceImpl extends ServiceImpl<AdjustStudentMapper, AdjustStudent> implements IAdjustStudentService {
    private static final Logger log = LoggerFactory.getLogger(AdjustStudentServiceImpl.class);

    @Autowired
    private IClassService classService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private AdjustStudentMapper adjustStudentMapper;

    @Autowired
    private IScheduleClassService scheduleClassService;

    @Autowired
    private IScheduleStudentService scheduleStudentService;

    @Autowired
    private IStudentClassService studentClassService;

    private Administrator administrator;

    @Override
    public AdjustStudent adjustCourse(Member member, Student student, Map<String, Object> fromData, Map<String, Object> destData) {

        com.stylefeng.guns.modular.system.model.Class sourceClassInfo = (com.stylefeng.guns.modular.system.model.Class) fromData.get("sourceClass");
        com.stylefeng.guns.modular.system.model.Class targetClassInfo = (com.stylefeng.guns.modular.system.model.Class) destData.get("targetClass");
        String outlineCode = (String) fromData.get("outlineCode");

        if (hasApproving(student.getCode(), sourceClassInfo.getCode(), targetClassInfo.getCode(), outlineCode, AdjustStudentTypeEnum.Adjust)){
            throw new ServiceException(MessageConstant.MessageCode.ADJUST_COURSE_DUPLICATE);
        }

        AdjustStudent adjustCourseApply = new AdjustStudent();

        adjustCourseApply.setUserName(member.getUserName());
        adjustCourseApply.setStudentCode(student.getCode());
        adjustCourseApply.setType(AdjustStudentTypeEnum.Adjust.code);
        adjustCourseApply.setOutlineCode(outlineCode);
        adjustCourseApply.setSourceClass(sourceClassInfo.getCode());
        adjustCourseApply.setTargetClass(targetClassInfo.getCode());

        adjustCourseApply.setStatus(GenericState.Valid.code);
        adjustCourseApply.setWorkStatus(AdjustStudentApproveStateEnum.Create.code);
        adjustCourseApply.setCreateTime(new Date());

        insert(adjustCourseApply);

        return adjustCourseApply;
    }

    @Override
    public AdjustStudent adjustClass(Member member, Student student, Map<String, Object> fromData, Map<String, Object> destData) {

        com.stylefeng.guns.modular.system.model.Class sourceClassInfo = (com.stylefeng.guns.modular.system.model.Class) fromData.get("sourceClass");
        com.stylefeng.guns.modular.system.model.Class targetClassInfo = (com.stylefeng.guns.modular.system.model.Class) destData.get("targetClass");

        if (hasApproving(student.getCode(), sourceClassInfo.getCode(), targetClassInfo.getCode(), null, AdjustStudentTypeEnum.Change)){
            throw new ServiceException(MessageConstant.MessageCode.CHANGE_CLASS_DUPLICATE);
        }

        AdjustStudent adjustCourseApply = new AdjustStudent();

        adjustCourseApply.setUserName(member.getUserName());
        adjustCourseApply.setStudentCode(student.getCode());
        adjustCourseApply.setType(AdjustStudentTypeEnum.Change.code);
        adjustCourseApply.setSourceClass(sourceClassInfo.getCode());
        adjustCourseApply.setTargetClass(targetClassInfo.getCode());

        adjustCourseApply.setStatus(GenericState.Valid.code);
        adjustCourseApply.setWorkStatus(AdjustStudentApproveStateEnum.Create.code);
        adjustCourseApply.setCreateTime(new Date());

        insert(adjustCourseApply);

        return adjustCourseApply;
    }

    @Override
    public Page<Map<String, Object>> selectApplyMapsPage(AdjustStudentTypeEnum type, Map<String, Object> queryMap) {
        Page<Map<String, Object>> page = new PageFactory<Map<String, Object>>().defaultPage();

        Map<String, Object> arguments = buildQueryArguments(type, queryMap);

        List<Map<String, Object>> pageResult = adjustStudentMapper.selectApplyMapsPage(page, arguments);

        page.setRecords(pageResult);

        return page;
    }

    private Map<String, Object> buildQueryArguments(AdjustStudentTypeEnum type, Map<String, Object> queryParams) {
        Iterator<String> queryKeyIter = queryParams.keySet().iterator();
        Map<String, Object> arguments = new HashMap<String, Object>();
        arguments.put("type", type.code);

        while(queryKeyIter.hasNext()){
            String key = queryKeyIter.next();
            Object value = queryParams.get(key);

            if (null == value)
                continue;

            if (String.class.equals(value.getClass())){
                if (StringUtils.isEmpty((String) value))
                    continue;
            }
            arguments.put(key, queryParams.get(key));
        }
        return arguments;
    }

    @Override
    public AdjustStudent closeApply(Long applyId) {

        if (null == applyId)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"需要关闭的申请项"});

        AdjustStudent adjustStudent = selectById(applyId);

        if (null == adjustStudent)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"申请没找到"});

        if (GenericState.Invalid.code == adjustStudent.getStatus())
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_STATE, new String[]{"已失效的申请不能再进行关闭操作"});

        if (AdjustStudentApproveStateEnum.Close.code == adjustStudent.getWorkStatus())
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_STATE, new String[]{"已关闭的申请不能再进行关闭操作"});

        if (AdjustStudentApproveStateEnum.Appove.code == adjustStudent.getWorkStatus())
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_STATE, new String[]{"已通过审批的申请不能再进行关闭操作"});

        adjustStudent.setStatus(GenericState.Invalid.code);
        adjustStudent.setWorkStatus(AdjustStudentApproveStateEnum.Close.code);
        if (null != administrator) {
            adjustStudent.setOpId(Long.parseLong(String.valueOf(administrator.getId())));
            adjustStudent.setOperator(administrator.getName());
        }
        updateById(adjustStudent);

        return adjustStudent;
    }

    @Override
    public AdjustStudent doAdjustApprove(Long applyId, AdjustStudentApproveStateEnum approveState, String remark) {
        if (null == applyId)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"需要关闭的申请项"});

        AdjustStudent adjustStudent = selectById(applyId);

        if (null == adjustStudent)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"申请没找到"});

        adjustStudent.setRemark(remark);
        doApprove(adjustStudent, approveState);

        // 调课
        if (AdjustStudentApproveStateEnum.Appove.code == approveState.code)
            scheduleStudentService.doAdjust(adjustStudent.getStudentCode(), adjustStudent.getOutlineCode(), adjustStudent.getSourceClass(), adjustStudent.getTargetClass());

        return adjustStudent;
    }

    @Override
    public AdjustStudent doChangeApprove(Long applyId, AdjustStudentApproveStateEnum approveState, String remark) {
        if (null == applyId)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"需要关闭的申请项"});

        AdjustStudent adjustStudent = selectById(applyId);

        if (null == adjustStudent)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"申请没找到"});

        adjustStudent.setRemark(remark);
        doApprove(adjustStudent, approveState);

        // 转班
        if (AdjustStudentApproveStateEnum.Appove.code == approveState.code) {
            //orderService.doCopyForChange(adjustStudent.getStudentCode(), adjustStudent.getSourceClass(), adjustStudent.getTargetClass());
            scheduleStudentService.doChange(adjustStudent.getStudentCode(), adjustStudent.getSourceClass(), adjustStudent.getTargetClass());
            studentClassService.doChange(adjustStudent.getStudentCode(), adjustStudent.getSourceClass(), adjustStudent.getTargetClass());
        }
        return adjustStudent;
    }

    private void doApprove(AdjustStudent adjustStudent, AdjustStudentApproveStateEnum approveState) {

        if (GenericState.Invalid.code == adjustStudent.getStatus())
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_STATE, new String[]{"已失效的申请不能再进行关闭操作"});
        if (AdjustStudentApproveStateEnum.Close.code == adjustStudent.getWorkStatus())
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_STATE, new String[]{"已关闭的申请不能再进行关闭操作"});
        if (AdjustStudentApproveStateEnum.Create.code != adjustStudent.getWorkStatus())
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_STATE, new String[]{"申请单状态异常"});

        if (null != administrator) {
            adjustStudent.setOpId(Long.parseLong(String.valueOf(administrator.getId())));
            adjustStudent.setOperator(administrator.getName());
        }
        adjustStudent.setWorkStatus(approveState.code);
        adjustStudent.setUpdateTime(new Date());
        updateById(adjustStudent);
    }

    @Override
    public int countAdjust(String classCode, String student, AdjustStudentTypeEnum type) {

        Wrapper<AdjustStudent> queryWrapper = new EntityWrapper<AdjustStudent>();

        queryWrapper.eq("source_class", classCode);
        queryWrapper.eq("student_code", student);
        queryWrapper.eq("work_status", AdjustStudentApproveStateEnum.Appove.code);
        queryWrapper.eq("status", GenericState.Valid.code);

        return selectCount(queryWrapper);
    }

    @Override
    public boolean canAdjust(AdjustStudent adjustApply) {
        String targetClassCode = adjustApply.getTargetClass();
        if (null == targetClassCode)
            return false;
        Class classInfo = classService.get(targetClassCode);
        if (null == classInfo)
            return false;

        return true;
    }

    @Override
    public boolean canChange(AdjustStudent adjustApply) {

        String targetClassCode = adjustApply.getTargetClass();
        if (null == targetClassCode)
            return false;
        Class classInfo = classService.get(targetClassCode);
        if (null == classInfo)
            return false;

        int targetClassSignedCount = classService.queryOrderedCount(targetClassCode);
        if (targetClassSignedCount >= classInfo.getQuato())
            throw new ServiceException(MessageConstant.MessageCode.ORDER_NO_CAPACITY);

        return true;
    }

    @Override
    public StudentClass getCurrentValidClass(StudentClass studentClass) {

        AdjustStudent currValidClass = null;
        StudentClass outStudentClassInfo = studentClass;
        StudentClass studentClassInfo = null;
        String classCode = studentClass.getClassCode();
        List<Long> excludeIds = new ArrayList<Long>();

        while(null == studentClassInfo) {
            Wrapper<AdjustStudent> queryWrapper = new EntityWrapper<AdjustStudent>();
            queryWrapper.eq("source_class", classCode);
            queryWrapper.eq("student_code", studentClass.getStudentCode());
            queryWrapper.eq("status", GenericState.Valid.code);
            queryWrapper.eq("work_status", 11);

            AdjustStudent classChange = selectOne(queryWrapper);

            if (null == classChange)
                // 找不到有效的订购课程，一般是退费了
                break;

            Wrapper<StudentClass> relWrapper = new EntityWrapper<StudentClass>();
            relWrapper.eq("student_code", classChange.getStudentCode());
            relWrapper.eq("class_code", classChange.getTargetClass());
            relWrapper.notIn("id", excludeIds);

            studentClassInfo = studentClassService.selectOne(relWrapper);

            if (null == studentClassInfo || null == studentClassInfo.getStatus())
                break;

            if (GenericState.Valid.code != studentClassInfo.getStatus()){
                excludeIds.add(outStudentClassInfo.getId());
                outStudentClassInfo = studentClassInfo;
                currValidClass = null;
                studentClassInfo = null;
                classCode = classChange.getTargetClass();
            }else{

            }
        }

        return studentClassInfo;
    }

    private boolean hasApproving(String student, String sourceClass, String targetClass, String outlineCode, AdjustStudentTypeEnum type) {
        Wrapper<AdjustStudent> queryLimitWrapper = new EntityWrapper<AdjustStudent>();
        queryLimitWrapper.eq("student_code", student);
        queryLimitWrapper.eq("status", GenericState.Valid.code);
        queryLimitWrapper.eq("work_status", AdjustStudentApproveStateEnum.Appove.code);

        if (3 <= selectCount(queryLimitWrapper))
            return false;

        Wrapper<AdjustStudent> queryWrapper = new EntityWrapper<AdjustStudent>();
        queryWrapper.eq("student_code", student);
        queryWrapper.eq("type", type.code);

        if (null != outlineCode)
            queryWrapper.eq("outline_code", outlineCode);

        queryWrapper.eq("source_class", sourceClass);
        queryWrapper.eq("status", GenericState.Valid.code);
        // 只有被拒绝的重复申请才被允许
        queryWrapper.eq("work_status", AdjustStudentApproveStateEnum.Create.code);

        return 0 < selectCount(queryWrapper);
    }

    @Override
    public void setAdministrator(Administrator admin) {
        administrator = admin;
    }
}
