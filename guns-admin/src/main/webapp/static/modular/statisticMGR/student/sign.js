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
        {title: '订单编号', field: 'orderNo', visible: false, align: 'center', valign: 'middle'},
        {title: '学员名称', field: 'studentName', visible: true, align: 'center', valign: 'middle'},
        {title: '家长电话', field: 'memberMobile', visible: true, align: 'center', valign: 'middle'},
        {title: '所报班级', field: 'className', visible: true, align: 'center', valign: 'middle'},
        {title: '实缴金额（元）', field: 'amount', visible: true, align: 'center', valign: 'middle'},
        {title: '支付方式', field: 'payMethodName', visible: true, align: 'center', valign: 'middle'},
        {title: '备注', field: 'desc', visible: true, align: 'center', valign: 'middle'},
        {title: '任课老师', field: 'teacher', visible: true, align: 'center', valign: 'middle'},
        {title: '报名时间', field: 'signDate', visible: true, align: 'center', valign: 'middle'}
    ];
};
/**
 * 检查是否选中
 */
StudentSign.check = function () {
    var selected = $('#' + this.id).bootstrapTable('getSelections');
    if(selected.length == 0){
        Feng.info("请先选中表格中的某一记录！");
        return false;
    }else{
        StudentSign.seItem = selected[0];
        return true;
    }
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
    queryData['academicYear'] = $("#academicYear").val();
    ajax.setData(queryData);
    ajax.start();
};
/**
 * 退费导出订单
 */
StudentSign.cancelExport = function () {
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
    queryData['academicYear'] = $("#academicYear").val();
    queryData['orderStatus'] = 2;
    ajax.setData(queryData);
    ajax.start();
};
/**
 * 退费
 */
StudentSign.cancel = function () {

    if (this.check()) {

        var remark = parent.layer.prompt({
            formType: 0,
            value: '退费金额:',
            title: '确定退费吗？'
        }, function(value,index){
            if('退费金额(必填):' == value || !value){
                Feng.alert("请填写实际退费金额")
                return;
            }else {
                var ajax = new $ax(Feng.ctxPath + "/order/class/doReverse/" + StudentSign.seItem.orderNo, function (data) {
                    Feng.success("操作成功!");
                    StudentSign.table.refresh();
                }, function (data) {
                    Feng.error("操作失败!" + data.responseJSON.message + "!");
                });
                ajax.setData({desc:value})
                ajax.start();
            }
            parent.layer.close(index);
        });
    }
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
    queryData['academicYear'] = $("#academicYear").val();

    StudentSign.table.refresh({query: queryData});
    StudentSign.table.setQueryParams(queryData);
};
/**
 * 查询《退费》订单管理列表
 */
StudentSign.cancelSearch = function () {
    var queryData = {};
    queryData['teacher'] = $("#teacher").val();
    queryData['student'] = $("#student").val();
    queryData['subject'] = $("#subject").val();
    queryData['ability'] = $("#ability").val();
    queryData['cycle'] = $("#cycle").val();
    queryData['grade'] = $("#grade").val();
    queryData['classInfo'] = $("#classInfo").val();
    queryData['orderStatus'] = 2;
    queryData['academicYear'] = $("#academicYear").val();

    StudentSign.table.refresh({query: queryData});
    StudentSign.table.setQueryParams(queryData);
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
    queryData['academicYear'] = $("#academicYear").val();

    return queryData;
}

$(function () {
    var defaultColunms = StudentSign.initColumn();
    var table = new BSTable(StudentSign.id, "/statistic/student/sign/list", defaultColunms);
    table.setPaginationType("server");
    table.setQueryParamsGetter(StudentSign.formParams);
    StudentSign.table = table.init();
});
