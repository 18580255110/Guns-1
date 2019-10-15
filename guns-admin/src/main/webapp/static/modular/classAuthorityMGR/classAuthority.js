/**
 * 管理初始化
 */
var ClassAuthority = {
    id: "ClassAuthorityTable",	//表格id
    seItem: null,		//选中的条目
    table: null,
    layerIndex: -1
};

/**
 * 初始化表格的列
 */
ClassAuthority.initColumn = function () {
    return [
        {field: 'selectItem', radio: true},
            {title: '主键', field: 'id', visible: true, align: 'center', valign: 'middle'},
            {title: '班级编码', field: 'classCode', visible: true, align: 'center', valign: 'middle'},
            {title: '班级名称', field: 'className', visible: true, align: 'center', valign: 'middle'},
            {title: '学生编码', field: 'studentCode', visible: true, align: 'center', valign: 'middle'},
            {title: '学生名称', field: 'studentName', visible: true, align: 'center', valign: 'middle'}
    ];
};

/**
 * 检查是否选中
 */
ClassAuthority.check = function () {
    var selected = $('#' + this.id).bootstrapTable('getSelections');
    if(selected.length == 0){
        Feng.info("请先选中表格中的某一记录！");
        return false;
    }else{
        ClassAuthority.seItem = selected[0];
        return true;
    }
};

/**
 * 点击添加班级
 */
ClassAuthority.openAddClassAuthority = function () {
    var index = layer.open({
        type: 2,
        title: '选择班级',
        area: ['1200px', '820px'], //宽高
        fix: false, //不固定
        maxmin: true,
        content: Feng.ctxPath + '/classAuthority/classAuthority_add'
    });
    layer.full(index);
    this.layerIndex = index;
};

/**
 * 打开查看详情
 */
ClassAuthority.openClassAuthorityDetail = function () {
    if (this.check()) {
        var index = layer.open({
            type: 2,
            title: '详情',
            area: ['800px', '420px'], //宽高
            fix: false, //不固定
            maxmin: true,
            content: Feng.ctxPath + '/classAuthority/classAuthority_update/' + ClassAuthority.seItem.id
        });
        this.layerIndex = index;
    }
};

/**
 * 删除
 */
ClassAuthority.delete = function () {
    if (this.check()) {
        var ajax = new $ax(Feng.ctxPath + "/classAuthority/delete", function (data) {
            Feng.success("删除成功!");
            ClassAuthority.table.refresh();
        }, function (data) {
            Feng.error("删除失败!" + data.responseJSON.message + "!");
        });
        ajax.set("classAuthorityId",this.seItem.id);
        ajax.start();
    }
};

/**
 * 查询列表
 */
ClassAuthority.search = function () {
    var queryData = {};
    queryData['condition'] = $("#condition").val();
    ClassAuthority.table.refresh({query: queryData});
};

$(function () {
    var defaultColunms = ClassAuthority.initColumn();
    var table = new BSTable(ClassAuthority.id, "/classAuthority/list", defaultColunms);
    table.setPaginationType("server");
    ClassAuthority.table = table.init();
});
