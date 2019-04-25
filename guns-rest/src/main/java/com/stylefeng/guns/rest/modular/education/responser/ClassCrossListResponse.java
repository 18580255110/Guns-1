package com.stylefeng.guns.rest.modular.education.responser;

import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.rest.core.Responser;
import com.stylefeng.guns.rest.core.SimpleResponser;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.*;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/4/25 23:00
 * @Version 1.0
 */
@ApiModel(value = "ClassCrossListResponse", description = "可跨报班级列表")
public class ClassCrossListResponse extends SimpleResponser {

    @ApiModelProperty(name = "currentList", value = "当前班级集合")
    private Collection<ClassResponser> currentList = new ArrayList<ClassResponser>();
    @ApiModelProperty(name = "signList", value = "报名班级集合")
    private Collection<ClassResponser> signList = new ArrayList<ClassResponser>();
    @ApiModelProperty(name = "changeMapping", value = "可转班级集合")
    private Map<String, Collection<ClassResponser>> changeMapping = new HashMap<String, Collection<ClassResponser>>();

    public Collection<ClassResponser> getCurrentList() {
        return currentList;
    }

    public void setCurrentList(Collection<Class> currentList) {
        for(Class currClassInfo : currentList){
            this.currentList.add(ClassResponser.me(currClassInfo));
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

    private void setChangeMapping(Map<String, Collection<Class>> changeMapping) {
        Iterator<Map.Entry<String, Collection<Class>>> entryIter = changeMapping.entrySet().iterator();

        while(entryIter.hasNext()){
            Map.Entry entry = entryIter.next();
            String code = (String) entry.getKey();
            Set<Class> classInfoList = (Set<Class>) entry.getValue();
            Set<ClassResponser> classResponserSet = new HashSet<ClassResponser>();
            for(Class classInfo : classInfoList){
                classResponserSet.add(ClassResponser.me(classInfo));
            }
            this.changeMapping.put(code, classResponserSet);
        }
    }

    public Map<String, Collection<ClassResponser>> getChangeMapping() {
        return changeMapping;
    }

    public static Responser me(Set<Class> classSignSet, Set<Class> classChangeSet, Map<String, Collection<Class>> changeMapping) {
        ClassCrossListResponse response = new ClassCrossListResponse();
        response.setCode(SUCCEED);
        response.setMessage("查询成功");

        response.setSignList(classSignSet);
        response.setCurrentList(classChangeSet);
        response.setChangeMapping(changeMapping);
        return response;
    }

}
