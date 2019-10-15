package com.stylefeng.guns.task.education;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.core.admin.Administrator;
import com.stylefeng.guns.modular.education.service.IStudentPrivilegeService;
import com.stylefeng.guns.modular.studentMGR.service.IStudentService;
import com.stylefeng.guns.modular.system.dao.StudentPrivilegeMapper;
import com.stylefeng.guns.modular.system.model.Student;
import com.stylefeng.guns.modular.system.model.StudentPrivilege;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/10/8 11:52
 * @Version 1.0
 */
@Component
public class SignPrivilegeTask {
    private static final Logger log = LoggerFactory.getLogger(SignPrivilegeTask.class);

    @Autowired
    private StudentPrivilegeMapper studentPrivilegeMapper;

    @Autowired
    private IStudentPrivilegeService studentPrivilegeService;

    @Autowired
    private IStudentService studentService;

    private Map<String, Long> privilegeCache = new HashMap<String, Long>();

//    @Scheduled(fixedDelay = 60000)
    public void grantPrivilege(){
        log.info("Grant privilege begin ... ");
        Administrator administrator = new Administrator();
        administrator.setAccount("1");
        administrator.setId(1);
        administrator.setName("科萃教育");

        log.info("Update privilege cache ... ");
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("studentCode", "XY19042800008874");
        List<Map<String, Object>> resultList = studentPrivilegeMapper.selectStudentPrivilegeStatistic(queryParams);

        for (Map<String, Object> result : resultList) {
            //
            StudentPrivilege studentPrivilege = new StudentPrivilege();
            try {
                BeanUtils.populate(studentPrivilege, result);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                continue;
            }
            // KEY = 学员 + 学年 + 年级 + 学期 + 班型
            String key = studentPrivilege.getKey();
            int next = 1;
            doGrant(key, next);
        }

        log.info("Update privilege over.");

    }

    private void doGrant(String key, int next) {
        StudentPrivilege studentPrivilege = parseKey(key);
        log.info("Begin grant privilege: student = {}, year = {}, grade = {}, cycle = {}, ability = {}", studentPrivilege.getStudentCode(), studentPrivilege.getAcademicYear(), studentPrivilege.getGrade(), studentPrivilege.getCycle(), studentPrivilege.getAbility());
        // 对一报名班级做权限补充，学员肯定是已报名的，所以补充以前缺少的权限
        grantCurrentCyclePrivilege(studentPrivilege);

        if (next > 0) {
            StudentPrivilege nextStudentPrivilege = studentPrivilege.next();
            String nextKey = nextStudentPrivilege.getKey();
            log.info("Start authorize student = {} next cycle privilege, year = {}, grade = {}, cycle = {}, ability = {}", nextStudentPrivilege.getStudentCode(), nextStudentPrivilege.getAcademicYear(), nextStudentPrivilege.getGrade(), nextStudentPrivilege.getCycle(), nextStudentPrivilege.getAbility());

            if (!studentPrivilegeService.hasPrivilege(nextStudentPrivilege)) {
                log.info("Next cycle privilege is not exists");
                doGrant(nextKey, --next);
            } else {
                log.info("Grant student = {} next cycle privilege, year = {}, grade = {}, cycle = {}, ability = {}", nextStudentPrivilege.getStudentCode(), nextStudentPrivilege.getAcademicYear(), nextStudentPrivilege.getGrade(), nextStudentPrivilege.getCycle(), nextStudentPrivilege.getAbility());
                grantNextCyclePrivilege(nextStudentPrivilege);
            }
        }
    }

    private void grantNextCyclePrivilege(StudentPrivilege nextStudentPrivilege) {
        log.info("Grant next cycle privilege, student = {}, year = {}, grade = {}, cycle = {}, ability = {}", nextStudentPrivilege.getStudentCode(), nextStudentPrivilege.getAcademicYear(), nextStudentPrivilege.getGrade(), nextStudentPrivilege.getCycle(), nextStudentPrivilege.getAbility());
        Student existStudent = studentService.get(nextStudentPrivilege.getStudentCode());

        if (null == existStudent)
            return;

        nextStudentPrivilege.setStudentName(existStudent.getName());
        nextStudentPrivilege.setType(1); // 1 报名类
        nextStudentPrivilege.setStatus(GenericState.Valid.code); // 激活

        if (!studentPrivilegeService.hasPrivilege(nextStudentPrivilege)) {
            log.info("Not found next privilege , need add");
            studentPrivilegeMapper.insert(nextStudentPrivilege);
        }

        log.info("Grant next cycle privilege complete! student = {}, year = {}, grade = {}, cycle = {}, ability = {}", nextStudentPrivilege.getStudentCode(), nextStudentPrivilege.getAcademicYear(), nextStudentPrivilege.getGrade(), nextStudentPrivilege.getCycle(), nextStudentPrivilege.getAbility());
    }

    private void grantCurrentCyclePrivilege(StudentPrivilege studentPrivilege) {

        log.info("Grant current cycle privilege, student = {}, year = {}, grade = {}, cycle = {}, ability = {}", studentPrivilege.getStudentCode(), studentPrivilege.getAcademicYear(), studentPrivilege.getGrade(), studentPrivilege.getCycle(), studentPrivilege.getAbility());
        Student existStudent = studentService.get(studentPrivilege.getStudentCode());

        if (null == existStudent)
            return;

        studentPrivilege.setStudentName(existStudent.getName());
        studentPrivilege.setType(1); // 1 报名类
        studentPrivilege.setStatus(GenericState.Valid.code); // 有效

        if (! studentPrivilegeService.hasPrivilege(studentPrivilege) ) {
            log.info("Not found current privilege, need add");
            studentPrivilegeMapper.insert(studentPrivilege);
        }

        log.info("Grant current cycle privilege complete! student = {}, year = {}, grade = {}, cycle = {}, ability = {}", studentPrivilege.getStudentCode(), studentPrivilege.getAcademicYear(), studentPrivilege.getGrade(), studentPrivilege.getCycle(), studentPrivilege.getAbility());
    }

    private StudentPrivilege parseKey(String key) {
        StringTokenizer keyTokenizer = new StringTokenizer(key, "_");

        int index = 0;

        StudentPrivilege studentPrivilege = new StudentPrivilege();
        while(keyTokenizer.hasMoreTokens()){
            String token = keyTokenizer.nextToken();

            switch (index){
                case 0:
                    studentPrivilege.setStudentCode(token);
                    break;
                case 1:
                    studentPrivilege.setAcademicYear(Integer.parseInt(token));
                    break;
                case 2:
                    studentPrivilege.setSubject(Integer.parseInt(token));
                    break;
                case 3:
                    studentPrivilege.setGrade(Integer.parseInt(token));
                    break;
                case 4:
                    studentPrivilege.setCycle(Integer.parseInt(token));
                    break;
                case 5:
                    studentPrivilege.setAbility(Integer.parseInt(token));
                    break;
                default:
                    // 首 尾
                    break;
            }
            index++;
        }

        return studentPrivilege;
    }
}
