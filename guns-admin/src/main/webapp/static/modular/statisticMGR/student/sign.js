/**
 * 订单管理管理初始化
 */
var StudentSign = {
    id: "StudentSignTable",	//表格id
    seItem: null,		//选中的条目
    table: null,
    layerIndex: -1
};

/**
 * 初始化表格的列
 */
StudentSign.initColumn = function () {
    return [
        {field: 'selectItem', radio: true},
        {title: '学员编号', field: 'studentCode', visible: true, align: 'center', valign: 'middle'},
        {title: '学员名称', field: 'studentName', visible: true, align: 'center', valign: 'middle'},
        {title: '家长电话', field: 'memberMobile', visible: true, align: 'center', valign: 'middle'},
        {title: '所报班级', field: 'className', visible: true, align: 'center', valign: 'middle'},
        {title: '报名时间', field: 'signDate', visible: true, align: 'center', valign: 'middle'}
    ];
};

/**
 * 导出订单
 */
StudentSign.export = function () {
    var ajax = new $ax(Feng.ctxPath + "/statistic/student/sign/export", function (data) {
        //Feng.success("导出成功!");
        window.location.href = encodeURI(data.message);
    }, function (data) {
        Feng.error("导出失败!" + data.responseJSON.message + "!");
    });

    var queryData = {};
    queryData['teacher'] = $("#teacher").val();
    queryData['student'] = $("#student").val();
    queryData['subject'] = $("#subject").val();
    queryData['ability'] = $("#ability").val();
    queryData['cycle'] = $("#cycle").val();
    queryData['grade'] = $("#grade").val();
    queryData['classInfo'] = $("#classInfo").val();
    ajax.setData(queryData);
    ajax.start();
};

/**
 * 查询订单管理列表
 */
StudentSign.search = function () {
    var queryData = {};
    queryData['teacher'] = $("#teacher").val();
    queryData['student'] = $("#student").val();
    queryData['subject'] = $("#subject").val();
    queryData['ability'] = $("#ability").val();
    queryData['cycle'] = $("#cycle").val();
    queryData['grade'] = $("#grade").val();
    queryData['classInfo'] = $("#classInfo").val();

    StudentSign.table.refresh({query: queryData});
};

/**
 * 查询表单提交参数对象
 * @returns {{}}
 */
StudentSign.formParams = function() {
    var queryData = {};
    queryData['teacher'] = $("#teacher").val();
    queryData['student'] = $("#student").val();
    queryData['subject'] = $("#subject").val();
    queryData['ability'] = $("#ability").val();
    queryData['cycle'] = $("#cycle").val();
    queryData['grade'] = $("#grade").val();
    queryData['classInfo'] = $("#classInfo").val();

    return queryData;
}

$(function () {
    var defaultColunms = StudentSign.initColumn();
    var table = new BSTable(StudentSign.id, "/statistic/student/sign/list", defaultColunms);
    table.setPaginationType("server");
    table.setQueryParamsGetter(StudentSign.formParams);
    StudentSign.table = table.init();
});
