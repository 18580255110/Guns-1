package com.stylefeng.guns.rest.modular.examine.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.examineMGR.service.*;
import com.stylefeng.guns.modular.studentMGR.service.IStudentService;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.modular.system.service.IAttachmentService;
import com.stylefeng.guns.rest.core.ApiController;
import com.stylefeng.guns.rest.core.Responser;
import com.stylefeng.guns.rest.core.SimpleResponser;
import com.stylefeng.guns.rest.modular.examine.requester.*;
import com.stylefeng.guns.rest.modular.examine.responser.*;
import com.stylefeng.guns.util.ToolUtil;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.*;

/**
 * 测试
 *
 * Created by 罗华.
 */
@RestController
@RequestMapping("/examine")
@Api(tags = "考试接口")
public class ExamController extends ApiController {

    @Autowired
    private IClassService classService;

    @Autowired
    private IStudentService studentService;

    @Autowired
    private IExamineService examineService;

    @Autowired
    private IQuestionService questionService;

    @Autowired
    private IQuestionItemService questionItemService;

    @Autowired
    private IAttachmentService attachmentService;

    @Autowired
    private IExaminePaperService examinePaperService;

    @Autowired
    private IExamineApplyService examineApplyService;

    @ApiOperation(value="班级所需试卷", httpMethod = "POST", response = ExaminePaperDetailResponse.class)
    @RequestMapping(value = "/paper/findone", method = RequestMethod.POST)
    public Responser getPaper(
            @RequestBody
            @Valid
            ClassPaperFinderRequester requester
    ){
        Member member = currMember();

        Class classInfo = classService.get(requester.getClassCode());

        if (null == classInfo)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"班级信息"});

        Student currStudent = null;
        if (ToolUtil.isEmpty(requester.getStudent())){
            List<Student> studentList = studentService.listStudents(member.getUserName());
            for(Student student : studentList){
                if (classInfo.getGrade().equals(student.getGrade())){
                    currStudent = student;
                    break;
                }
            }
        }else{
            currStudent = studentService.get(requester.getStudent());
        }

        if (null == currStudent)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"学员信息"});

        // 查找符合当前学员和班级的试卷
        Map<String, Object> queryParams = requester.toMap();
        ExamineApply apply = examineService.findExaminePaper(queryParams);

        if (null == apply)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"试卷"});

        ExaminePaper paper = examinePaperService.get(apply.getPaperCode());

        if (null == paper)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"试卷"});

        return ExaminePaperDetailResponse.me(ExaminePaperDetail.me(paper, apply));
    }

    @ApiOperation(value="试卷列表", httpMethod = "POST", response = PaperListResponse.class)
    @RequestMapping(value = "/paper/list", method = RequestMethod.POST)
    public Responser listPaper(
            @RequestBody
            @Valid
            StudentPaperFinderRequester requester
    ){
        Member member = currMember();

        if (ToolUtil.isEmpty(requester.getStudent()) || ToolUtil.isEmpty(requester.getStudent().getCode())){
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"学员编码"});
        }

        Student student = studentService.get(requester.getStudent().getCode());

        if (null == student || !(student.isValid()))
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"学员"});

        List<Student> studentList = studentService.listStudents(member.getUserName());
        boolean isMemberStudent = false;
        for(Student studentMember : studentList){
            if (studentMember.getCode().equals(requester.getStudent().getCode())){
                isMemberStudent = true;
                break;
            }
        }

        if (!isMemberStudent)
            throw new ServiceException(MessageConstant.MessageCode.SYS_DATA_ILLEGAL, new String[]{"没有该学员信息"});

        Set<ExamineAnswerPaperResponse> paperResponseList = new HashSet<>();
        // 查找学员的试卷列表
        Collection<Map<String, Object>> examineAnswerPaperList = examineService.findExamineAnswerPaperList(student.getCode());
        for(Map<String, Object> examineAnswerPaper : examineAnswerPaperList){
            paperResponseList.add(ExamineAnswerPaperResponse.me(examineAnswerPaper));
        }

        return ExamineAnswerPaperListResponse.me(paperResponseList);
    }

    @ApiOperation(value="开始测试", httpMethod = "POST", response = ExamineOutlineResponse.class)
    @RequestMapping("/begin")
    public Responser beginExamine(
            @ApiParam(required = true, value = "开始测试请求")
            @RequestBody
            @Valid
            BeginExamineRequester requester
    ){
        Member member = currMember();

        Student student = studentService.get(requester.getStudent());

        ExaminePaper paper = examineService.getExaminePaper(requester.getPaperCode());

        ExamineApply apply = examineApplyService.selectById(requester.getApplyId());

        Map<String, Collection<Question>> beginResult = examineService.doBeginExamine(student, paper, apply);

        return ExamineOutlineResponse.me(beginResult);
    }

    @ApiOperation(value="试题详情", response = QuestionDetailResponse.class)
    @RequestMapping(value = "/question/detail/{code}", method = {RequestMethod.POST})
    public Responser paperDetail(@PathVariable("code") String code){

        Question question = questionService.get(code);

        List<QuestionItem> questionItemList = questionItemService.findAll(question.getCode());

        QuestionResponse questionResponse = QuestionResponse.me(question);
        questionResponse.setItems(questionItemList);

        return QuestionDetailResponse.me(questionResponse);
    }

    @ApiOperation(value="提交试卷", httpMethod = "POST", response = SimpleResponser.class)
    @RequestMapping("/paper/submit")
    public Responser submitPaper(@RequestBody ExamPaperSubmitRequester requester){

        ExamineAnswer answerPaper = examineService.getAnswerPaper(requester.getCode());

        if (null == answerPaper)
            throw new ServiceException(MessageConstant.MessageCode.EXAMINE_SUBMIT_FAILED, new String[]{"请重试"});

        requester.parseSubmit();

        List<ExamineAnswerDetail> detailList = new ArrayList<>();
        for(QuestionRequester qr : requester.getSubmitItems()) {
            ExamineAnswerDetail answerDetail = new ExamineAnswerDetail();

            int score = examineService.getQuestionScore(answerPaper.getPaperCode(), qr.getCode());

            answerDetail.setTotalScore(score);
            answerDetail.setAnswerCode(requester.getCode());
            answerDetail.setQuestionCode(qr.getCode());
            answerDetail.setStudentAnswer(qr.getAnswer());
            answerDetail.setAnswer(qr.getExpactAnswer());
            answerDetail.setStatus(GenericState.Valid.code);

            detailList.add(answerDetail);
        }

        examineService.doFinishExamine(requester.getCode(), detailList);

        return SimpleResponser.success();
    }

    @ApiOperation(value="获取答案图片", httpMethod = "POST")
    @ApiImplicitParams(
            @ApiImplicitParam(name = "id", value = "答案项ID", required = true, dataType = "String", example = "301")
    )
    @RequestMapping(value = "/view/question/item/{id}", method = RequestMethod.POST)
    public void download(@PathVariable("id")String id, HttpServletResponse response) {
        File zipFile = null;

        Wrapper<Attachment> attachmentWrapper = new EntityWrapper<>();
        attachmentWrapper.eq("master_name", "QuestionItem");
        attachmentWrapper.eq("master_code", id);
        attachmentWrapper.eq("status", GenericState.Valid.code);

        Attachment attachment = attachmentService.selectOne(attachmentWrapper);

        if (null == attachment)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS);

        InputStream resStream = null;
        String fileName = "";

        try {

            resStream = new FileInputStream(new File(attachment.getPath()));
            fileName = attachment.getFileName();

        }catch(Exception e){}

        if (null == resStream)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS);

        try {
            // 输出文件
            response.reset();
            response.setContentType("bin");
            response.addHeader("Content-Disposition",
                    "attachment; filename=\"" + new String(URLEncoder.encode(fileName, "UTF-8")) + "\"");
            byte[] buffer = new byte[1024];
            int byteread = 0;
            while ((byteread = resStream.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, byteread);
            }

        } catch (Exception e) {
            // e.printStackTrace();
        } finally {
            try {
                resStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
