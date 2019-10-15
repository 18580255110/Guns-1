/**
 * 订单管理管理初始化
 */
var Order = {
    id: "OrderTable",	//表格id
    seItem: null,		//选中的条目
    table: null,
    layerIndex: -1
};

/**
 * 初始化表格的列
 */
Order.initColumn = function () {
    return [
        {field: 'selectItem', radio: true},
            {title: '标示', field: 'id', visible: false, align: 'center', valign: 'middle'},
            {title: '订单号', field: 'acceptNo', visible: true, align: 'center', valign: 'middle'},
            {title: '学员', field: 'studentName', visible: true, align: 'center', valign: 'middle'},
            {title: '所报班级', field: 'className', visible: true, align: 'center', valign: 'middle'},
            {title: '教授老师', field: 'teacherName', visible: true, align: 'center', valign: 'middle'},
            {title: '开班金额（元）', field: 'oldAmount', visible: true, align: 'center', valign: 'middle'},
            {title: '实缴金额（元）', field: 'amount', visible: true, align: 'center', valign: 'middle'},
            {title: '备注', field: 'desc', visible: true, align: 'center', valign: 'middle'},
            {title: 'status', field: 'status', visible: false, align: 'center', valign: 'middle'},
            {title: '状态', field: 'statusName', visible: true, align: 'center', valign: 'middle'},
            {title: '支付结果', field: 'payResult', visible: true, align: 'center', valign: 'middle'},
            {title: '支付方式', field: 'payMethodName', visible: true, align: 'center', valign: 'middle'},
            {title: '生成时间', field: 'acceptDate', visible: true, align: 'center', valign: 'middle'},
            {title: '支付时间', field: 'payDate', visible: true, align: 'center', valign: 'middle'}
    ];
};

/**
 * 检查是否选中
 */
Order.check = function () {
    var selected = $('#' + this.id).bootstrapTable('getSelections');
    if(selected.length == 0){
        Feng.info("请先选中表格中的某一记录！");
        return false;
    }else{
        Order.seItem = selected[0];
        return true;
    }
};

/**
 * 点击添加订单管理
 */
Order.openOrderManager = function () {
    if (this.check()) {
        var index = layer.open({
            type: 2,
            title: '订单管理详情',
            area: ['640px', '480px'], //宽高
            fix: false, //不固定
            maxmin: true,
            content: Feng.ctxPath + '/order/class/order_update/' + Order.seItem.acceptNo
        });
        layer.full(index);
        this.layerIndex = index;
    }
};

/**
 * 打开查看订单管理详情
 */
Order.openOrderDetail = function () {
    if (this.check()) {
        var index = layer.open({
            type: 2,
            title: '订单详情',
            area: ['640px', '480px'], //宽高
            fix: false, //不固定
            maxmin: true,
            content: Feng.ctxPath + '/order/class/detail/' + Order.seItem.acceptNo
        });
        layer.full(index);
        this.layerIndex = index;
    }
};

/**
 * 关闭订单管理
 */
Order.close = function () {
    if (this.check()) {
        var ajax = new $ax(Feng.ctxPath + "/order/class/close", function (data) {
            Feng.success("关闭成功!");
            Order.table.refresh();
        }, function (data) {
            Feng.error("关闭失败!" + data.responseJSON.message + "!");
        });
        ajax.set("orderNo",this.seItem.acceptNo);
        ajax.start();
    }
};

/**
 * 导出订单
 */
Order.export = function () {
        var ajax = new $ax(Feng.ctxPath + "/order/class/export", function (data) {
            //Feng.success("导出成功!");
            window.location.href = encodeURI(data.message);
        }, function (data) {
            Feng.error("导出失败!" + data.responseJSON.message + "!");
        });

        var queryData = {};
        queryData['orderNo'] = $("#condition").val();
        queryData['teacher'] = $("#teacher").val();
        queryData['student'] = $("#student").val();
        queryData['subject'] = $("#subject").val();
        queryData['ability'] = $("#ability").val();
        queryData['cycle'] = $("#cycle").val();
        ajax.setData(queryData);
        ajax.start();
};

/**
 * 查询订单管理列表
 */
Order.search = function () {
    var queryData = {};
    queryData['orderNo'] = $("#condition").val();
    queryData['teacher'] = $("#teacher").val();
    queryData['student'] = $("#student").val();
    queryData['subject'] = $("#subject").val();
    queryData['ability'] = $("#ability").val();
    queryData['cycle'] = $("#cycle").val();
    Order.table.refresh({query: queryData});
    Order.table.setQueryParams(queryData);
};

/**
 * 查询《退费》订单管理列表
 */
Order.cancelSearch = function () {
    var queryData = {};
    queryData['orderNo'] = $("#condition").val();
    queryData['teacher'] = $("#teacher").val();
    queryData['student'] = $("#student").val();
    queryData['subject'] = $("#subject").val();
    queryData['ability'] = $("#ability").val();
    queryData['cycle'] = $("#cycle").val();
    queryData['orderStatus'] = 2;

    Order.table.refresh({query: queryData});
    Order.table.setQueryParams(queryData);
};

$(function () {
    var defaultColunms = Order.initColumn();
    var table = new BSTable(Order.id, "/order/class/list", defaultColunms);
    table.setPaginationType("server");
    Order.table = table.init();
});
