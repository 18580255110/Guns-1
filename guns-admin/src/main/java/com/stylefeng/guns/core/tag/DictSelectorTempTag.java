package com.stylefeng.guns.core.tag;

import com.stylefeng.guns.common.exception.BizExceptionEnum;
import com.stylefeng.guns.core.exception.GunsException;
import com.stylefeng.guns.modular.system.model.Dict;
import com.stylefeng.guns.modular.system.service.IDictService;
import com.stylefeng.guns.util.ToolUtil;
import org.beetl.core.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/10/15 08:07
 * @Version 1.0
 */
@Component
@Scope("prototype")
public class DictSelectorTempTag extends Tag {

    @Autowired
    IDictService dictService;

    @Override
    public void render() {
        Map attrs = (Map) args[1];
        if(ToolUtil.isEmpty(attrs.get("code"))){
            throw new GunsException(BizExceptionEnum.ERROR_CODE_EMPTY);
        }

        //字典类型编码
        String code = attrs.get("code").toString();
        //控件显示类型select 选择框,radio 单选按钮,checkbox 多选按钮
        String type = ToolUtil.isNotEmpty(attrs.get("type"))?attrs.get("type").toString():"select";
        //字典名称
        String label = ToolUtil.isNotEmpty(attrs.get("label"))?attrs.get("label").toString():"";
        //提示
        String placeholder = (ToolUtil.isNotEmpty(attrs.get("placeholder"))?attrs.get("placeholder").toString():"");
        //宽度
        String width = ToolUtil.isNotEmpty(attrs.get("width"))?attrs.get("width").toString():"248";
        String labelColWidth = ToolUtil.isNotEmpty(attrs.get("labelColWidth"))?attrs.get("labelColWidth").toString():"1";
        String selectColWidth = ToolUtil.isNotEmpty(attrs.get("selectColWidth"))?attrs.get("selectColWidth").toString():"1";
        //默认值
        String value = ToolUtil.isNotEmpty(attrs.get("value"))?attrs.get("value").toString():"";
        //id
        String id = ToolUtil.isNotEmpty(attrs.get("id"))?attrs.get("id").toString():"";
        //name
        String name = ToolUtil.isNotEmpty(attrs.get("name"))?attrs.get("name").toString():"";
        String elid = ToolUtil.isNotEmpty(attrs.get("elid"))?attrs.get("elid").toString():"id";
        //分割线
        String underline = ToolUtil.isNotEmpty(attrs.get("underline"))?attrs.get("underline").toString():"";
        //searchnum 下拉选项数量达到多少启用搜索,默认10
        int searchnum = ToolUtil.isNum(attrs.get("searchnum"))?Integer.parseInt(attrs.get("searchnum").toString()):10;
        //startNum 下拉选项数量达到多少启用搜索,默认10
        String startNum = ToolUtil.isNotEmpty(attrs.get("startNum"))?attrs.get("startNum").toString():"0";
        //endNum 下拉选项数量达到多少启用搜索,默认10
        String endNum = ToolUtil.isNotEmpty(attrs.get("endNum"))?attrs.get("endNum").toString():"50";
        //根据code查询字典数据
        List<Dict> list = dictService.selectByParentCodeAndLimit(code,startNum,endNum);

        StringBuffer html = new StringBuffer();
        //html.append("<label class=\"col-xs-"+labelColWidth+" control-label\">"+name+"</label>\r\n");
        html.append("<div class=\"col-xs-"+selectColWidth+"\">\r\n");
        html.append("<select class=\"form-control\" "+elid+"=\""+id+"\">\r\n");

        if(ToolUtil.isNotEmpty(placeholder)){
            html.append("<option value=\"\">"+placeholder+"</option>\r\n");
        }
        //将查询出来的数据添加到select中
        list.forEach(obj -> {
            if (ToolUtil.isNotEmpty(value) && value.equals(obj.getCode())) {
                html.append("<option selected value=\"" + obj.getCode() + "\">" + obj.getName() + "</option>\r\n");
            } else {
                html.append("<option value=\"" + obj.getCode() + "\">" + obj.getName() + "</option>\r\n");
            }
        });

        html.append("</select>\r\n</div>\r\n");

        try{
            this.ctx.byteWriter.writeString(html.toString());
        }catch (IOException e){
            throw new RuntimeException("输出字典标签错误");
        }
    }
}
