/**
 * 报名管理
 */
var classAuthorityWizard = {
    Wizard: {
        id: 'wizard',
        classCode: $('#classCode').val(),
        postData: {},
        postUrl: {
            'classAuthorityUrl': Feng.ctxPath + "/classAuthority/add",
        }
    },
    ClassPlan: {
        id: "classAuthorityTable",	                //表格id
        seItem: null,		            //选中的条目
        table: null,
        layerIndex: -1,
        queryParams: {
            status : 1,
            signState : 1,
            examState : 1,
        },
    },
    forms: [
        {
            id: 'basicForm',
            validateFields: {
                student: {
                    feedbackIcons: false,
                    validators: {
                        notEmpty: {
                            message: '学员姓名不能为空'
                        }
                    }
                },
                age: {
                    feedbackIcons: false,
                    validators: {
                        notEmpty: { message: '年龄不能为空'},
                        numeric: { message: '无效的年龄数据'},
                        between: { min: 0, max: 100, message: '无效的年龄数据'}
                    }
                },
                grade: {
                    feedbackIcons: false,
                    validators: {
                        notEmpty: {
                            message: '学员年级不能为空'
                        }
                    }
                },
                gender: {
                    feedbackIcons: false,
                    validators: {
                        notEmpty: {
                            message: '学员性别不能为空'
                        }
                    }
                },
                mobileNumber: {
                    feedbackIcons: false,
                    validators: {
                        notEmpty: {
                            message: '家长电话不能为空'
                        },
                        regexp: {
                            regexp: '^1[0-9]{10}$',
                            message: '电话号码无效'
                        }
                    }
                },
                memberName: {
                    feedbackIcons: false,
                    validators: {
                        notEmpty: {
                            message: '家长名字不能为空'
                        }
                    }
                },
                targetSchool: {
                    feedbackIcons: false,
                    validators: {
                        notEmpty: {
                            message: '目标学校不能为空'
                        }
                    }
                }
            }
        },
    ]
};


/**
 * 初始化表格的列
 */
