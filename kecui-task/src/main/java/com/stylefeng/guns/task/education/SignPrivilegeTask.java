package com.stylefeng.guns.task.education;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.stylefeng.guns.core.admin.Administrator;
import com.stylefeng.guns.modular.studentMGR.service.IStudentService;
import com.stylefeng.guns.modular.system.dao.StudentPrivilegeMapper;
import com.stylefeng.guns.modular.system.model.Student;
import com.stylefeng.guns.modular.system.model.StudentPrivilege;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/10/8 11:52
 * @Version 1.0
 */
public class SignPrivilegeTask {
    private static final Logger log = LoggerFactory.getLogger(SignPrivilegeTask.class);

    @Autowired
    private StudentPrivilegeMapper studentPrivilegeMapper;

    @Autowired
    private IStudentService studentService;

    private Map<String, Integer> privilegeCache = new HashMap<String, Integer>();

    @Scheduled(fixedDelay = 60000)
    public void grantPrivilege(){
        log.info("Grant privilege begin ... ");
        Administrator administrator = new Administrator();
        administrator.setAccount("1");
        administrator.setId(1);
        administrator.setName("科萃教育");


        log.info("Update privilege cache ... ");
        Map<String, Object> queryParams = new HashMap<>();
        List<Map<String, Object>> resultList = studentPrivilegeMapper.selectStudentPrivilegeCount(queryParams);
        Set<String> keySet = new HashSet<>();

        for (Map<String, Object> result : resultList) {
            StudentPrivilege studentPrivilege = new StudentPrivilege();
            try {
                BeanUtils.populate(studentPrivilege, result);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                continue;
            }

            String key = studentPrivilege.getKey();

            if (privilegeCache.containsKey(key))
                continue;

            privilegeCache.put(key, (Integer)result.get("privilegeCount"));
        }

        keySet.addAll(privilegeCache.keySet());

        Iterator<String> keyIter = keySet.iterator();
        while(keyIter.hasNext()){
            String key = keyIter.next();
            doGrant(key);
        }

        log.info("Update privilege over...");

    }

    private void doGrant(String key) {
        Integer privilegeCount = privilegeCache.get(key);
        StudentPrivilege studentPrivilege = parseKey(key);
        if (0 == privilegeCount){
            // 补充以前缺少的权限
            grantCurrentCyclePrivilege(studentPrivilege);
            //privilegeCache.put(key, 1);
        }

        StudentPrivilege nextStudentPrivilege = studentPrivilege.next();
        String nextKey = nextStudentPrivilege.getKey();
        if (privilegeCache.containsKey(nextKey)){
            doGrant(nextKey);
        }else{
            grantNextCyclePrivilege(nextStudentPrivilege);
            //privilegeCache.put(nextKey, 1);
        }
    }

    private void grantNextCyclePrivilege(StudentPrivilege nextStudentPrivilege) {
        Student existStudent = studentService.get(nextStudentPrivilege.getStudentCode());

        if (null == existStudent)
            return;

        nextStudentPrivilege.setStudentName(existStudent.getName());
        nextStudentPrivilege.setType(1);
        nextStudentPrivilege.setStatus(1); // 激活

        long existCount = queryExistStudentPrivilegeCount(nextStudentPrivilege);

        if (0L == existCount)
            studentPrivilegeMapper.insert(nextStudentPrivilege);
    }

    private void grantCurrentCyclePrivilege(StudentPrivilege studentPrivilege) {

        Student existStudent = studentService.get(studentPrivilege.getStudentCode());

        if (null == existStudent)
            return;

        studentPrivilege.setStudentName(existStudent.getName());
        studentPrivilege.setType(1);
        studentPrivilege.setStatus(0); // 已使用

        long existCount = queryExistStudentPrivilegeCount(studentPrivilege);

        if (0L == existCount)
            studentPrivilegeMapper.insert(studentPrivilege);
        else{
            exipireExistStudentPrivilege(studentPrivilege);
        }
    }

    private void exipireExistStudentPrivilege(StudentPrivilege studentPrivilege) {

        studentPrivilegeMapper.updateForSet("status=0", new EntityWrapper<StudentPrivilege>(){
            {
                eq("studentCode", studentPrivilege.getStudentCode());
                eq("academicYear", studentPrivilege.getAcademicYear());
                eq("grade", studentPrivilege.getGrade());
                eq("cycle", studentPrivilege.getCycle());
                eq("ability", studentPrivilege.getAbility());
            }
        });
    }

    private long queryExistStudentPrivilegeCount(StudentPrivilege studentPrivilege) {

        return studentPrivilegeMapper.selectCount(new EntityWrapper<StudentPrivilege>(){
            {
                eq("studentCode", studentPrivilege.getStudentCode());
                eq("academicYear", studentPrivilege.getAcademicYear());
                eq("grade", studentPrivilege.getGrade());
                eq("cycle", studentPrivilege.getCycle());
                eq("ability", studentPrivilege.getAbility());
            }
        });
    }

    private StudentPrivilege parseKey(String key) {
        StringTokenizer keyTokenizer = new StringTokenizer(key, "_");

        int index = 0;

        StudentPrivilege studentPrivilege = new StudentPrivilege();
        while(keyTokenizer.hasMoreTokens()){
            String token = keyTokenizer.nextToken();

            switch (index){
                case 1:
                    studentPrivilege.setStudentCode(token);
                    break;
                case 2:
                    studentPrivilege.setAcademicYear(Integer.parseInt(token));
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
