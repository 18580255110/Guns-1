/**
 * 班级管理
 */
var SettingWizard = {
    Wizard: {
        id: 'wizard',
        classCode: $('#code').val(),
        postData: {},
        presignClassTable : {
            id : 'presignClassTable',
            table: null
        }
    },
    forms: [
        {
            id: 'infoForm',
            validateFields: []
        },
        {
            id: 'classForm',
            validateFields: {
                sourceClassCode: {
                    validators: {
                        notEmpty: {
                            message: ' ',
                            onError: function(e, data){
                                Feng.error('请选择班级');
                            }
                        }
                    }
                }
            }
        },
        {
            id: 'settingForm',
            validateFields: {
                feedbackIcons: false,
                presignStartDate: {
                    validators: {
                        notEmpty: {
                            message: '续报开始时间'
                        }
                    }
                },
                presignEndDate: {
                    validators: {
                        notEmpty: {
                            message: '续报结束时间'
                        }
                    }
                }
            }
        }
    ]
};


/**
 * 初始化表格的列
 */
SettingWizard.Wizard.presignClassTable.initColumn = function () {
    return [
        {field: 'selectItem', radio: true},
        {title: 'ID', field: 'id', visible: false, align: 'center', valign: 'middle'},
        {title: '班级编码', field: 'code', visible: true, align: 'center', valign: 'middle'},
        {title: '班级名称', field: 'name', visible: true, align: 'center', valign: 'middle'},
        {title: '年级', field: 'gradeName', visible: false, align: 'center', valign: 'middle'},
        {title: '学期', field: 'cycleName', visible: true, align: 'center', valign: 'middle'},
        {title: '学科', field: 'subjectName', visible: true, align: 'center', valign: 'middle'},
        {title: '班型', field: 'abilityName', visible: true, align: 'center', valign: 'middle'},
        {title: '单节时长(分钟)', field: 'duration', visible: false, align: 'center', valign: 'middle'},
        {title: '总课时数', field: 'period', visible: false, align: 'center', valign: 'middle'},
        {title: '教室编码', field: 'classRoomCode', visible: false, align: 'center', valign: 'middle'},
        {title: '教室', field: 'classRoom', visible: true, align: 'center', valign: 'middle'},
        {title: '教授课程', field: 'courseCode', visible: false, align: 'center', valign: 'middle'},
        {title: '课程名称', field: 'courseName', visible: false, align: 'center', valign: 'middle'},
        {title: '关注度', field: 'star', visible: false, align: 'center', valign: 'middle'},
        {title: '价格(元)', field: 'price', visible: true, align: 'center', valign: 'middle'},
        {title: '学员人数', field: 'quato', visible: true, align: 'center', valign: 'middle'},
        {title: '状态', field: 'statusName', visible: false, align: 'center', valign: 'middle'},
        {title: '主讲教师编码', field: 'teacherCode', visible: false, align: 'center', valign: 'middle'},
        {title: '主讲教师名称', field: 'teacher', visible: true, align: 'center', valign: 'middle'}
    ];
};


/**
 * 检查是否选中
 */
SettingWizard.Wizard.presignClassTable.check = function () {
    var selected = $('#' + this.id).bootstrapTable('getSelections');
    if(selected.length == 0){
        Feng.info("请先选中表格中的某一记录！");
        return false;
    }else{
        SettingWizard.Wizard.presignClassTable.seItems = selected.slice(0);
        return true;
    }
};

/**
 * 设置对话框中的数据
 *
 * @param key 数据的名称
 * @param val 数据的具体值
 */
SettingWizard.set = function(key, val) {
    this.Wizard.postData[key] = (typeof val == "undefined") ? $("#" + key).val() : val;
    return this;
};

/**
 * 清除数据
 */
SettingWizard.clearData = function() {
    this.Wizard.postData = {};
};

/**
 * 收集数据
 */
SettingWizard.collectData = function() {

    this
        .set('id')
        .set('code')
    ;

    var sourceClassCode = $('#_sourceClassCode').val();
    console.log('sourceClassCode = ', sourceClassCode);
    if (sourceClassCode.length > 0){
        this.set('presignStartDate')
            .set('presignEndDate')
        ;
    }

    this.Wizard.postData['presignSourceClassCode'] = sourceClassCode;
};

/**
 * 关闭此对话框
 */
SettingWizard.close = function() {
    parent.layer.close(window.parent.Class.layerIndex);
};

/**
 * 查询课程管理列表
 */
