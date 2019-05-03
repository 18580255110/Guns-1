/**
 * 试卷管理初始化
 */
var PaperDlg = {
    currPaper: $('#code').val(),
    paperInfoData: {},
    ztreeInstance: null,
    editable: false,
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
    validateFields: {
        grades: {
            validators: {
                notEmpty: {
                    message: '适应年级不能为空'
                }
            }
        },
        subject: {
            validators: {
                notEmpty: {
                    message: '学科不能为空'
                }
            }
        },
        examTime: {
            validators: {
                notEmpty: {
                    message: '测试时间不能为空'
                }
            }
        },
        ability: {
            validators: {
                notEmpty: {
                    message: '适应班次不能为空'
                }
            }
        }
    }
};

/**
 * 初始化表格的列
 */
PaperDlg.SelectedQuestion.initColumn = function () {
    return [
        {field: 'selectItem', checkbox: true},
        {title: '试题编码', field: 'code', visible: false, align: 'center', valign: 'middle'},
        {title: '试题题目', field: 'question', visible: true, align: 'center', valign: 'middle'},
        {title: '试题类型', field: 'typeName', visible: true, align: 'center', valign: 'middle'},
        {title: '出题人', field: 'teacherName', visible: true, align: 'center', valign: 'middle'},
        {title: '分值', field: 'score', visible: true, align: 'center', valign: 'middle',
            formatter: function(val, row){
                console.log(PaperDlg.SelectedQuestion.seScores);
                console.log(row);
                var score = parseInt(PaperDlg.SelectedQuestion.seScores[row.code], 10);
                if (isNaN(score))
                    score = 0;

                return score;
            }
        }
    ];
};
PaperDlg.UnSelectQuestion.initColumn = function () {
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
 * 检查是否选中
 */
PaperDlg.UnSelectQuestion.check = function () {
    var selected = $('#' + this.id).bootstrapTable('getSelections');
    if(selected.length == 0){
        Feng.info("请先选中表格中的某一记录！");
        return false;
    }else{
        PaperDlg.UnSelectQuestion.seItems = selected.slice(0);
        return true;
    }
};

PaperDlg.join = function(){
    if (this.UnSelectQuestion.check()) {
        $.each(this.UnSelectQuestion.seItems, function(idx, eo){
            //eo.editable = {
            //    type: 'text', title: '分值'
            //}
            $('#' + PaperDlg.SelectedQuestion.id).bootstrapTable('append', eo);
            $('#' + PaperDlg.UnSelectQuestion.id).bootstrapTable('remove', {
                field: 'code',
                values: [eo.code]
            });
            PaperDlg.SelectedQuestion.seCodes.push(eo.code);
        });
    }
};

PaperDlg.remove = function(){
    if (this.SelectedQuestion.check()) {
        console.log('begin remove');
        $.each(this.SelectedQuestion.seItems, function(idx, eo){
            console.log(1);
            $('#' + PaperDlg.UnSelectQuestion.id).bootstrapTable('append', eo);
            $('#' + PaperDlg.SelectedQuestion.id).bootstrapTable('remove', {
                field: 'code',
                values: [eo.code]
            });
            $.each(PaperDlg.SelectedQuestion.seCodes, function(idx, code){
                if (code == eo.code){
                    // 删除
                    PaperDlg.SelectedQuestion.seCodes.splice(idx, 1);
                }
            });
            delete PaperDlg.SelectedQuestion.seScores[row.code];
        });
    }

    console.log('remove over');
}

/**
 * 设置对话框中的数据
 *
 * @param key 数据的名称
 * @param val 数据的具体值
 */
PaperDlg.set = function(key, val) {
    this.paperInfoData[key] = (typeof val == "undefined") ? $("#" + key).val() : val;
    return this;
}

/**
 * 设置对话框中的数据
 *
 * @param key 数据的名称
 * @param val 数据的具体值
 */
PaperDlg.get = function(key) {
    return $("#" + key).val();
}

/**
 * 关闭此对话框
 */
PaperDlg.close = function() {
    parent.layer.close(window.parent.Paper.layerIndex);
}

/**
 * 清除数据
 */
PaperDlg.clearData = function() {
    this.paperInfoData = {};
}

PaperDlg.saveEditColumn = function(index, field, value) {
    $('#' + PaperDlg.SelectedQuestion.id).bootstrapTable('updateCell', {
        index: index,       //行索引
        field: field,       //列名
        value: value        //cell值
    });
}


/**
 * 收集数据
 */
PaperDlg.collectData = function() {
    this
        .set('code')
        .set('id')
        .set('grades')
        .set('subject')
        .set('ability')
        .set('examTime');

    var questions = PaperDlg.SelectedQuestion.seCodes.slice(0);
    console.log(questions);
    var paperItems = new Array();
    $.each(questions, function(idx, code){
        console.log('code = ' + code);
        var score = parseInt(PaperDlg.SelectedQuestion.seScores[code], 10);

        console.log('score = ' + score);
        if (isNaN(score))
            return true;

        paperItems.push(code + '=' + score);
    });
    console.log('paperItems = ' + paperItems);
    this.SelectedQuestion.seItems = paperItems;
}


/**
 * 验证数据是否为空
 */
PaperDlg.validate = function () {

    if (PaperDlg.SelectedQuestion.seItems.length != PaperDlg.SelectedQuestion.seCodes.length) {
        Feng.error("提交失败: 每道题目需设置分数");
        return false
    }

    $('#paperInfoForm').data("bootstrapValidator").resetForm();
    $('#paperInfoForm').bootstrapValidator('validate');
    return $("#paperInfoForm").data('bootstrapValidator').isValid();
};
/**
 * 提交新增
 */
PaperDlg.addSubmit = function() {

    this.clearData();
    this.collectData();

    if (!this.validate()) {
        console.log('validate failed');
        return;
    }

    console.log(this.SelectedQuestion.seItems);

    //提交信息
    var ajax = new $ax(Feng.ctxPath + "/examine/paper/add", function(data){
        Feng.success("修改成功!");
        window.parent.Paper.table.refresh();
        PaperDlg.close();
    },function(data){
        Feng.error("修改失败!" + data.responseJSON.message + "!");
    });
    ajax.set(this.paperInfoData);

    console.log(this.SelectedQuestion.seItems);
    ajax.set('paperItems', this.SelectedQuestion.seItems.join(';'))
    ajax.start();
}
/**
 * 提交修改
 */
PaperDlg.editSubmit = function() {

    this.clearData();
    this.collectData();

    if (!this.validate()) {
        console.log('validate failed');
        return;
    }

    console.log(this.SelectedQuestion.seItems);

    //提交信息
    var ajax = new $ax(Feng.ctxPath + "/examine/paper/update", function(data){
        Feng.success("修改成功!");
        window.parent.Paper.table.refresh();
        PaperDlg.close();
    },function(data){
        Feng.error("修改失败!" + data.responseJSON.message + "!");
    });
    ajax.set(this.paperInfoData);

    console.log(this.SelectedQuestion.seItems);
    ajax.set('paperItems', this.SelectedQuestion.seItems.join(';'))
    ajax.start();
}

$(function () {

    //非空校验
    Feng.initValidator("paperInfoForm", PaperDlg.validateFields);

    // 初始化
    try {
        PaperDlg.SelectedQuestion.seCodes = JSON.parse($('#questionCodes').val());
        PaperDlg.SelectedQuestion.seScores = JSON.parse($('#questionScores').val());
    }catch(error){}

    // 其他
    var displayColumns = PaperDlg.UnSelectQuestion.initColumn();
    var table = new BSTable(PaperDlg.UnSelectQuestion.id, "/examine/paper/question/list?excludePaper=" + $('#code').val(), displayColumns);
    table.setPaginationType("server");
    table.setQueryParamsGetter(function(){
        return {'workingCodes': PaperDlg.SelectedQuestion.seCodes.join(',')};
    });

    PaperDlg.UnSelectQuestion.table = table.init();

    displayColumns = PaperDlg.SelectedQuestion.initColumn();
    var table = new BSTable(PaperDlg.SelectedQuestion.id, "/examine/paper/question/list?includePaper=" + $('#code').val(), displayColumns);
    table.setPaginationType("server");
    table.setShowColumns(false);
    table.setShowRefresh(false);
    table.setPaginational(false);
    table.setClickCell(function(field, value, row, $element) {
        //$element.attr('contenteditable', true);
        if (PaperDlg.editable)
            return;

        PaperDlg.editable = true
        var currValue = $element.html();
        $element.html('');
        var input = $('<input type="input" size="3" maxlength="3" value="'+currValue+'" />');
        $element.append(input);
        input.focus();

        $(document).bind('keydown', function(event){
            if (event.keyCode==13) {  //回车键的键值为13
                var tdValue = input.val();
                input.remove();
                var index = $element.parent().data('index');
                PaperDlg.SelectedQuestion.seScores[row.code] = tdvalue;

                PaperDlg.saveEditColumn(index, field, tdValue);
                $(document).unbind('keydown');
                PaperDlg.editable = false;
            }
        });

        input.blur(function(){
            var tdValue = input.val();
            input.remove();
            var index = $element.parent().data('index');
            PaperDlg.SelectedQuestion.seScores[row.code] = tdValue;

            PaperDlg.saveEditColumn(index, field, tdValue);
            $(document).unbind('keydown');
            PaperDlg.editable = false;
        });
    });
    PaperDlg.SelectedQuestion.table = table.init();

    //初始select选项
    $("#grades").val($("#gradesValue").val());
    $("#ability").val($("#abilityValue").val());
    $("#subject").val($("#subjectValue").val());
});
