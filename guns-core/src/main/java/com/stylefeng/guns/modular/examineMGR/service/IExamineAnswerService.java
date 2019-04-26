package com.stylefeng.guns.modular.examineMGR.service;

import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.IService;
import com.stylefeng.guns.modular.system.model.ExamineAnswer;
import com.stylefeng.guns.modular.system.model.ExamineApply;
import com.stylefeng.guns.modular.system.model.ExaminePaper;
import com.stylefeng.guns.modular.system.model.Student;

import java.util.Map;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2018/12/22 16:35
 * @Version 1.0
 */
public interface IExamineAnswerService extends IService<ExamineAnswer> {
    /**
     * 创建答卷
     *
     * @param student
     * @param examinePaper
     * @return
     */
    ExamineAnswer generatePaper(Student student, ExaminePaper examinePaper, ExamineApply apply);

    /**
     * 获取答卷
     *
     * @param code
     */
    ExamineAnswer get(String code);

    /**
     * 试卷是否在用
     *
     * @param paper
     * @return
     */
    boolean paperOnair(ExaminePaper paper);

    /**
     * 查询答卷
     *
     * @param conditionMap
     * @return
     */
    Page<Map<String,Object>> selectMapsPage(Map<String, Object> conditionMap);

    /**
     * 自动阅卷
     *
     * @param examineAnswer
     */
    void doAutoCheckPaper(ExamineAnswer examineAnswer);
}
