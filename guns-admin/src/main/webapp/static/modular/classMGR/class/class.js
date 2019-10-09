/**
 * 课程管理管理初始化
 */
var Class = {
    id: "ClassTable",	//表格id
    seItem: null,		//选中的条目
    table: null,
    queryParams: {status : 1},
    layerIndex: -1
};

/**
 * 初始化表格的列
 */
Class.initColumn = function () {
    return [
        {field: 'selectItem', radio: true},
            {title: 'ID', field: 'id', visible: false, align: 'center', valign: 'middle'},
            {title: '班级编码', field: 'code', visible: true, align: 'center', valign: 'middle'},
            {title: '班级名称', field: 'name', visible: true, align: 'center', valign: 'middle'},
            {title: '年级', field: 'gradeName', visible: false, align: 'center', valign: 'middle'},
            {title: '学期', field: 'cycle', visible: false, align: 'center', valign: 'middle'},
            {title: '班型', field: 'abilityName', visible: false, align: 'center', valign: 'middle'},
            {title: '开课日期', field: 'beginDate', visible: true, align: 'center', valign: 'middle',
                formatter: function(value){
                    return value.substring(0, 10);
                }
            },
            {title: '单节时长(分钟)', field: 'duration', visible: false, align: 'center', valign: 'middle'},
            {title: '总课时数', field: 'period', visible: false, align: 'center', valign: 'middle'},
            {title: '教室编码', field: 'classRoomCode', visible: false, align: 'center', valign: 'middle'},
            {title: '教室', field: 'classRoom', visible: false, align: 'center', valign: 'middle'},
            {title: '教授课程', field: 'courseCode', visible: false, align: 'center', valign: 'middle'},
            {title: '课程名称', field: 'courseName', visible: false, align: 'center', valign: 'middle'},
            {title: '关注度', field: 'star', visible: false, align: 'center', valign: 'middle'},
            {title: '价格(元)', field: 'price', visible: true, align: 'center', valign: 'middle'},
            {title: '学员人数', field: 'quato', visible: true, align: 'center', valign: 'middle'},
            {title: '剩余人数', field: 'remainderQuato', visible: true, align: 'center', valign: 'middle'},
            {title: '报名开始日期', field: 'signStartDate', visible: true, align: 'center', valign: 'middle',
                formatter: function(value){
                    return value.substring(0, 10);
                }
            },
            {title: '报名截止日期', field: 'signEndDate', visible: true, align: 'center', valign: 'middle',
                formatter: function(value){
                    return value.substring(0, 10);
                }
            },
            {title: '状态', field: 'statusName', visible: false, align: 'center', valign: 'middle'},
            {title: '主讲教师编码', field: 'teacherCode', visible: false, align: 'center', valign: 'middle'},
            {title: '主讲教师名称', field: 'teacher', visible: true, align: 'center', valign: 'middle'},
            {title: '辅导教师编码', field: 'teacherSecondCode', visible: false, align: 'center', valign: 'middle'},
            {title: '辅导教师名称', field: 'teacherSecond', visible: true, align: 'center', valign: 'middle'},
            {title: '开放报名', field: 'signable', visible: true, align: 'center', valign: 'middle',
                formatter: function(value, row){
                    if (1 == value)
                        return '<input type="checkbox" class="js-switch-signable" data-code="'+row.code+'" checked />';
                    else
                        return '<input type="checkbox" class="js-switch-signable" data-code="'+row.code+'" />';
                }
            },
            {title: '入学测试', field: 'examinable', visible: true, align: 'center', valign: 'middle',
                formatter: function(value, row){
                    if (1 == value)
                        return '<input type="checkbox" class="js-switch-examinable" data-code="'+row.code+'" checked />';
                    else
                        return '<input type="checkbox" class="js-switch-examinable" data-code="'+row.code+'" />';
                }
            }
    ];
};

/**
 * 检查是否选中
 */
Class.check = function () {
    var selected = $('#' + this.id).bootstrapTable('getSelections');
    if(selected.length == 0){
        Feng.info("请先选中表格中的某一记录！");
        return false;
    }else{
        Class.seItem = selected[0];
        return true;
    }
};

/**
 * 点击添加班级管理
 */
Class.openAddClass = function () {
    var index = layer.open({
        type: 2,
        title: '添加班级',
        area: ['640px', '480px'], //宽高
        fix: false, //不固定
        maxmin: true,
        content: Feng.ctxPath + '/class/class_add'
    });
    layer.full(index);
    this.layerIndex = index;
};

/**
 * 打开查看班级详情
 */
