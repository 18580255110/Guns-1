package com.stylefeng.guns.modular.education.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.classMGR.service.ICourseService;
import com.stylefeng.guns.modular.education.service.IStudentPrivilegeService;
import com.stylefeng.guns.modular.studentMGR.service.IStudentService;
import com.stylefeng.guns.modular.system.dao.StudentPrivilegeMapper;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.modular.system.model.Class;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/10/10 09:48
 * @Version 1.0
 */
@Service
public class StudentPrivilegeServiceImpl extends ServiceImpl<StudentPrivilegeMapper, StudentPrivilege> implements IStudentPrivilegeService {
    private static final Logger log = LoggerFactory.getLogger(ScheduleStudentServiceImpl.class);

//    private static final Integer[] SIX  = new Integer[]{9, 8, 7, 6, 5, 3, 4, 11, 10, 12, 99};
//    private static final Integer[] FIVE  = new Integer[]{2, 1, 12, 99};
//    private static final Integer[] Four  = new Integer[]{2, 1, 12, 99};
//    private static final Integer[] Seven  = new Integer[]{9, 8, 7, 12, 99};

    private static final Integer[] SIX  = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 10, 12, 13, 99};
    private static final Integer[] FIVE  = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 10, 12, 13, 99};
    private static final Integer[] Four  = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 10, 12, 13, 99};
    private static final Integer[] Seven  = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 10, 12, 13, 99};

    private static final Map<Integer, Integer> AbilityWeight = new HashMap<Integer, Integer>(){
        {
            put(9, 90); // 尖端
            put(8, 80); // 实验
            put(6, 80); // 真题C
            put(5, 70); // 真题B
            put(3, 70); // 真题
            put(2, 70); // 尖子
            put(4, 60); // 真题A
            put(7, 60); // 同步
            put(11, 50); // 复习B
            put(1, 50); // 提高
            put(10, 40); // 复习A
            put(12, 0); // 活动
            put(13, 50); // 鸿志
            put(99, 0); // 其他
        }
    };

    @Autowired
    private ICourseService courseService;

    @Autowired
    private IClassService classService;

    @Autowired
    private IStudentService studentService;

    @Override
    public boolean hasPrivilege(Student student, Class classInfo) {
        if(null == student)
            return false;

        if (null == classInfo)
            return false;

        Course courseInfo = courseService.get(classInfo.getCourseCode());

        if (null == courseInfo)
            return false;

        Integer existCount = selectCount(new EntityWrapper<StudentPrivilege>(){
            {
                eq("student_code", student.getCode());
                eq("academic_year", classInfo.getAcademicYear());
                eq("subject", Integer.parseInt(courseInfo.getSubject()));
                eq("grade", courseInfo.getGrade());
                eq("cycle", classInfo.getCycle());
                eq("ability", classInfo.getAbility());
                eq("status", GenericState.Valid.code);
                eq("type", 1);
            }
        });
        return 0 < existCount;
    }

    @Override
    public boolean hasPrivilege(StudentPrivilege studentPrivilege) {
        return 0 < selectCount(getQueryWrapper(studentPrivilege));
    }

    private Wrapper<StudentPrivilege> getQueryWrapper(StudentPrivilege studentPrivilege) {
        return new EntityWrapper<StudentPrivilege>(){
            {
                eq("student_code", studentPrivilege.getStudentCode());
                eq("academic_year", studentPrivilege.getAcademicYear());
                eq("subject", studentPrivilege.getSubject());
                eq("grade", studentPrivilege.getGrade());
                eq("cycle", studentPrivilege.getCycle());
                eq("ability", studentPrivilege.getAbility());
                eq("type", 1);
                eq("status", GenericState.Valid.code);
            }
        };
    }

    @Override
    public void grantSignPrivileges(String studentCode, String classCode) {
        Student student = studentService.get(studentCode);
        log.info("Grant sign privilege student = {}, class = {}", studentCode, classCode);
        if (null == student)
            return;
        Class classInfo = classService.get(classCode);
        if (null == classInfo)
            return ;

        Course course = courseService.get(classInfo.getCourseCode());
        if (null == course)
            return;

        boolean hasPrivilege = hasPrivilege(student, classInfo);

        if (!hasPrivilege){
            StudentPrivilege studentPrivilege = new StudentPrivilege();
            studentPrivilege.setStudentCode(studentCode);
            studentPrivilege.setStudentName(student.getName());
            studentPrivilege.setAcademicYear(classInfo.getAcademicYear());
            studentPrivilege.setCycle(classInfo.getCycle());
            studentPrivilege.setGrade(course.getGrade());
            studentPrivilege.setAbility(classInfo.getAbility());
            studentPrivilege.setType(1);
            studentPrivilege.setComments(classInfo.getName());

            studentPrivilege.setStatus(GenericState.Invalid.code);
            List<StudentPrivilege> studentPrivilegeList = selectList(getQueryWrapper(studentPrivilege));

            if (null == studentPrivilegeList || studentPrivilegeList.isEmpty()){
                studentPrivilege.setStatus(GenericState.Valid.code);
                insert(studentPrivilege);
            }else{
                studentPrivilege = studentPrivilegeList.get(0);
                studentPrivilege.setStatus(GenericState.Valid.code);
                updateById(studentPrivilege);
            }
        }
    }

    @Override
    public void grantSignPrivileges(StudentPrivilege studentPrivilege) {
        log.info("Grant sign privilege student = {}, class = {}", studentPrivilege.getStudentCode(), studentPrivilege);

        boolean hasPrivilege = hasPrivilege(studentPrivilege);

        if (!hasPrivilege){
            studentPrivilege.setStatus(GenericState.Invalid.code);
            List<StudentPrivilege> existInvalidStudentPrivilegeList = selectList(getQueryWrapper(studentPrivilege));

            if (null == existInvalidStudentPrivilegeList || existInvalidStudentPrivilegeList.isEmpty()){
                studentPrivilege.setStatus(GenericState.Valid.code);
                insert(studentPrivilege);
            }else{
                studentPrivilege = existInvalidStudentPrivilegeList.get(0);
                studentPrivilege.setStatus(GenericState.Valid.code);
                updateById(studentPrivilege);
            }
        }
    }

    @Override
    public void grantNextSignPrivileges(String studentCode, String classCode) {
        Student student = studentService.get(studentCode);
        log.info("Grant sign privilege student = {}, class = {}", studentCode, classCode);
        if (null == student)
            return;

        Class classInfo = classService.get(classCode);
        if (null == classInfo)
            return ;

        Course course = courseService.get(classInfo.getCourseCode());
        if (null == course)
            return;

        StudentPrivilege studentPrivilege = new StudentPrivilege();
        studentPrivilege.setStudentCode(studentCode);
        studentPrivilege.setStudentName(student.getName());
        studentPrivilege.setAcademicYear(classInfo.getAcademicYear());
        studentPrivilege.setCycle(classInfo.getCycle());
        studentPrivilege.setGrade(course.getGrade());
        studentPrivilege.setSubject(Integer.parseInt(course.getSubject()));
        studentPrivilege.setAbility(classInfo.getAbility());
        studentPrivilege.setType(1);
        studentPrivilege.setComments(classInfo.getName());

        StudentPrivilege nextStudentPrivilege = studentPrivilege.next();

        boolean hasPrivilege = hasPrivilege(nextStudentPrivilege);

        if (!hasPrivilege){
            nextStudentPrivilege.setStatus(GenericState.Invalid.code);
            List<StudentPrivilege> studentPrivilegeList = selectList(getQueryWrapper(nextStudentPrivilege));

            if (null == studentPrivilegeList || studentPrivilegeList.isEmpty()){
                nextStudentPrivilege.setStatus(GenericState.Valid.code);
                insert(nextStudentPrivilege);
            }else{
                nextStudentPrivilege = studentPrivilegeList.get(0);
                nextStudentPrivilege.setStatus(GenericState.Valid.code);
                updateById(nextStudentPrivilege);
            }
        }
    }

    @Override
    public boolean hasAdvancePrivilege(Student student, Class classInfo) {

        if(null == student)
            return false;

        if (null == classInfo)
            return false;

        Course courseInfo = courseService.get(classInfo.getCourseCode());

        if (null == courseInfo)
            return false;

        int currWeight =  0 ;
        
        try{
            currWeight = AbilityWeight.get(classInfo.getAbility());
            log.info("Advantage privilege weight = {}", currWeight);
        }catch(Exception e){
            log.warn("Student <{}> couldn't found privileges, class <{}>, ability <{}>", student.getCode(), classInfo.getCode(), classInfo.getAbility());
        }
        boolean hasAdvPrivilege = false;

        switch(classInfo.getGrade()){
            case 4:
                // 四年级
                hasAdvPrivilege = hasAdvancePrivilege(currWeight, Four, student, classInfo);
                break;
            case 5:
                hasAdvPrivilege = hasAdvancePrivilege(currWeight, FIVE, student, classInfo);
                break;
            case 6:
                hasAdvPrivilege = hasAdvancePrivilege(currWeight, SIX, student, classInfo);
                break;
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
                hasAdvPrivilege = hasAdvancePrivilege(currWeight, Seven, student, classInfo);
                break;
        }
        return hasAdvPrivilege;
    }

    private boolean hasAdvancePrivilege(int currWeight, Integer[] abilityArray, Student student, Class classInfo) {
        boolean hasAdvPrivilege = false;

        for(Integer ability : abilityArray){
            int abilityWeight = AbilityWeight.get(ability);
            if (abilityWeight < currWeight)
                continue;

            classInfo.setAbility(ability);
            if (hasPrivilege(student, classInfo)) {
                log.info(" Student {} has {} privilege for class {}", student.getCode(), ability, classInfo.getCode());
                hasAdvPrivilege = true;
            }

            if (hasAdvPrivilege)
                break;
        }

        return hasAdvPrivilege;
    }
}
