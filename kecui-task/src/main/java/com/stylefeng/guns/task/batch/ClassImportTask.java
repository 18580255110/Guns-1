package com.stylefeng.guns.task.batch;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.modular.batchMGR.service.IBatchProcessDetailService;
import com.stylefeng.guns.modular.batchMGR.service.IBatchProcessService;
import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.classMGR.service.ICourseService;
import com.stylefeng.guns.modular.classMGR.transfer.ClassPlan;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 批量开班
 *
 * 在 GunsTaskApplication 中创建Bean
 *
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/3/28 01:08
 * @Version 1.0
 */
public class ClassImportTask extends ImportTaskSupport{
    private static final Logger log = LoggerFactory.getLogger(ClassImportTask.class);
    private static final int MIN_COLUMN_SIZE = 17;
    @Autowired
    private IBatchProcessService batchProcessService;

    @Autowired
    private IBatchProcessDetailService batchProcessDetailService;

    @Autowired
    private IClassService classService;

    @Autowired
    private ICourseService courseService;

    @Scheduled(fixedDelay = 60000)
    public void handleClassImport(){
        log.info("<<< Examine check begin ");
        //
        Wrapper<BatchProcess> queryWrapper = new EntityWrapper<BatchProcess>();
        queryWrapper.eq("status", GenericState.Valid.code);
        queryWrapper.eq("service", BatchServiceEnum.Class.code);
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

            Map<String, Object> classMap = assembleClassData(processDetail.getData());

            try{
                if (!classMap.containsKey("classInfo"))
                    throw new Exception("数据导入失败: 班级信息解析失败");

                Class classInfo = (Class) classMap.get("classInfo");
                if (null == classInfo)
                    throw new Exception("数据导入失败: 班级信息解析失败");

                if (!classMap.containsKey("planList"))
                    throw new Exception("数据导入失败: 排班计划解析失败");

                List<ClassPlan> classPlanList = (List<ClassPlan>)classMap.get("planList");
                if (null == classPlanList || classPlanList.isEmpty())
                    throw new Exception("数据导入失败: 排班计划解析失败");

                Course courseInfo = courseService.get(classInfo.getCourseCode());
                if (null == courseInfo)
                    throw new Exception ("数据导入失败: 课程信息解析失败");

                if (!(courseInfo.getPeriod().equals(classPlanList.size())))
                    throw new Exception("数据导入失败: 排班计划数量与总课时数不符合");

                classInfo.setPeriod(courseInfo.getPeriod());
                classInfo.setGrade(courseInfo.getGrade());
                classInfo.setStar(1);
                classService.createClass(classInfo, classPlanList);
                processDetail.setWorkStatus(BatchProcessDetailStatusEnum.Pass.code);
                processDetail.setRemark(BatchProcessDetailStatusEnum.Pass.text);
                successCompleteCount++;

            }catch(Exception e){
                log.error(e.getMessage(), e);
                processDetail.setWorkStatus(BatchProcessDetailStatusEnum.Reject.code);
                processDetail.setRemark(e.getMessage());
            }finally{
                Date handleEndDate = new Date();
                processDetail.setDuration(handleEndDate.getTime() - handleBeginDate.getTime());
                processDetail.setCompleteDate(handleEndDate);
            }

            log.info("update batch process detail");
            batchProcessDetailService.updateById(processDetail);
        }

