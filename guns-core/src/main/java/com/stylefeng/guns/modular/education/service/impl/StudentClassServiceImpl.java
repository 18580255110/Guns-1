package com.stylefeng.guns.modular.education.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.education.service.IStudentClassService;
import com.stylefeng.guns.modular.system.dao.StudentClassMapper;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.modular.system.model.Member;
import com.stylefeng.guns.modular.system.model.Student;
import com.stylefeng.guns.modular.system.model.StudentClass;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
    private StudentClassMapper studentClassMapper;

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
    public List<Student> listSignedStudent(Map<String, Object> queryMap) {
        return studentClassMapper.listSignedStudent(queryMap);
    }

    @Override
    public List<Class> selectMemberHistorySignedClass(Student student, Map<String, Object> historyQueryMap) {

//        historyQueryMap.put("member", member.getUserName());
        historyQueryMap.put("student", student.getCode());

        return studentClassMapper.selectMemberSignedClass(historyQueryMap);
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
}
