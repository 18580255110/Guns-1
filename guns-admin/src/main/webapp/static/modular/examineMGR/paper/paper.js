/**
 * 入学诊断管理初始化
 */
var Paper = {
    id: "PaperTable",	//表格id
    seItem: null,		//选中的条目
    table: null,
    layerIndex: -1
};

/**
 * 初始化表格的列
 */
Paper.initColumn = function () {
    return [
        {field: 'selectItem', radio: true},
        {title: '试卷编码', field: 'code', visible: true, align: 'center', valign: 'middle'},
        {title: '针对学科', field: 'subjectName', visible: true, align: 'center', valign: 'middle'},
        {title: '应用年级', field: 'gradesName', visible: true, align: 'center', valign: 'middle'},
        {title: '题目数量', field: 'count', visible: true, align: 'center', valign: 'middle'},
        {title: '总分值', field: 'totalScore', visible: true, align: 'center', valign: 'middle'},
        {title: '出题人', field: 'teacher', visible: true, align: 'center', valign: 'middle'},
        {title: '出题时间', field: 'createDate', visible: true, align: 'center', valign: 'middle'},
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
Paper.check = function () {
    var selected = $('#' + this.id).bootstrapTable('getSelections');
    if(selected.length == 0){
        Feng.info("请先选中表格中的某一记录！");
        return false;
    }else{
        Paper.seItem = selected[0];
        return true;
    }
};

/**
 * 点击添加入学诊断
 */
Paper.openAddPaper = function () {
    var index = layer.open({
        type: 2,
        title: '新增试卷',
        area: ['640px', '480px'], //宽高
        fix: false, //不固定
        maxmin: true,
        content: Feng.ctxPath + '/examine/paper/wizard?code=0' // 新增 传入一个不可能的code
    });
    layer.full(index);
    this.layerIndex = index;
};

/**
 * 打开查看入学诊断详情
 */
Paper.openPaperDetail = function () {
    if (this.check()) {
        var index = layer.open({
            type: 2,
            title: '编辑试卷',
            area: ['640px', '480px'], //宽高
            fix: false, //不固定
            maxmin: true,
            content: Feng.ctxPath + '/examine/paper/wizard?code=' + Paper.seItem.code
        });
        layer.full(index);
        this.layerIndex = index;
    }
};

/**
 * 打开查看试卷应用
 */
Paper.openPaperUse = function () {
    if (this.check()) {
        var index = layer.open({
            type: 2,
            title: '应用试卷',
            area: ['640px', '480px'], //宽高
            fix: false, //不固定
            maxmin: true,
            content: Feng.ctxPath + '/examine/paper/open_use?code=' + Paper.seItem.code
        });
        layer.full(index);
        this.layerIndex = index;
    }
};
/**
 * 复制试卷
 */
Paper.copy = function () {
    if (this.check()) {
        var ajax = new $ax(Feng.ctxPath + "/examine/paper/copy", function (data) {
            Feng.success("复制成功!");
            Paper.table.refresh();
        }, function (data) {
            Feng.error("复制失败!" + data.responseJSON.message + "!");
        });
        ajax.set("code",this.seItem.code);
        ajax.start();
    }
};
/**
 * 删除试卷
 */
Paper.delete = function () {
    if (this.check()) {
        var ajax = new $ax(Feng.ctxPath + "/examine/paper/delete", function (data) {
            Feng.success("删除成功!");
            Paper.table.refresh();
        }, function (data) {
            Feng.error("删除失败!" + data.responseJSON.message + "!");
        });
        ajax.set("code",this.seItem.code);
        ajax.start();
    }
};

/**
 * 查询入学诊断列表
 */
Paper.search = function () {
    var queryData = {};
    queryData['condition'] = $("#condition").val();
    queryData['subject'] = $("#subject").val();
    queryData['grade'] = $("#grade").val();
    queryData['status'] = $("#status").val();
    Paper.table.refresh({query: queryData});
};


Paper.doUpdate = function(reqUrl, data){
    var ajax = new $ax(reqUrl, function (data) {
    }, function (data) {
        Feng.error("操作失败!" + data.responseJSON.message + "!");
    });

    ajax.setData(data);
    ajax.start();
};

Paper.initSwitcher = function(selector, options){
    var switchers = Array.prototype.slice.call(document.querySelectorAll(selector));

    switchers.forEach(function(switcher) {
        var switchery = new Switchery(switcher, {
            size: 'small'
        });
        switcher.onchange = function(){
            var state = switcher.checked;

            var reqUrl = '';
            var postData = {
                code : $(switcher).attr('data-code')
            };

            if (state){
                // true 启用
                reqUrl = options.resume.url;
            }else{
                // false 停用
                reqUrl = options.pause.url;
            }
            Paper.doUpdate(reqUrl, postData);
        }
    });
};

$(function () {
    var defaultColunms = Paper.initColumn();
    var table = new BSTable(Paper.id, "/examine/paper/list", defaultColunms);
    table.setPaginationType("server");
    table.setLoadSuccessCallback(function(){
        // 初始化报名开关
        Paper.initSwitcher('.js-switch', {
            pause: {
                url: Feng.ctxPath + "/examine/paper/pause"
            },
            resume: {
                url: Feng.ctxPath + "/examine/paper/resume"
            }
        });
    });
    Paper.table = table.init();
});
