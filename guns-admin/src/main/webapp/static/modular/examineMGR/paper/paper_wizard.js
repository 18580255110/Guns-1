/**
 * 试卷管理
 */
var PaperWizard = {
    Wizard: {
        id: 'wizard'
    },
    UnSelectQuestion: {
        id: "UnSelectQuestionTable",	        //表格id
        seItems: new Array(),		            //选中的条目
        table: null,
        layerIndex: -1
    },
    SelectedQuestion: {
        id: "SelectedQuestionTable",	        //表格id
        seCodes: new Array(),                   // 已加入试卷的题目
        seScores : new Object(),                // 题目分数
        seItems: new Array(),		            //选中的条目
        table: null,
        layerIndex: -1
    },
};

PaperWizard.UnSelectQuestion.initColumn = function () {
    return [
        {field: 'selectItem', checkbox: true},
        {title: '试题编码', field: 'code', visible: false, align: 'center', valign: 'middle'},
        {title: '试题题目', field: 'question', visible: true, align: 'center', valign: 'middle'},
        {title: '试题类型', field: 'typeName', visible: true, align: 'center', valign: 'middle'},
        {title: '出题人', field: 'teacherName', visible: true, align: 'center', valign: 'middle'},
        {title: '分值', field: 'score', visible: false, align: 'center', valign: 'middle'}
    ];
};


/**
 * 打开试卷题目预览界面
 */
Paper.openPaperViewer = function () {
    var index = layer.open({
        type: 2,
        title: '添加题目',
        area: ['320px', '480px'], //宽高
        fix: false, //不固定
        maxmin: true,
        content:
    });
    this.layerIndex = index;
};

$(function () {
    $('#' + PaperWizard.Wizard.id).steps({
        headerTag: "h1",
        bodyTag: "fieldset",
        transitionEffect: "slideLeft",
        autoFocus: true,
        labels: {
            finish: "完成", // 修改按钮得文本
            next: "下一步", // 下一步按钮的文本
            previous: "上一步", // 上一步按钮的文本
            loading: "Loading ..."
        }
    });

    var displayColumns = PaperWizard.UnSelectQuestion.initColumn();
    var table = new BSTable(PaperWizard.UnSelectQuestion.id, "/examine/paper/question/list?excludePaper=" + $('#code').val(), displayColumns);
    table.setPaginationType("server");
    table.setQueryParamsGetter(function(){
        return {'workingCodes': PaperWizard.SelectedQuestion.seCodes.join(',')};
    });

    PaperWizard.UnSelectQuestion.table = table.init();
});
