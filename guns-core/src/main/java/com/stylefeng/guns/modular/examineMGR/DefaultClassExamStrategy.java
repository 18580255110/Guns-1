package com.stylefeng.guns.modular.examineMGR;

import com.stylefeng.guns.modular.system.model.ClassExamStrategy;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.FactoryBean;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2018/12/2 16:49
 * @Version 1.0
 */
public class DefaultClassExamStrategy implements FactoryBean<ClassExamStrategy> {

    private Integer zero = 0;

    private DefaultClassExamStrategyProperties config;

    public void setConfig(DefaultClassExamStrategyProperties config) {
        this.config = config;
    }

    @Override
    public ClassExamStrategy getObject() throws Exception {
        ClassExamStrategy strategy = new ClassExamStrategy();
        BeanUtils.copyProperties(config, strategy);

        if (strategy.getSubjectRatio().compareTo(zero) <= 0)
            strategy.setAutoMarking(1);
        else
            strategy.setAutoMarking(0);
        return strategy;
    }

    @Override
    public Class<?> getObjectType() {
        return ClassExamStrategy.class;
    }
}
