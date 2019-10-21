package com.stylefeng.guns.modular.orderMGR.service;

import com.baomidou.mybatisplus.service.IService;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.modular.system.model.Class;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2018/11/15 22:46
 * @Version 1.0
 */
public interface ICourseCartService extends IService<CourseCart> {
    /**
     * 加入选课单
     *
     * @param member
     * @param student
     * @param classInfo
     * @param skipTest
     * @param channel
     * @param type
     */
    String doJoin(Member member, Student student, Class classInfo, boolean skipTest, SignChannel channel, SignType type);

    /**
     * 从选课单移除
     *
     * @param member
     * @param existStudent
     * @param classInfo
     */
    void remove(Member member, Student existStudent, Class classInfo);

    /**
     * 根据编码获取购课单
     * 
     * @param itemObjectCode
     * @return
     */
    CourseCart get(String itemObjectCode);

    /**
     * 生成订单回调
     *
     * @param userName
     * @param student
     * @param itemObjectCode
     */
    void generateOrder(String userName, String student, String itemObjectCode);

    /**
     * 自动预报
     *
     * @param classInfo
     */
    void doAutoPreSign(Class classInfo);

    /**
     * 移除购课单
     *
     * @param courseCartCode
     */
    void remove(String courseCartCode);
}
