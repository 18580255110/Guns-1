package com.stylefeng.guns.rest.modular.member.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.modular.adjustMGR.service.IAdjustStudentService;
import com.stylefeng.guns.modular.memberMGR.service.IMemberService;
import com.stylefeng.guns.modular.memberMGR.service.IScoreService;
import com.stylefeng.guns.modular.studentMGR.service.IStudentService;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.rest.core.ApiController;
import com.stylefeng.guns.rest.core.Responser;
import com.stylefeng.guns.rest.modular.education.responser.ClassResponser;
import com.stylefeng.guns.rest.modular.member.requester.ScoreQueryRequest;
import com.stylefeng.guns.rest.modular.member.responser.MyClassListResponser;
import com.stylefeng.guns.rest.modular.member.responser.ScorePageResponse;
import com.stylefeng.guns.rest.modular.member.responser.ScoreResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.*;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2018/12/17 1:24
 * @Version 1.0
 */
@Api(tags = "个人中心")
@RestController
@RequestMapping("/my")
public class MyController extends ApiController {
    @Autowired
    private IMemberService memberService;

    @Autowired
    private IStudentService studentService;

    @Autowired
    private IScoreService scoreService;

    @Autowired
    private IAdjustStudentService adjustStudentService;

    @ApiOperation(value = "我的班级列表", httpMethod = "POST", response = MyClassListResponser.class)
    @RequestMapping(value = "/class/list", method = RequestMethod.POST)
    public Responser listMyClass(String student, boolean showHis) {
        Member member = currMember();

        Map<String, Set<Class>> myClasses = memberService.findMyClasses(member.getUserName(), student, showHis);

        List<ClassResponser> classList = new ArrayList<ClassResponser>();

        Iterator<String> myClassIterator = myClasses.keySet().iterator();
        while(myClassIterator.hasNext()){
            String studentCode = myClassIterator.next();

            for (Class classInfo : myClasses.get(studentCode)) {
                ClassResponser classResponser = ClassResponser.me(classInfo);
                classResponser.setAdjustCount(
                        adjustStudentService.selectCount(new EntityWrapper<AdjustStudent>(){
                            {
                                eq("status", GenericState.Valid.code);
                                eq("work_status", AdjustStudentApproveStateEnum.Appove.code);
                                eq("type", AdjustStudentTypeEnum.Adjust.code);
                                eq("student_code", studentCode);
                                eq("source_class", classInfo.getCode());
                            }
                        })
                );
                classResponser.judgement();

//                classResponser.setChangeCount(
//                        adjustStudentService.selectCount(new EntityWrapper<AdjustStudent>(){
//                            {
//                                eq("status", GenericState.Valid.code);
//                                eq("work_status", AdjustStudentApproveStateEnum.Appove.code);
//                                eq("type", AdjustStudentTypeEnum.Change.code);
//                                eq("source_class", classInfo.getCode());
//                            }
//                        })
//                );

                classResponser.setStudent(studentCode);
                classList.add(classResponser);
            }
        }

        return MyClassListResponser.me(classList);
    }


    @ApiOperation(value = "查分", httpMethod = "POST", response = ScorePageResponse.class)
    @RequestMapping(value = "/score/list", method = RequestMethod.POST)
    public Responser listMyScore(
            @RequestBody
            @Valid
            @ApiParam(required = true, value = "查分条件")
            ScoreQueryRequest requester) {
        Member member = currMember();

        Map<String, Object> queryParams = buildQueryArguments(requester.toMap());

        queryParams.put("member", member.getUserName());

        if (!(queryParams.containsKey("studentList"))) {
            List<String> studentQueryList = new ArrayList<>();
            List<Student> studentList = studentService.listStudents(member.getUserName());
            for(Student student : studentList){
                studentQueryList.add(student.getCode());
            }

            queryParams.put("studentList", studentQueryList);
        }
        Page<Score> scorePage  = scoreService.selectPage(queryParams, requester.getPage());

        List<ScoreResponse> scoreResponseList = new ArrayList<ScoreResponse>();
        for(Score score : scorePage.getRecords()){
            scoreResponseList.add(ScoreResponse.me(score));
        }

        return ScorePageResponse.me(scoreResponseList, scorePage);
    }


    private Map<String, Object> buildQueryArguments(Map<String, Object> queryParams) {
        Iterator<String> queryKeyIter = queryParams.keySet().iterator();
        Map<String, Object> arguments = new HashMap<String, Object>();

        List<String> studentList = new ArrayList<>();

        while(queryKeyIter.hasNext()){
            String key = queryKeyIter.next();
            Object value = queryParams.get(key);

            if (null == value)
                continue;

            if (String.class.equals(value.getClass())){
                if (StringUtils.isEmpty((String) value))
                    continue;
            }

            if ("student".equals(key)){
                studentList.add((String) value);

                arguments.remove("student");
                arguments.put("studentList", studentList);
            }

            arguments.put(key, queryParams.get(key));
        }
        return arguments;
    }
}
