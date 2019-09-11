/**
 * 教室管理管理初始化
 */
var Classroom = {
    id: "ClassroomTable",	//表格id
    seItem: null,		//选中的条目
    table: null,
    layerIndex: -1
};

/**
 * 初始化表格的列
 */
Classroom.initColumn = function () {
    return [
        {field: 'selectItem', radio: true},
            {title: '教室编码', field: 'code', visible: true, align: 'center', valign: 'middle'},
            {title: '教室名称', field: 'name', visible: true, align: 'center', valign: 'middle'},
            {title: '类型', field: 'typeName', visible: true, align: 'center', valign: 'middle'},
            {title: '教室地址', field: 'address', visible: true, align: 'center', valign: 'middle'},
            {title: '座位数', field: 'maxCount', visible: true, align: 'center', valign: 'middle'},
            {title: '状态', field: 'status', visible: true, align: 'center', valign: 'middle',
                formatter: function(value, row){
                    if (1 == value)
                        return '<input type="checkbox" class="js-switch" data-code="'+row.code+'" checked />';
                    else
                        return '<input type="checkbox" class="js-switch" data-code="'+row.code+'" />';
                }
            }
    ];
};

/**
 * 检查是否选中
 */
Classroom.check = function () {
    var selected = $('#' + this.id).bootstrapTable('getSelections');
    if(selected.length == 0){
        Feng.info("请先选中表格中的某一记录！");
        return false;
    }else{
        Classroom.seItem = selected[0];
        return true;
    }
};

/**
 * 点击添加教室管理
 */
Classroom.openAddClassroom = function () {
    var index = layer.open({
        type: 2,
        title: '添加教室管理',
        area: ['800px', '420px'], //宽高
        fix: false, //不固定
        maxmin: true,
        content: Feng.ctxPath + '/classroom/classroom_add'
    });
    layer.full(index);
    this.layerIndex = index;
};

/**
 * 打开查看教室管理详情
 */
Classroom.openClassroomDetail = function () {
    if (this.check()) {
        var index = layer.open({
            type: 2,
            title: '教室管理详情',
            area: ['800px', '420px'], //宽高
            fix: false, //不固定
            maxmin: true,
            content: Feng.ctxPath + '/classroom/classroom_update/' + Classroom.seItem.id
        });
        this.layerIndex = index;
    }
};

/**
 * 删除教室管理
 */
Classroom.delete = function () {
    if (this.check()) {
        var ajax = new $ax(Feng.ctxPath + "/classroom/delete", function (data) {
            Feng.success("删除成功!");
            Classroom.table.refresh();
        }, function (data) {
            Feng.error("删除失败!" + data.responseJSON.message + "!");
        });
        ajax.set("classroomId",this.seItem.id);
        ajax.start();
    }
};

/**
 * 查询教室管理列表
 */
Classroom.search = function () {
    var queryData = {};
    queryData['condition'] = $("#condition").val();
    queryData['status'] = $("#status").val();
    Classroom.table.refresh({query: queryData});
};

Classroom.doUpdate = function(reqUrl, data){
    var ajax = new $ax(reqUrl, function (data) {
    }, function (data) {
        Feng.error("操作失败!" + data.responseJSON.message + "!");
        switchery.setPosition(true);
        switchery.handleOnchange(true);
    });

    ajax.setData(data);
    ajax.start();
}

$(function () {
    var defaultColunms = Classroom.initColumn();
    var table = new BSTable(Classroom.id, "/classroom/list?status=1", defaultColunms);
    table.setPaginationType("server");
    table.setLoadSuccessCallback(function(){
        var switchers = Array.prototype.slice.call(document.querySelectorAll('.js-switch'));

        switchers.forEach(function(switcher) {
            var switchery = new Switchery(switcher, {
                size: 'small'
            });
            switcher.onchange = function(){
                var state = switcher.checked;
                console.log('<<< change classroom state' + $(switcher).attr('data-code') + ' : ' + state);

                var reqUrl = '';
                var postData = {
                    code : $(switcher).attr('data-code')
                };

                if (state){
                    // true 启用
                    reqUrl = Feng.ctxPath + "/classroom/resume";
                }else{
                    // false 停用
                    reqUrl = Feng.ctxPath + "/classroom/pause";
                }
                Classroom.doUpdate(reqUrl, postData);
            }
        });
    });
    Classroom.table = table.init();
});
