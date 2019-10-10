package com.stylefeng.guns.rest.modular.education.responser;

import com.stylefeng.guns.modular.classMGR.transfer.ClassPlan;
import com.stylefeng.guns.modular.education.transfer.StudentPlan;
import com.stylefeng.guns.rest.core.Responser;
import com.stylefeng.guns.rest.core.SimpleResponser;

import java.util.ArrayList;
import java.util.List;

/**
 * 班级排课表
 *
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/10/9 08:52
 * @Version 1.0
 */
public class ClassPlanListResponser extends SimpleResponser {
    private List<ClassPlan> planList = new ArrayList<ClassPlan>();

    public List<ClassPlan> getPlanList() {
        return planList;
    }

    public void setPlanList(List<ClassPlan> planList) {
        this.planList = planList;
    }

    public static Responser me(List<ClassPlan> planList) {
        ClassPlanListResponser response = new ClassPlanListResponser();

        response.setCode(SUCCEED);
        response.setMessage("查询成功");

        response.setPlanList(planList);
        return response;
    }
}
