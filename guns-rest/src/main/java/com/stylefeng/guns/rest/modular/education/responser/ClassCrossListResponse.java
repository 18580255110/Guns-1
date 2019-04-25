package com.stylefeng.guns.rest.modular.education.responser;

import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.rest.core.Responser;
import com.stylefeng.guns.rest.core.SimpleResponser;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/4/25 23:00
 * @Version 1.0
 */
@ApiModel(value = "ClassCrossListResponse", description = "可跨报班级列表")
public class ClassCrossListResponse extends SimpleResponser {

    @ApiModelProperty(name = "data", value = "转班班级集合")
    private Collection<ClassResponser> changeList = new ArrayList<ClassResponser>();
    @ApiModelProperty(name = "data", value = "报名班级集合")
    private Collection<ClassResponser> signList = new ArrayList<ClassResponser>();

    public Collection<ClassResponser> getChangeList() {
        return changeList;
    }

    public void setChangeList(Collection<Class> changeList) {
        for(Class changeClassInfo : changeList){
            this.changeList.add(ClassResponser.me(changeClassInfo));
        }
    }

    public Collection<ClassResponser> getSignList() {
        return signList;
    }

    public void setSignList(Collection<Class> signList) {
        for(Class signClassInfo : signList){
            this.signList.add(ClassResponser.me(signClassInfo));
        }
    }

    public static Responser me(Set<Class> classSignSet, Set<Class> classChangeSet) {
        ClassCrossListResponse response = new ClassCrossListResponse();
        response.setCode(SUCCEED);
        response.setMessage("查询成功");

        response.setSignList(classSignSet);
        response.setChangeList(classChangeSet);
        return response;
    }
}