classAuthorityWizard.ClassPlan.initColumn = function () {
    return [
        {field: 'selectItem', radio: false },
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
        {title: '教室', field: 'classRoom', visible: true, align: 'center', valign: 'middle'},
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
        {title: '开放报名', field: 'signable', visible: false, align: 'center', valign: 'middle',
            formatter: function(value, row){
                if (1 == value)
                    return '<input type="checkbox" class="js-switch-signable" data-code="'+row.code+'" checked />';
                else
                    return '<input type="checkbox" class="js-switch-signable" data-code="'+row.code+'" />';
            }
        },
        {title: '入学测试', field: 'examinable', visible: false, align: 'center', valign: 'middle',
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
 * 设置对话框中的数据
 *
 * @param key 数据的名称
 * @param val 数据的具体值
 */
classAuthorityWizard.set = function(key, val) {
    this.Wizard.postData[key] = (typeof val == "undefined") ? $("#" + key).val() : val;
    return this;
};

/**
 * 清除数据
 */
classAuthorityWizard.clearData = function() {
    this.Wizard.postData = {};
};

/**
 * 检查是否选中
 */
classAuthorityWizard.check = function () {
    var selected = $('#' + classAuthorityWizard.ClassPlan.id).bootstrapTable('getSelections');
    if(selected.length == 0){
        Feng.info("请先选中表格中的某一记录！");
        return false;
    }else{
        classAuthorityWizard.ClassPlan.seItem = selected;
        return true;
    }
};

/**
 * 收集数据
 */
classAuthorityWizard.collectData = function() {
    this.Wizard.postData.classInfoList =  classAuthorityWizard.ClassPlan.seItem;
    this.Wizard.postData.student = {
        code : $('#studentCode').val(),
        name : $('#student').val(),
        gender : $('#gender').val(),
        age : $('#age').val(),
        school : $('#school').val(),
        grade : $('#grade').val(),
        targetSchool : $('#targetSchool').val()
    };
    this.Wizard.postData.member = {
        mobileNumber : $('#mobileNumber').val(),
        name : $('#memberName').val()
    };

};

/**
 * 关闭此对话框
 */
classAuthorityWizard.close = function() {
    parent.layer.close(window.parent.Sign.layerIndex);
};

classAuthorityWizard.ClassPlan.getQueryParams = function(){
    return classAuthorityWizard.ClassPlan.queryParams;
}


/**
 * 查询课程管理列表
 */
classAuthorityWizard.ClassPlan.search = function () {
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
    // queryData['pageNumber'] = 1;
    classAuthorityWizard.ClassPlan.queryParams = queryData;
    classAuthorityWizard.ClassPlan.table.refresh({query: queryData});
};

/** 保存 */
classAuthorityWizard.ClassPlan.save = function () {
    classAuthorityWizard.clearData();
    classAuthorityWizard.check();
    classAuthorityWizard.collectData();

    //提交信息
    var ajax = new $ax(classAuthorityWizard.Wizard.postUrl['classAuthorityUrl'], function(data){
        Feng.success("保存成功!");
        classAuthorityWizard.close();
    },function(data){
        Feng.error("保存失败!" + data.responseJSON.message + "!");
    });
    ajax.setContentType("application/json");
    ajax.setData(JSON.stringify(classAuthorityWizard.Wizard.postData));
    ajax.start();
    Feng.success("保存成功!");
    classAuthorityWizard.close();

};

function initClassTable(){
    var defaultColunms = classAuthorityWizard.ClassPlan.initColumn();
    var table = new BSTable(classAuthorityWizard.ClassPlan.id, "/class/list", defaultColunms);
    table.setPaginationType("server");
    table.setQueryParams(classAuthorityWizard.ClassPlan.getQueryParams());
    classAuthorityWizard.ClassPlan.table = table.init();
    console.log("初始化",table.init());
    // 日期条件初始化
    laydate.render({elem: '#studyDate'});
    laydate.render({elem: '#signDate'});

    //初始select选项
    $("#grades").val($("#gradesValue").val());
    $("#ability").val($("#abilityValue").val());
    $("#subject").val($("#subjectValue").val());
};
$(function () {
    console.log('<<< init result');
    var wizard = $('#' + classAuthorityWizard.Wizard.id);
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

            if (step > classAuthorityWizard.forms.length - 1) {
                console.log(' no validator match');
                return true;
            }

            Feng.initValidator(classAuthorityWizard.forms[step].id, classAuthorityWizard.forms[step].validateFields, {excludes: [":disabled"]});
            $('#' + classAuthorityWizard.forms[step].id).data("bootstrapValidator").resetForm();
            $('#' + classAuthorityWizard.forms[step].id).bootstrapValidator('validate');
            return $('#' + classAuthorityWizard.forms[step].id).data('bootstrapValidator').isValid();
        },
        onStepChanged: function(event, step, prev){
            console.log('<<< step ' + step + ' change from ' + prev);

            if (step == 1){
            }

            if (step == 2 && step  >  prev) {

            }
        },
        onFinished: function(){
            classAuthorityWizard.ClassPlan.save();
        }
    });

    initClassTable();
    $('#studentCode').bind('change', function(){
        var val = $(this).val();
        console.log(val);
        if (val.length == 0){
            $('#student').val('');
            $('#student').removeAttr('readOnly');
            $('#age').val(1);
            $('#age').removeAttr('readOnly');
            $('#school').val('');
            $('#school').removeAttr('readOnly');
            $('#targetSchool').val('');
            $('#targetSchool').removeAttr('readOnly', true);
            $('#mobileNumber').val('');
            $('#mobileNumber').removeAttr('readOnly');
            $('#memberName').val('');
            $('#memberName').removeAttr('readOnly', true);
        }else{
            var ajax = new $ax(Feng.ctxPath + '/student/get/' + val, function (data) {
                $('#student').val(data.name);
                $('#student').attr('readOnly', true);
                $('#age').val(data.age);
                $('#age').attr('readOnly', true);
                $('#school').val(data.school);
                $('#school').attr('readOnly', true);
                $('#targetSchool').val(data.targetSchool);
                $('#targetSchool').attr('readOnly', true);

                var memberReq = new $ax(Feng.ctxPath + '/member/get/' + data.userName, function (data) {
                    $('#mobileNumber').val(data.mobileNumber);
                    $('#mobileNumber').attr('readOnly', true);
                    $('#memberName').val(data.name);
                    $('#memberName').attr('readOnly', true);
                }, function (data) {
                    Feng.error("操作失败!" + data.responseJSON.message + "!");
                });
                memberReq.start();
            }, function (data) {
                Feng.error("操作失败!" + data.responseJSON.message + "!");
            });

            ajax.start();
        }
    });
});
