package com.stylefeng.guns.modular.education.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.stylefeng.guns.common.constant.factory.ConstantFactory;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.modular.adjustMGR.service.IAdjustStudentService;
import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.education.service.IScheduleStudentService;
import com.stylefeng.guns.modular.education.service.IStudentClassService;
import com.stylefeng.guns.modular.education.transfer.StudentClassInfo;
import com.stylefeng.guns.modular.system.dao.StudentClassMapper;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.modular.system.model.Class;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2018/12/28 9:48
 * @Version 1.0
 */
@Service
public class StudentClassServiceImpl extends ServiceImpl<StudentClassMapper, StudentClass> implements IStudentClassService {

    @Autowired
    private IClassService classService;

    @Autowired
    private IScheduleStudentService scheduleStudentService;

    @Autowired
    private StudentClassMapper studentClassMapper;

    @Autowired
    private IAdjustStudentService adjustStudentService;

    @Override
    public void doChange(String studentCode, String sourceClass, String targetClass) {

        Wrapper<StudentClass> queryWrapper = new EntityWrapper<StudentClass>();
        queryWrapper.eq("student_code", studentCode);
        queryWrapper.eq("class_code", sourceClass);
        queryWrapper.eq("status", GenericState.Valid.code);

        StudentClass currClass = selectOne(queryWrapper);

        if (null == currClass)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"报班信息"});

        currClass.setStatus(GenericState.Invalid.code);
        currClass.setRemark("转入班级 [" + sourceClass +"]");
        updateById(currClass);

        String[] ignoreProperties = new String[]{"id", "fcode", "pcode", "pcodes"};
        StudentClass newClass = new StudentClass();
        BeanUtils.copyProperties(currClass, newClass, ignoreProperties);

        Class classInfo = classService.get(targetClass);

        newClass.setClassCode(targetClass);
        newClass.setClassName(classInfo.getName());
        newClass.setStatus(GenericState.Valid.code);

        insert(newClass);
    }

    @Override
    public List<StudentClassInfo> listSignedStudent(Map<String, Object> queryMap) {
        return studentClassMapper.listSignedStudent(queryMap);
    }

    @Override
    public List<Class> selectMemberHistorySignedClass(Student student, Map<String, Object> historyQueryMap) {
        Map<String, Object> arguments = buildQueryArguments(historyQueryMap);
        arguments.put("student", student.getCode());

        return studentClassMapper.selectMemberSignedClass(arguments);
    }

    @Override
    public List<StudentClass> selectCurrentClassInfo(Student student) {
        Wrapper<StudentClass> queryWrapper = new EntityWrapper<StudentClass>();

        queryWrapper.eq("student_code", student.getCode());
        queryWrapper.eq("status", GenericState.Valid.code);

        return selectList(queryWrapper);
    }

    @Override
    public String getOrderNo(String studentCode, String sourceClass) {
        return null;
    }

    @Override
    public void doReverse(String studentCode, String classCode) {
        Wrapper<StudentClass> queryWrapper = new EntityWrapper<StudentClass>();
        queryWrapper.eq("student_code", studentCode);
        queryWrapper.eq("class_code", classCode);
        queryWrapper.eq("status", GenericState.Valid.code);
        List<StudentClass> currStudentClassList = selectList(queryWrapper);

        for (StudentClass studentClass : currStudentClassList){
            studentClass.setStatus(GenericState.Invalid.code);
            studentClass.setRemark("已退费");
            updateById(studentClass);

            scheduleStudentService.doReverse(studentCode, classCode);
        }
    }

    @Override
    public void doReverse(String orderNo) {
        Wrapper<StudentClass> queryWrapper = new EntityWrapper<StudentClass>();
        queryWrapper.eq("order_no", orderNo);
        queryWrapper.eq("status", GenericState.Valid.code);
        List<StudentClass> currStudentClassList = selectList(queryWrapper);

        String now = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");

        for (StudentClass studentClass : currStudentClassList){
            studentClass.setStatus(GenericState.Invalid.code);

            studentClass.setRemark(now + " 已退费");
            updateById(studentClass);

            scheduleStudentService.doReverse(studentClass.getStudentCode(), studentClass.getClassCode());
        }
    }

    @Override
    public void doReverse(OrderItem orderItem) {
        Wrapper<StudentClass> queryWrapper = new EntityWrapper<StudentClass>();
        queryWrapper.eq("order_no", orderItem.getOrderNo());
        queryWrapper.eq("class_code", orderItem.getItemObjectCode());
        queryWrapper.eq("status", GenericState.Valid.code);
        List<StudentClass> currStudentClassList = selectList(queryWrapper);

        StudentClass studentClass = null;

        if (currStudentClassList.isEmpty()){
            // 没有有效的班级，查询是否有转班记录
            Wrapper<StudentClass> queryWrapper2 = new EntityWrapper<StudentClass>();
            queryWrapper2.eq("order_no", orderItem.getOrderNo());
            queryWrapper2.eq("class_code", orderItem.getItemObjectCode());
            queryWrapper2.eq("status", GenericState.Invalid.code);
            StudentClass hisStudentClass = selectOne(queryWrapper);
            if (null == hisStudentClass){
                throw new ServiceException("");
            }

            studentClass = findCurrentStudentClass(orderItem.getOrderNo(), hisStudentClass.getStudentCode(), orderItem.getItemObjectCode());
        }else{
            studentClass = currStudentClassList.get(0);
        }

        if (null == studentClass){
            throw new ServiceException("订单异常, 不能退费");
        }

        String now = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");

        studentClass.setStatus(GenericState.Invalid.code);

        studentClass.setRemark(now + " 已退费");
        updateById(studentClass);

        scheduleStudentService.doReverse(studentClass.getStudentCode(), studentClass.getClassCode());
    }

    private StudentClass findCurrentStudentClass(String orderNo, String studentCode, String classCode) {
        Wrapper<AdjustStudent> queryWrapper = new EntityWrapper<AdjustStudent>();
        queryWrapper.eq("student_code", studentCode);
        queryWrapper.eq("source_class", classCode);
        queryWrapper.eq("status", "11");

        AdjustStudent studentAdjustRecord = adjustStudentService.selectOne(queryWrapper);

        StudentClass currentStudentClass = null;
        if (null == studentAdjustRecord){
            return currentStudentClass;
        }

        Wrapper<StudentClass> studentClassQueryWrapper = new EntityWrapper<StudentClass>();
        studentClassQueryWrapper.eq("order_no", orderNo);
        studentClassQueryWrapper.eq("class_code", studentAdjustRecord.getTargetClass());
        studentClassQueryWrapper.eq("status", GenericState.Valid.code);
        currentStudentClass = selectOne(studentClassQueryWrapper);

        if (null == currentStudentClass){
            // 转班记录里面的目标班级也不是当前班级, 需要继续往后找
            currentStudentClass = findCurrentStudentClass(orderNo, studentCode, studentAdjustRecord.getTargetClass());
        }

        return currentStudentClass;
    }


    private Map<String, Object> buildQueryArguments(Map<String, Object> queryParams) {
        Iterator<String> queryKeyIter = queryParams.keySet().iterator();
        Map<String, Object> arguments = new HashMap<String, Object>();

        List<String> subjectList = new ArrayList<String>();
        arguments.put("subjectList", subjectList);
        List<Integer> cycleList = new ArrayList<Integer>();
        arguments.put("cycleList", cycleList);
        List<Integer> abilityList = new ArrayList<Integer>();
        arguments.put("abilityList", abilityList);
        List<Integer> methodList = new ArrayList<Integer>();
        arguments.put("methodList", methodList);
        List<Integer> weekList = new ArrayList<Integer>();
        arguments.put("weekList", weekList);
        List<Integer> gradeList = new ArrayList<Integer>();
        arguments.put("gradeList", gradeList);
        List<String> teacherList = new ArrayList<String>();
        arguments.put("teacherList", teacherList);
        List<String> payStateList = new ArrayList<String>();
        arguments.put("payStateList", payStateList);

        Map<String , Object> subjectMap = ConstantFactory.me().getdictsMap("subject_type");

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

            if ("subjects".equals(key)){
                StringTokenizer tokenizer = new StringTokenizer((String)queryParams.get(key), ",");
                while(tokenizer.hasMoreTokens()){
                    String subject = tokenizer.nextToken();
                    String subjectValue = subject;
                    try {
                        Integer.parseInt(subject);
                    }catch(Exception e){
                        subjectValue = String.valueOf(subjectMap.get(subject));
                        try {
                            Integer.parseInt(subjectValue);
                        }catch(Exception ee){
                            subjectValue = null;
                        }
                    }

                    if (null != subjectValue)
                        subjectList.add(subjectValue);
                }
                arguments.put("subjectList", subjectList);
                arguments.remove(key);
            }

            if ("classCycles".equals(key)){
                StringTokenizer tokenizer = new StringTokenizer((String)queryParams.get(key), ",");
                while(tokenizer.hasMoreTokens()){
                    try {
                        cycleList.add(Integer.parseInt(tokenizer.nextToken()));
                    }catch(Exception e){}
                }
                arguments.put("cycleList", cycleList);
                arguments.remove(key);
            }

            if ("abilities".equals(key)){
                StringTokenizer tokenizer = new StringTokenizer((String)queryParams.get(key), ",");
                while(tokenizer.hasMoreTokens()){
                    try {
                        abilityList.add(Integer.parseInt(tokenizer.nextToken()));
                    }catch(Exception e){}
                }
                arguments.put("abilityList", abilityList);
                arguments.remove(key);
            }

            if ("methods".equals(key)){
                StringTokenizer tokenizer = new StringTokenizer((String)queryParams.get(key), ",");
                while(tokenizer.hasMoreTokens()){
                    try {
                        methodList.add(Integer.parseInt(tokenizer.nextToken()));
                    }catch(Exception e){}
                }
                arguments.put("methodList", methodList);
                arguments.remove(key);
            }

            if ("grades".equals(key)){
                StringTokenizer tokenizer = new StringTokenizer((String)queryParams.get(key), ",");
                while(tokenizer.hasMoreTokens()){
                    try {
                        gradeList.add(Integer.parseInt(tokenizer.nextToken()));
                    }catch(Exception e){}
                }
                arguments.put("gradeList", gradeList);
                arguments.remove(key);
            }

            if ("teachers".equals(key)){
                StringTokenizer tokenizer = new StringTokenizer((String)queryParams.get(key), ",");
                while(tokenizer.hasMoreTokens()){
                    try {
                        teacherList.add(tokenizer.nextToken());
                    }catch(Exception e){}
                }
                arguments.put("teacherList", teacherList);
                arguments.remove(key);
            }


            if ("payStates".equals(key)){
                StringTokenizer tokenizer = new StringTokenizer((String)queryParams.get(key), ",");
                while(tokenizer.hasMoreTokens()){
                    try {
                        payStateList.add(tokenizer.nextToken());
                    }catch(Exception e){}
                }
                arguments.put("payStateList", payStateList);
                arguments.remove(key);
            }
        }

        return arguments;
    }
}