        preparedProcess.setWorkStatus(BatchProcessStatusEnum.Pass.code);
        preparedProcess.setCompleteCount(successCompleteCount);
        preparedProcess.setCompleteDate(new Date());
        log.info("update batch process with succeed");
        batchProcessService.updateById(preparedProcess);
        log.info(">>> Import class task complete!");
    }

    private Map<String, Object> assembleClassData(String data) {
        if (null == data || StringUtils.isEmpty(data))
            throw new RuntimeException("没有导入数据");

        if (data.indexOf("@_@") == -1 )
            throw new RuntimeException("导入数据无效");

        String[] datas = data.split("@_@");

        String[] requiredCols = datas[0].split(",");
        if (MIN_COLUMN_SIZE > requiredCols.length)
            throw new RuntimeException("缺少必要的导入数据");

        Map<String , Object> resultMap = new HashMap<String, Object>();

        Class classInfo = new Class();
        String[] courseInfo = getMapping(requiredCols, 0);
        classInfo.setCourseCode(courseInfo[0]);
        classInfo.setCourseName(courseInfo[1]);

        classInfo.setName(getString(requiredCols, 1));
        classInfo.setAcademicYear(getInteger(requiredCols, 2));
        classInfo.setCycle(getMappingCode(requiredCols, 3));
        classInfo.setAbility(getMappingCode(requiredCols, 4));

        String[] teacherInfo = getMapping(requiredCols, 5);
        classInfo.setTeacherCode(teacherInfo[0]);
        classInfo.setTeacher(teacherInfo[1]);

        String[] assisterInfo = getMapping(requiredCols, 6);
        classInfo.setTeacherSecondCode(assisterInfo[0]);
        classInfo.setTeacherSecond(assisterInfo[1]);

        classInfo.setQuato(getInteger(requiredCols, 7));

        String[] classroomInfo = getMapping(requiredCols, 8);
        classInfo.setClassRoomCode(classroomInfo[0]);
        classInfo.setClassRoom(classroomInfo[1]);

        classInfo.setPresignSourceClassCode(getString(requiredCols, 9));
        classInfo.setPresignStartDate(getDate(requiredCols, 10));
        classInfo.setPresignEndDate(getDate(requiredCols, 11));

        classInfo.setCrossable(Integer.parseInt(getMappingCode(requiredCols, 12)));
        classInfo.setCrossStartDate(getDate(requiredCols, 13));
        classInfo.setCrossEndDate(getDate(requiredCols, 14));

        classInfo.setSignable(GenericState.Valid.code);
        classInfo.setSignStartDate(getDate(requiredCols, 15));
        classInfo.setSignEndDate(getDate(requiredCols, 16));

        classInfo.setExaminable(Integer.parseInt(getMappingCode(requiredCols, 17)));
        classInfo.setStudyTimeDesp(getString(requiredCols, 18));
        classInfo.setPrice(getMoney(requiredCols, 19));

        resultMap.put("classInfo", classInfo);

        if (datas.length > 1 && StringUtils.isNotEmpty(datas[1])){
            List<ClassPlan> classPlanList = assembleClassPlanInfo(datas[1]);
            resultMap.put("planList", classPlanList);
        }

        return resultMap;
    }

    private List<ClassPlan> assembleClassPlanInfo(String classPlanStr) {
        StringTokenizer classPlanToken = new StringTokenizer(classPlanStr, "\n");

        List<ClassPlan> classPlanList = new ArrayList<ClassPlan>();
        while(classPlanToken.hasMoreTokens()){
            ClassPlan classPlan = new ClassPlan();

            String classPlanInfo = classPlanToken.nextToken();

            if(StringUtils.isEmpty(classPlanInfo))
                continue; // 没有排班信息，异常

            Pattern planPattern = Pattern.compile("^(\\[.*\\]|\\{.*\\}),\\[.*\\]$");
            Matcher m = planPattern.matcher(classPlanInfo);
            if (!m.matches())
                continue; // 没有正确设置排班时间

            Pattern datePattern = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}");
            Pattern timePattern = Pattern.compile("[0-9]{2}:[0-9]{2}:[0-9]{2}");

            Matcher dateMatcher = datePattern.matcher(classPlanInfo);
            Matcher timeMatcher = timePattern.matcher(classPlanInfo);

            String[] timeRange = new String[2];
            int timeCount = 0;
            while(timeMatcher.find()){
                timeRange[timeCount++] = timeMatcher.group();
            }

            Set<String> dateRangeSet = new HashSet<String>();
            while(dateMatcher.find()){
                dateRangeSet.add(dateMatcher.group());
            }

            if (2 != timeCount)
                continue; // 时间不正确

            String[] dateRange = dateRangeSet.toArray(new String[dateRangeSet.size()]);
            if (classPlanInfo.startsWith("[")){
                if (2 != dateRangeSet.size())
                    continue;
                classPlanList.addAll(parseRangePlanList(dateRange, timeRange));
            }else if (classPlanInfo.startsWith("{")){
                classPlanList.addAll(parseEnumPlanList(dateRange, timeRange));
            }
/*
            String[] classPlans = classPlanInfo.split(",");

            Date beginTime = null;
            Date endTime = null;
            try {
                beginTime = DateUtils.parseDate(, new String[]{"yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss"});
            }catch(Exception e){}
            try {
                endTime = DateUtils.parseDate(classPlans[1], new String[]{"yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss"});
            }catch(Exception e){}

            if (null == beginTime || null == endTime)
                continue; // 这里不抛出异常，在后面通过检查排班记录数量

            if (null != beginTime)
                classPlan.setClassTime(DateUtil.format(beginTime, "HHmm"));
            if (null != endTime)
                classPlan.setEndTime(DateUtil.format(endTime, "HHmm"));

            classPlan.setStudyDate(DateUtils.truncate(beginTime, Calendar.DAY_OF_MONTH));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(classPlan.getStudyDate());
            classPlan.setWeek(calendar.get(Calendar.DAY_OF_WEEK) - 1);

            classPlanList.add(classPlan);*/
        }

        return classPlanList;
    }

    private List<? extends ClassPlan> parseEnumPlanList(String[] dateRange, String[] timeMatcher) {

        List<ClassPlan> classPlanList = new ArrayList<ClassPlan>();

        for(int idx = 0; idx < dateRange.length; idx++ ){
            ClassPlan classPlan = new ClassPlan();
            String date = dateRange[idx];
            String beginPlanDate = date + " " + timeMatcher[0];
            String endPlanDate = date + " " + timeMatcher[1];
            Date beginTime = null;
            Date endTime = null;

            try {
                beginTime = DateUtils.parseDate(beginPlanDate, new String[]{"yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss"});
            }catch(Exception e){}
            try {
                endTime = DateUtils.parseDate(endPlanDate, new String[]{"yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss"});
            }catch(Exception e){}

            if (null == beginTime || null == endTime)
                continue; // 这里不抛出异常，在后面通过检查排班记录数量

            if (null != beginTime)
                classPlan.setClassTime(DateUtil.format(beginTime, "HHmm"));
            if (null != endTime)
                classPlan.setEndTime(DateUtil.format(endTime, "HHmm"));

            classPlan.setStudyDate(DateUtils.truncate(beginTime, Calendar.DAY_OF_MONTH));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(classPlan.getStudyDate());
            classPlan.setWeek(calendar.get(Calendar.DAY_OF_WEEK) - 1);

            classPlanList.add(classPlan);
        }

        return classPlanList;
    }

    private List<? extends ClassPlan> parseRangePlanList(String[] dateRange, String[] timeRange) {

        Date beginDate = null;
        Date endDate = null;
        List<ClassPlan> classPlanList = new ArrayList<ClassPlan>();

        try {
            beginDate = DateUtils.parseDate(dateRange[0], new String[]{"yyyy-MM-dd", "yyyy/MM/dd"});
        }catch(Exception e){}

        try {
            endDate = DateUtils.parseDate(dateRange[1], new String[]{"yyyy-MM-dd", "yyyy/MM/dd"});
        }catch(Exception e){}

        if (null == beginDate || null == endDate)
            return classPlanList;

        if (beginDate.compareTo(endDate) > 0){
            Date temp = beginDate;
            beginDate = endDate;
            endDate = temp;
        }

        while(beginDate.compareTo(endDate) <= 0){

            ClassPlan classPlan = new ClassPlan();
            String dateStr = DateUtil.format(beginDate, "yyyy-MM-dd");
            String beginPlanDate = dateStr + " " + timeRange[0];
            String endPlanDate = dateStr + " " + timeRange[1];
            Date beginTime = null;
            Date endTime = null;

            try {
                beginTime = DateUtils.parseDate(beginPlanDate, new String[]{"yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss"});
            }catch(Exception e){}
            try {
                endTime = DateUtils.parseDate(endPlanDate, new String[]{"yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss"});
            }catch(Exception e){}

            if (null == beginTime || null == endTime)
                continue; // 这里不抛出异常，在后面通过检查排班记录数量

            if (null != beginTime)
                classPlan.setClassTime(DateUtil.format(beginTime, "HHmm"));
            if (null != endTime)
                classPlan.setEndTime(DateUtil.format(endTime, "HHmm"));

            classPlan.setStudyDate(DateUtils.truncate(beginTime, Calendar.DAY_OF_MONTH));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(classPlan.getStudyDate());
            classPlan.setWeek(calendar.get(Calendar.DAY_OF_WEEK) - 1);

            classPlanList.add(classPlan);

            beginDate = DateUtil.add(beginDate, Calendar.DAY_OF_MONTH, 1);
        }

        return classPlanList;
    }
}