SettingWizard.classSearch = function () {
    //$('#sourceClassCode').val(''); // 需要重新选择

    var queryData = {};
    queryData['grades'] = $("#grade").val();
    queryData['subjects'] = $("#subject").val();
    queryData['abilities'] = $("#ability").val();
    queryData['classCycles'] = $("#cycle").val();
    queryData['teacherQueryString'] = $("#teacherQueryString").val();

    queryData['offset'] = 0;
    queryData['status'] = 1;
    // queryData['pageNumber'] = 1;
    //SettingWizard.Wizard.queryParams = queryData;
    SettingWizard.Wizard.presignClassTable.table.refresh({query: queryData});
};

$(function () {
    var now = new Date();
    var year = now.getFullYear();
    var month = 1 + now.getMonth();
    if (month < 10)
        month = '0' + month;
    var day = now.getDate();
    if (day < 10)
        day = '0' + day;

    var today = year + '-' + month + '-' + day;

    // 注册验证方法
    $.fn.bootstrapValidator.validators.presign_v = {
        validate: function() {
            var presignClassCode = $('#presignClassCode').val();

            var presignBeginDate = $('#presignStartDate').val();
            var presignEndDate = $('#presignEndDate').val();
            return presignClassCode.length > 0 ? (presignBeginDate.length > 0 && presignEndDate.length > 0) : true;
        }
    };

    // 初始化


    console.log('<<< init result');
    var wizard = $('#' + SettingWizard.Wizard.id);
    wizard.steps({
        headerTag: "h1",
        bodyTag: "fieldset",
        transitionEffect: "slideLeft",
        autoFocus: true,
        labels: {
            finish: "完成", // 修改按钮得文本
            next: "下一步", // 下一步按钮的文本
            previous: "上一步", // 上一步按钮的文本
            loading: "Loading ..."
        },
        onStepChanging: function(event, step, next){
            console.log('<<< step ' + step + ' change to ' + next);

            if (next < step) {
                console.log(' return not need validate');
                return true;
            }

            if (step > SettingWizard.forms.length - 1) {
                console.log(' no validator match');
                return true;
            }

            console.log('Before validate, sourceClassCode = ', $('#_sourceClassCode').val());
            Feng.initValidator(SettingWizard.forms[step].id, SettingWizard.forms[step].validateFields, {excludes: [":disabled"]});
            $('#' + SettingWizard.forms[step].id).data("bootstrapValidator").resetForm();
            $('#' + SettingWizard.forms[step].id).bootstrapValidator('validate');
            return $('#' + SettingWizard.forms[step].id).data('bootstrapValidator').isValid();
        },
        onStepChanged: function(event, step, prev){
            console.log('<<< step ' + step + ' change from ' + prev);

            if (1 == step){
                laydate.render({elem: '#presignStartDate', min: today});
                laydate.render({elem: '#presignEndDate', min: today});

                var defaultColunms = SettingWizard.Wizard.presignClassTable.initColumn();
                var table = new BSTable(SettingWizard.Wizard.presignClassTable.id, "/class/list", defaultColunms);
                table.setPaginationType("server");
                table.setItemSelectCallback(function(row) {
                    console.log('selected row = ', row);
                    $('#_sourceClassCode').val(row.code);

                    console.log($('#_sourceClassCode').val());

                    $('#confirm-source-className').val(row.name);
                    $('#confirm-source-gradeName').val(row.gradeName);
                    $('#confirm-source-period').val(row.period);
                    $('#confirm-source-subjectName').val(row.subjectName);
                    $('#confirm-source-abilityName').val(row.abilityName);
                    $('#confirm-source-teacher').val(row.teacher);
                });
                SettingWizard.Wizard.presignClassTable.table = table.init();
            }

            if (3 == step) {
                $('#confirm-presign-startDate').val($('#presignStartDate').val());
                $('#confirm-presign-endDate').val($('#presignEndDate').val());
            }

        },
        onFinished: function(){
            console.log('do finish');

            SettingWizard.clearData();
            SettingWizard.collectData();

            console.log('<=== post ');
            console.log(SettingWizard.Wizard.postData);

            //提交信息
            var ajax = new $ax(Feng.ctxPath + "/class/presign_setting/save", function(data){
                Feng.success("设置成功!");
                SettingWizard.close();
            },function(data){
                Feng.error("保存失败!" + data.responseJSON.message + "!");
            });
            ajax.set(SettingWizard.Wizard.postData);

            ajax.start();
        }
    });
});
