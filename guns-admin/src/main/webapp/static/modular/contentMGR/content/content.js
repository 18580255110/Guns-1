/**
 * 资讯管理管理初始化
 */
var Content = {
    id: "ContentTable",	//表格id
    seItem: null,		//选中的条目
    table: null,
    layerIndex: -1
};

/**
 * 初始化表格的列
 */
Content.initColumn = function () {
    return [
        {field: 'selectItem', radio: true},
            {title: '内容编码', field: 'code', visible: false, align: 'center', valign: 'middle'},
            {title: '类型', field: 'typeName', visible: true, align: 'center', valign: 'middle', sortable: true},
            {title: '标题图片', field: 'timage', visible: true, align: 'center', valign: 'middle',
                formatter:function (value,row,index) {
                    return '<img alt="image" class="img-circle" src="'+Feng.ctxPath+'/attachment/download?masterName=Content&masterCode='+row.id+'" width="64px" height="64px">';
                }
            },
            {title: '标题', field: 'title', visible: true, align: 'center', valign: 'middle'},
            {title: '一句话简介', field: 'introduce', visible: true, align: 'center', valign: 'middle'},
            {title: '作者', field: 'author', visible: true, align: 'center', valign: 'middle', sortable: true},
            {title: '发布类型', field: 'publishTypeName', visible: true, align: 'center', valign: 'middle'},
            {title: '创建时间', field: 'createDate', visible: true, align: 'center', valign: 'middle'},
            {title: '下架时间', field: 'deadDate', visible: true, align: 'center', valign: 'middle', sortable: true},
    ];
};

/**
 * 检查是否选中
 */
Content.check = function () {
    var selected = $('#' + this.id).bootstrapTable('getSelections');
    if(selected.length == 0){
        Feng.info("请先选中表格中的某一记录！");
        return false;
    }else{
        Content.seItem = selected[0];
        return true;
    }
};

/**
 * 点击添加资讯管理
 */
Content.openAddContent = function () {
    var index = layer.open({
        type: 2,
        title: '添加资讯管理',
        area: ['800px', '420px'], //宽高
        fix: false, //不固定
        maxmin: true,
        content: Feng.ctxPath + '/content/content_add'
    });
    layer.full(index);
    this.layerIndex = index;
};

/**
 * 打开查看资讯管理详情
 */
Content.openContentDetail = function () {
    if (this.check()) {
        var index = layer.open({
            type: 2,
            title: '资讯管理详情',
            area: ['800px', '420px'], //宽高
            fix: false, //不固定
            maxmin: true,
            content: Feng.ctxPath + '/content/content_update/' + Content.seItem.id
        });
        layer.full(index);
        this.layerIndex = index;
    }
};

/**
 * 删除资讯管理
 */
Content.delete = function () {
    if (this.check()) {
        var ajax = new $ax(Feng.ctxPath + "/content/delete", function (data) {
            Feng.success("删除成功!");
            Content.table.refresh();
        }, function (data) {
            Feng.error("删除失败!" + data.responseJSON.message + "!");
        });
        ajax.set("contentId",this.seItem.id);
        ajax.start();
    }
};

/**
 * 查询资讯管理列表
 */
Content.search = function () {
    var queryData = {};
    queryData['condition'] = $("#condition").val();
    Content.table.refresh({query: queryData});
    Content.table.setQueryParams(queryData);
};

$(function () {
    var defaultColunms = Content.initColumn();
    var table = new BSTable(Content.id, "/content/list", defaultColunms);
    table.setPaginationType("server");
    Content.table = table.init();
});
