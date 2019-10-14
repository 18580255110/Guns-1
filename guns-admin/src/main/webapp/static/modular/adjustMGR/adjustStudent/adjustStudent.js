/**
 * 调课管理管理初始化
 */
var AdjustStudent = {
    id: "AdjustStudentTable",	//表格id
    seItem: null,		//选中的条目
    table: null,
    layerIndex: -1
};

/**
 * 初始化表格的列
 */
AdjustStudent.initColumn = function () {
    return [
        {field: 'selectItem', radio: true},
            {title: '申请用户', field: 'number', visible: true, align: 'center', valign: 'middle', width: 100},
            {title: '申请学生', field: '', visible: true, align: 'center', valign: 'middle',
                formatter:function (value,row,index) {
                    return row.sname + '(' + row.student_code + ')';
                }
            },
            {title: '当前班级', field: 'scname', visible: true, align: 'center', valign: 'middle'},
            {title: '审批状态', field: 'workStatusName', visible: true, align: 'center', valign: 'middle', width: 80},
            {title: '状态', field: 'statusName', visible: false, align: 'center', valign: 'middle' },
            {title: '调整明细', field: 'remark', visible: true, align: 'center', valign: 'middle',
                formatter:function (value,row,index) {
                    return row.dcname + '(' + row.oname + ')';
                }
            },
            {title: '申请时间', field: 'create_time', visible: true, align: 'center', valign: 'middle'},
            {title: '审核时间', field: 'update_time', visible: true, align: 'center', valign: 'middle'}
    ];
};

/**
 * 检查是否选中
 */
AdjustStudent.check = function () {
    var selected = $('#' + this.id).bootstrapTable('getSelections');
    if(selected.length == 0){
        Feng.info("请先选中表格中的某一记录！");
        return false;
    }else{
        AdjustStudent.seItem = selected[0];
        return true;
    }
};

/**
 * 審核通過
 */
AdjustStudent.openApprove = function () {
    if (this.check()) {
        var index = layer.open({
            type: 2,
            title: '审核调课申请',
            area: ['480px', '320px'], //宽高
            fix: false, //不固定
            maxmin: true,
            content: Feng.ctxPath + '/adjust/pass?applyId=' + this.seItem.id
        });
        this.layerIndex = index;
    }
};
/**
 * 審核打回
 */
AdjustStudent.openReject = function () {
    if (this.check()) {
        var index = layer.open({
            type: 2,
            title: '拒绝调课申请',
            area: ['480px', '320px'], //宽高
            fix: false, //不固定
            maxmin: true,
            content: Feng.ctxPath + '/adjust/reject?applyId=' + this.seItem.id
        });
        this.layerIndex = index;
    }
};

/**
 * 关闭调课管理
 */
AdjustStudent.close = function () {
    if (this.check()) {
        var ajax = new $ax(Feng.ctxPath + "/adjust/close", function (data) {
            Feng.success("关闭成功! ");
            AdjustStudent.table.refresh();
        }, function (data) {
            Feng.error("关闭失败! " + data.responseJSON.message + "!");
        });
        ajax.set("applyId",this.seItem.id);
        ajax.start();
    }
};

/**
 * 查询调课管理列表
 */
AdjustStudent.search = function () {
    var queryData = {};
    queryData['student'] = $("#student").val();
    queryData['classInfo'] = $("#classInfo").val();
    queryData['beginDate'] = $("#beginDate").val();
    queryData['endDate'] = $("#endDate").val();
    queryData['status'] = '1';
    AdjustStudent.table.refresh({query: queryData});
    AdjustStudent.table.setQueryParams(queryData);
};

$(function () {
    var defaultColunms = AdjustStudent.initColumn();
    var table = new BSTable(AdjustStudent.id, "/adjust/list", defaultColunms);
    table.setPaginationType("server");
    AdjustStudent.table = table.init();


    // 日期条件初始化
    laydate.render({elem: '#beginDate'});
    laydate.render({elem: '#endDate'});
});