Class.openClassDetail = function () {
    if (this.check()) {
        var index = layer.open({
            type: 2,
            title: '课程管理详情',
            area: ['640px', '480px'], //宽高
            fix: false, //不固定
            maxmin: true,
            content: Feng.ctxPath + '/class/class_update/' + Class.seItem.code
        });
        layer.full(index);
        this.layerIndex = index;
    }
};


/**
 * 打开续报设置
 */
Class.openPresignSetting = function () {
    if (this.check()) {
        var index = layer.open({
            type: 2,
            title: '班级续报设置',
            area: ['640px', '480px'], //宽高
            fix: false, //不固定
            maxmin: true,
            content: Feng.ctxPath + '/class/presign_setting/' + Class.seItem.code
        });
        layer.full(index);
        this.layerIndex = index;
    }
};


/**
 * 打开跨报设置
 */
Class.openCrosssignSetting = function () {
    if (this.check()) {
        var index = layer.open({
            type: 2,
            title: '班级跨报设置',
            area: ['640px', '480px'], //宽高
            fix: false, //不固定
            maxmin: true,
            content: Feng.ctxPath + '/class/crosssign_setting/' + Class.seItem.code
        });
        layer.full(index);
        this.layerIndex = index;
    }
};

/**
 * 删除课程管理
 */
Class.delete = function () {
    if (this.check()) {
        var ajax = new $ax(Feng.ctxPath + "/class/delete", function (data) {
            Feng.success("删除成功!");
            Class.table.refresh();
        }, function (data) {
            Feng.error("删除失败!" + data.responseJSON.message + "!");
        });
        ajax.set("classCode",this.seItem.code);
        ajax.start();
    }
};

/**
 * 查询课程管理列表
 */
Class.search = function () {
    var queryData = {};
    queryData['grades'] = $("#grade").val();
    queryData['subjects'] = $("#subject").val();
    queryData['abilities'] = $("#ability").val();
    queryData['classCycles'] = $("#cycle").val();
    queryData['classPlans'] = $("#studyDate").val();
    queryData['signState'] = $("#signState").val();
    queryData['examinable'] = $("#examState").val();
    queryData['teacherQueryString'] = $("#teacherQueryString").val();

    var minPrice = parseFloat($('#minPrice').val(), 10);
    var maxPrice = parseFloat($('#maxPrice').val(), 10);
    if (!(isNaN(minPrice))) {
        queryData['minPrice'] = Math.floor(100 * minPrice);;
    }
    if (!(isNaN(maxPrice))) {
        queryData['maxPrice'] = Math.floor(100 * maxPrice);;
    }
    queryData['studyDate'] = $("#studyDate").val();
    queryData['signDate'] = $("#signDate").val();
    queryData['offset'] = 0;
    queryData['status'] = 1;
    // queryData['pageNumber'] = 1;
    Class.queryParams = queryData;
    Class.table.refresh({query: queryData});
};

Class.getQueryParams = function(){
    return Class.queryParams;
}

Class.doUpdate = function(reqUrl, data){
    var ajax = new $ax(reqUrl, function (data) {
    }, function (data) {
        Feng.error("操作失败!" + data.responseJSON.message + "!");
    });

    ajax.setData(data);
    ajax.start();
};

Class.initSwitcher = function(selector, options){
    var switchers = Array.prototype.slice.call(document.querySelectorAll(selector));

    switchers.forEach(function(switcher) {
        var switchery = new Switchery(switcher, {
            size: 'small'
        });
        switcher.onchange = function(){
            var state = switcher.checked;

            var reqUrl = '';
            var postData = {
                classCode : $(switcher).attr('data-code')
            };

            if (state){
                // true 启用
                reqUrl = options.resume.url;
            }else{
                // false 停用
                reqUrl = options.pause.url;
            }
            Class.doUpdate(reqUrl, postData);
        }
    });
};

$(function () {

    var defaultColunms = Class.initColumn();
    var table = new BSTable(Class.id, "/class/list?status=1", defaultColunms);
    table.setPaginationType("server");
    table.setLoadSuccessCallback(function(){
        // 初始化报名开关
        Class.initSwitcher('.js-switch-signable', {
            pause: {
                url: Feng.ctxPath + "/class/signable/stop"
            },
            resume: {
                url: Feng.ctxPath + "/class/signable/resume"
            }
        });
        // 初始化测试开关
        Class.initSwitcher('.js-switch-examinable', {
            pause: {
                url: Feng.ctxPath + "/class/examinable/stop"
            },
            resume: {
                url: Feng.ctxPath + "/class/examinable/resume"
            }
        });

    });
    table.setQueryParamsGetter(Class.getQueryParams);
    Class.table = table.init();

    // 日期条件初始化
    laydate.render({elem: '#studyDate'});
    laydate.render({elem: '#signDate'});
});
