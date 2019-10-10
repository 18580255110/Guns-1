package com.stylefeng.guns.task.batch;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.modular.batchMGR.service.IBatchProcessDetailService;
import com.stylefeng.guns.modular.batchMGR.service.IBatchProcessService;
import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.memberMGR.service.IMemberService;
import com.stylefeng.guns.modular.orderMGR.OrderAddList;
import com.stylefeng.guns.modular.orderMGR.service.ICourseCartService;
import com.stylefeng.guns.modular.orderMGR.service.IOrderService;
import com.stylefeng.guns.modular.studentMGR.service.IStudentService;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.modular.system.model.Class;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.provider.symmetric.AES;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 批量报名
 *
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/3/30 11:22
 * @Version 1.0
 */
public class SignImportTask extends ImportTaskSupport {
    private static final Logger log = LoggerFactory.getLogger(SignImportTask.class);
    private static final int MIN_COLUMN_SIZE = 8;

    @Autowired
    private IBatchProcessService batchProcessService;

    @Autowired
    private IBatchProcessDetailService batchProcessDetailService;

    @Autowired
    private IClassService classService;

    @Autowired
    private IMemberService memberService;

    @Autowired
    private IStudentService studentService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private ICourseCartService courseCartService;

    @Scheduled(fixedDelay = 60000)
    public void handleSignImport(){
        log.info("<<< Import sign begin ");
        //
        Wrapper<BatchProcess> queryWrapper = new EntityWrapper<BatchProcess>();
        queryWrapper.eq("status", GenericState.Valid.code);
        queryWrapper.eq("service", BatchServiceEnum.Sign.code);
        queryWrapper.eq("work_status", BatchProcessStatusEnum.Prepare.code);

        BatchProcess preparedProcess = batchProcessService.selectOne(queryWrapper);

        if (null == preparedProcess) {
            log.warn("没有需处理的任务");
            return;
        }

        Wrapper<BatchProcessDetail> detailQueryWrapper = new EntityWrapper<BatchProcessDetail>();
        detailQueryWrapper.eq("batch_code", preparedProcess.getCode());
        detailQueryWrapper.eq("status", GenericState.Valid.code);
        detailQueryWrapper.eq("work_status", BatchProcessDetailStatusEnum.Create.code);

        List<BatchProcessDetail> processDetailList = batchProcessDetailService.selectList(detailQueryWrapper);

        if (null == processDetailList || processDetailList.isEmpty()){
            log.warn("批处理任务{}没有需要处理的数据", preparedProcess.getCode());
            preparedProcess.setWorkStatus(BatchProcessStatusEnum.Pass.code);
            batchProcessService.updateById(preparedProcess);
            return;
        }

        preparedProcess.setWorkStatus(BatchProcessStatusEnum.Working.code);
        batchProcessService.updateById(preparedProcess);
        int successCompleteCount = 0;

        for (BatchProcessDetail processDetail : processDetailList){
            Date handleBeginDate  = new Date();
            processDetail.setWorkStatus(BatchProcessDetailStatusEnum.Working.code);
            batchProcessDetailService.updateById(processDetail);

            SignRequest request = assembleSignData(processDetail.getData());
            try {
                doSign(request);
                processDetail.setWorkStatus(BatchProcessDetailStatusEnum.Pass.code);
                processDetail.setRemark(BatchProcessDetailStatusEnum.Pass.text);
                successCompleteCount++;
            }catch(Exception e){
                processDetail.setWorkStatus(BatchProcessDetailStatusEnum.Reject.code);
                processDetail.setRemark(e.getMessage());
            }finally{
                Date handleEndDate = new Date();
                processDetail.setDuration(handleEndDate.getTime() - handleBeginDate.getTime());
                processDetail.setCompleteDate(handleEndDate);
            }
            batchProcessDetailService.updateById(processDetail);
        }

        preparedProcess.setWorkStatus(BatchProcessStatusEnum.Pass.code);
        preparedProcess.setCompleteCount(successCompleteCount);
        preparedProcess.setCompleteDate(new Date());
        batchProcessService.updateById(preparedProcess);
        log.info(">>> Import sign task complete!");
    }

    private void doSign(SignRequest request) {
        Class classInfo = classService.get(request.getClassInfo().getCode());

        if (null == classInfo)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"班级信息"});

        Member member = request.getMember();
        Member currMember = memberService.getOrCreateIfNotExists(member.getMobileNumber(), member.getName());

        if (null == currMember)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"用户信息"});

        Student student = request.getStudent();
        Student currStudent = null;
        currStudent = studentService.get(student.getCode());
        if (null == currStudent) {
            Wrapper<Student> queryWrapper = new EntityWrapper<Student>();
            queryWrapper.eq("name", request.getStudent().getName());
            queryWrapper.eq("user_name", currMember.getUserName());
            queryWrapper.eq("status", GenericState.Valid.code);
            currStudent = studentService.selectOne(queryWrapper);

            if (null == currStudent)
                currStudent = studentService.addStudent(currMember.getUserName(), student);
        }

        if (null == currStudent)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"学员信息"});

        // 下订单
        String courseCartCode = courseCartService.doJoin(currMember, currStudent, classInfo, true, SignChannel.Admin, SignType.Normal);

        PayTypeEnum paytype = PayTypeEnum.AppPay;

        try {
            paytype = PayTypeEnum.instanceOf(request.getPayType());
        }catch(Exception e){}

        OrderItem orderItem = new OrderItem();
        orderItem.setCourseCartCode(courseCartCode);
        orderItem.setItemObject(OrderItemTypeEnum.Course.code);
        orderItem.setItemObjectCode(classInfo.getCode());
        orderItem.setItemAmount(classInfo.getPrice());
        OrderAddList orderAddList = new OrderAddList();
        orderAddList.add(orderItem);

        Map<String, Object> extendInfo = new HashMap<>();

        if (PayTypeEnum.ClassicPay.equals(paytype)){
            Order signOrder = orderService.order(currMember, orderAddList, PayMethodEnum.classic, extendInfo);
            orderService.completePay(signOrder.getAcceptNo());
        }else{
            orderService.order(currMember, orderAddList, PayMethodEnum.weixin, extendInfo);
        }
    }

    private SignRequest assembleSignData(String data) {
        if (null == data || StringUtils.isEmpty(data))
            throw new RuntimeException("没有导入数据");

        String[] requiredCols = data.split(",");
        if (MIN_COLUMN_SIZE > requiredCols.length)
            throw new RuntimeException("缺少必要的导入数据");

        SignRequest request = new SignRequest();

        Class signClass = new Class();
        signClass.setCode(getString(requiredCols, 0));
        request.setClassInfo(signClass);

        Member signMember = new Member();
        signMember.setMobileNumber(getString(requiredCols, 1));
        signMember.setName(getString(requiredCols, 2));
        request.setMember(signMember);

        Student signStudent = new Student();
        signStudent.setCode(getString(requiredCols, 3));
        signStudent.setName(getString(requiredCols, 4));
        signStudent.setGender(getMappingCode(requiredCols, 5));
        signStudent.setAge(getInteger(requiredCols, 6));
        signStudent.setAge(getInteger(requiredCols, 6));
        signStudent.setGrade(getMappingCode(requiredCols, 7));
        signStudent.setSchool(getString(requiredCols, 8));
        signStudent.setTargetSchool(getString(requiredCols, 9));
        request.setStudent(signStudent);

        Integer payType = Integer.parseInt(getMappingCode(requiredCols, 10));
        request.setPayType(payType);

        return request;
    }


}
