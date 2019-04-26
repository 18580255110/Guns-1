/**
 * 入学诊断管理初始化
 */
var Question = {
    id: "QuestionTable",	//表格id
    seItem: null,		//选中的条目
    table: null,
    layerIndex: -1
};

/**
 * 初始化表格的列
 */
Question.initColumn = function () {
    return [
        {field: 'selectItem', radio: true},
        {title: '试题题目', field: 'question', visible: true, align: 'center', valign: 'middle'},
        {title: '试题编码', field: 'code', visible: false, align: 'center', valign: 'middle'},
        {title: '试题类型', field: 'typeName', visible: true, align: 'center', valign: 'middle'},
        {title: '试题学科', field: 'subjectName', visible: true, align: 'center', valign: 'middle'},
        {title: '试题年级', field: 'gradeName', visible: true, align: 'center', valign: 'middle'},
        {title: '答案', field: 'expactAnswer', visible: false, align: 'center', valign: 'middle'},
        {title: '出题人', field: 'teacherName', visible: true, align: 'center', valign: 'middle'},
        {title: '状态', field: 'status', visible: true, align: 'center', valign: 'middle',
            formatter: function(value, row){
                if (1 == value)
                    return '<input type="checkbox" class="js-switch" data-code="'+row.code+'" checked />';
                else
                    return '<input type="checkbox" class="js-switch" data-code="'+row.code+'" />';
            }
        },
        {title: '操作', field: 'status', visible: true, align: 'center', valign: 'middle',
            formatter: function(value, row){
                return '<button type="button" class="btn" onclick="Question.openView(\''+row.code+'\')" title="预览"><i class="fa fa-eye"></i></button>';
            }
        },
    ];
};
Question.openView = function(code){
    console.log(code);
    var index = layer.open({
        type: 2,
        title: '试题预览',
        area: ['400px', '600px'], //宽高
        fix: true, //不固定
        maxmin: false,
        content: Feng.ctxPath + '/question/question_view/' + code
    });
    this.layerIndex = index;
}
/**
 * 检查是否选中
 */
Question.check = function () {
    var selected = $('#' + this.id).bootstrapTable('getSelections');
    if(selected.length == 0){
        Feng.info("请先选中表格中的某一记录！");
        return false;
    }else{
        Question.seItem = selected[0];
        return true;
    }
};

/**
 * 点击添加入学诊断
 */
Question.openAddQuestion = function () {
    var index = layer.open({
        type: 2,
        title: '添加题目',
        area: ['640px', '480px'], //宽高
        fix: false, //不固定
        maxmin: true,
        content: Feng.ctxPath + '/question/question_add'
    });
    layer.full(index);
    this.layerIndex = index;
};

/**
 * 打开查看入学诊断详情
 */
Question.openQuestionDetail = function () {
    if (this.check()) {
        var index = layer.open({
            type: 2,
            title: '入学诊断详情',
            area: ['640px', '480px'], //宽高
            fix: false, //不固定
            maxmin: true,
            content: Feng.ctxPath + '/question/question_update/' + Question.seItem.code
        });
        layer.full(index);
        this.layerIndex = index;
    }
};
/**
 * 删除
 */
Question.delQuestionDetail = function () {

    if (this.check()) {
        var ajax = new $ax(Feng.ctxPath + "/question/delete", function (data) {
            Feng.success("删除成功!");
            Question.table.refresh();
        }, function (data) {
            Feng.error("删除失败!" + data.responseJSON.message + "!");
        });
        ajax.set("questionId",  Question.seItem.id);
        ajax.start();
    }
}
/**
 * 查询入学诊断列表
 */
Question.search = function () {
    var queryData = {};
    queryData['condition'] = $("#condition").val();
    queryData['subject'] = $("#subject").val();
    queryData['status'] = $("#status").val();
    queryData['grade'] = $("#grade").val();
    Question.table.refresh({query: queryData});
};

Question.doUpdate = function(reqUrl, data){
    var ajax = new $ax(reqUrl, function (data) {
    }, function (data) {
        Feng.error("操作失败!" + data.responseJSON.message + "!");
    });

    ajax.setData(data);
    ajax.start();
};

Question.initSwitcher = function(selector, options){
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
            Question.doUpdate(reqUrl, postData);
        }
    });
};

$(function () {
    var defaultColunms = Question.initColumn();
    var table = new BSTable(Question.id, "/question/list", defaultColunms);
    table.setPaginationType("server");
    table.setLoadSuccessCallback(function(){
        // 初始化报名开关
        Question.initSwitcher('.js-switch', {
            pause: {
                url: Feng.ctxPath + "/question/pause"
            },
            resume: {
                url: Feng.ctxPath + "/question/resume"
            }
        });
    });
    Question.table = table.init();
});
