package com.stylefeng.guns.rest.modular.education.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.common.constant.factory.PageFactory;
import com.stylefeng.guns.modular.system.model.Teacher;
import com.stylefeng.guns.modular.teacherMGR.service.TeacherService;
import com.stylefeng.guns.rest.core.Responser;
import com.stylefeng.guns.rest.modular.education.responser.TeacherDetailResponse;
import com.stylefeng.guns.rest.modular.education.responser.TeacherListResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 教师
 *
 * Created by 罗华.
 */
@Api(tags = "教师接口")
@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;


    /**
     * 获取教师管理列表
     */
    @ApiOperation(value="教师列表", httpMethod = "POST", response = TeacherListResponse.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "condition", value = "教师关键字", required = true, dataType = "String", example = "LS000001"),
            @ApiImplicitParam(name = "status", value = "状态", required = true, dataType = "String", example = "1")
    })
    @RequestMapping(value = "/list")
    @ResponseBody
    public Object list(@RequestParam Map<String, String> queryParams) {
        List<Teacher> teacherList = teacherService.selectList(new EntityWrapper<Teacher>() {
            {
                //condition条件分页
                if (queryParams.containsKey("condition") && StringUtils.isNotEmpty(queryParams.get("condition"))) {
                    like("name", queryParams.get("condition"));
                    or();
                    eq("code", queryParams.get("condition"));
                    or();
                    eq("mobile", queryParams.get("condition"));
                }

                if (StringUtils.isNotEmpty(queryParams.get("status"))){
                    try{
                        int status = Integer.parseInt(queryParams.get("status"));
                        eq("status", status);
                    }catch(Exception e){}
                }
            }
        });

        return TeacherListResponse.me(teacherList);
    }

    @ApiOperation(value="教师详情", httpMethod = "POST", response = TeacherDetailResponse.class)
    @ApiImplicitParam(name = "code", value = "教师编码", required = true, dataType = "String", example = "LS000001")
    @RequestMapping("/detail/{code}")
    @ResponseBody
    public Responser detail(@PathVariable("code") String code){

        Wrapper<Teacher> queryWrapper = new EntityWrapper<Teacher>();

        Teacher teacher = teacherService.selectOne(queryWrapper);

        return TeacherDetailResponse.me(teacher);
    }
}
