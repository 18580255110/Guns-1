package com.stylefeng.guns.rest.modular.education.responser;

import com.stylefeng.guns.modular.system.model.Student;
import com.stylefeng.guns.modular.system.model.Teacher;
import com.stylefeng.guns.rest.core.Responser;
import com.stylefeng.guns.rest.core.SimpleResponser;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/4/25 19:26
 * @Version 1.0
 */
@ApiModel(value = "TeacherListResponse", description = "Teacher列表")
public class TeacherListResponse extends SimpleResponser {
    @ApiModelProperty(name = "data", value = "学员集合")
    private List<Teacher> data;

    public List<Teacher> getData() {
        return data;
    }

    public void setData(List<Teacher> data) {
        this.data = data;
    }

    public static Responser me(List<Teacher> teacherList) {
        TeacherListResponse response = new TeacherListResponse();

        response.setCode(SUCCEED);
        response.setMessage("查询成功");

        response.setData(teacherList);
        return response;
    }
}
