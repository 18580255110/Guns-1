/**
 * 课程管理管理初始化
 */
var Course = {
    id: "CourseTable",	//表格id
    seItem: null,		//选中的条目
    table: null,
    layerIndex: -1
};

/**
 * 初始化表格的列
 */
Course.initColumn = function () {
    return [
        {field: 'selectItem', radio: true},
            {title: '课程编码', field: 'code', visible: true, align: 'center', valign: 'middle'},
            {title: '课程名称', field: 'name', visible: true, align: 'center', valign: 'middle'},
            {title: '授课方式', field: 'methodName', visible: true, align: 'center', valign: 'middle'},
            {title: '授课学科', field: 'subjectName', visible: true, align: 'center', valign: 'middle'},
            {title: '授课年级', field: 'gradeName', visible: true, align: 'center', valign: 'middle'},
            {title: '授课课时数', field: 'period', visible: true, align: 'center', valign: 'middle'},
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
Course.check = function () {
    var selected = $('#' + this.id).bootstrapTable('getSelections');
    if(selected.length == 0){
        Feng.info("请先选中表格中的某一记录！");
        return false;
    }else{
        Course.seItem = selected[0];
        return true;
    }
};

/**
 * 点击添加课程管理
 */
Course.openAddCourse = function () {
    var index = layer.open({
        type: 2,
        title: '添加课程管理',
        area: ['640px', '480px'], //宽高
        fix: false, //不固定
        maxmin: true,
        content: Feng.ctxPath + '/course/add'
    });
    layer.full(index);
    this.layerIndex = index;
};

/**
 * 打开查看课程管理详情
 */
Course.openCourseDetail = function () {
    if (this.check()) {
        var index = layer.open({
            type: 2,
            title: '课程管理详情',
            area: ['640px', '480px'], //宽高
            fix: false, //不固定
            maxmin: true,
            content: Feng.ctxPath + '/course/update/' + Course.seItem.code
        });
        layer.full(index);
        this.layerIndex = index;
    }
};

/**
 * 删除课程管理
 */
Course.delete = function () {
    if (this.check()) {
        var ajax = new $ax(Feng.ctxPath + "/course/delete", function (data) {
            Feng.success("删除成功!");
            Course.table.refresh();
        }, function (data) {
            Feng.error("删除失败!" + data.responseJSON.message + "!");
        });
        ajax.set("code",this.seItem.code);
        ajax.start();
    }
};


/**
 * 打开查看课程大纲设置
 */
Course.openCourseOutline = function () {
    if (this.check()) {
        var index = layer.open({
            type: 2,
            title: '课程大纲设置',
            area: ['640px', '480px'], //宽高
            fix: false, //不固定
            maxmin: true,
            content: Feng.ctxPath + '/course/outline/setting/' + Course.seItem.code
        });
        layer.full(index);
        this.layerIndex = index;
    }
};

/**
 * 查询课程管理列表
 */
Course.search = function () {
    var queryData = {};
    queryData['condition'] = $("#condition").val();
    queryData['grade'] = $("#grade").val();
    queryData['subject'] = $("#subject").val();
    queryData['status'] = $("#status").val();
    Course.table.refresh({query: queryData});
    Course.table.setQueryParams(queryData);
};

$(function () {
    var defaultColunms = Course.initColumn();
    var table = new BSTable(Course.id, "/course/list", defaultColunms);
    table.setPaginationType("server");
    table.setLoadSuccessCallback(function(){
        var switchers = Array.prototype.slice.call(document.querySelectorAll('.js-switch'));
        switchers.forEach(function(switcher) {
            var switchery = new Switchery(switcher, {
                size: 'small'
            });
            switcher.onchange = function(){
                var state = switcher.checked;
                console.log('<<< change member state' + $(switcher).attr('data-code') + ' : ' + state);

                var reqUrl = '';
                if (state){
                    // true 启用
                    reqUrl = Feng.ctxPath + "/course/resume";
                }else{
                    // false 停用
                    reqUrl = Feng.ctxPath + "/course/pause";
                }

                var ajax = new $ax(reqUrl, function (data) {
                }, function (data) {
                    Feng.error("操作失败!" + data.responseJSON.message + "!");
                });
                ajax.set("code",$(switcher).attr('data-code'));
                ajax.start();

            }
        });
    });
    Course.table = table.init();
});
