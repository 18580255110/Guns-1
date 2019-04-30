package com.stylefeng.guns.modular.studentMGR.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.modular.studentMGR.service.IStudentZoneService;
import com.stylefeng.guns.modular.system.dao.StudentZoneMapper;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.modular.system.model.Student;
import com.stylefeng.guns.modular.system.model.StudentZone;
import org.springframework.stereotype.Service;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/4/30 06:06
 * @Version 1.0
 */
@Service
public class StudentZoneServiceImpl extends ServiceImpl<StudentZoneMapper, StudentZone> implements IStudentZoneService {
    @Override
    public boolean isZoneStudent(Student student, Class classInfo) {

        Wrapper<StudentZone> queryWrapper = new EntityWrapper<StudentZone>();
        queryWrapper.eq("code", student.getCode());
        queryWrapper.eq("status", GenericState.Valid.code);

        int count = selectCount(queryWrapper);
        return count > 0 ? true : false;
    }
}
