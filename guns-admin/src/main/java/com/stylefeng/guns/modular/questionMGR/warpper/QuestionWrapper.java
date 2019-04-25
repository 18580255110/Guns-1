package com.stylefeng.guns.modular.questionMGR.warpper;

import com.stylefeng.guns.common.constant.factory.ConstantFactory;
import com.stylefeng.guns.core.base.warpper.BaseControllerWarpper;

import java.util.Map;

/**
 * 学生信息包装类
 * @author: simple.song
 * Date: 2018/10/7 Time: 10:55
 */
public class QuestionWrapper extends BaseControllerWarpper{


    public QuestionWrapper(Object obj) {
        super(obj);
    }

    @Override
    protected void warpTheMap(Map<String, Object> map) {
        map.put("statusName", ConstantFactory.me().getStatusName((Integer) map.get("status")));
        map.put("typeName", ConstantFactory.me().getQuestionTypeName((Integer) map.get("type")));
        map.put("subjectName", ConstantFactory.me().getsubjectName((Integer) map.get("subject")));
        map.put("gradeName", ConstantFactory.me().getGradeName(Integer.valueOf((String) map.get("grade"))));
        map.put("score", 0);
    }
}