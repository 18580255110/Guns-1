/**
 * 报名管理
 */
var SignWizard = {
    Wizard: {
        id: 'wizard',
        classCode: $('#classCode').val(),
        postData: {},
        postUrl: {
            'order': Feng.ctxPath + "/order/sign/doSign",
            'update': Feng.ctxPath + "/examine/paper/update"
        }
    },
    ClassPlan: {
        id: "ClassPlanTable",	                //表格id
        seItems: new Array(),		            //选中的条目
        table: null,
        layerIndex: -1
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
        {
            id: 'confirmForm',
            validateFields: {
                feedbackIcons: false,
                payType: {
                    validators: {
                        notEmpty: {
                            message: '支付类型不能为空'
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
SignWizard.ClassPlan.initColumn = function () {
    return [
        {field: 'selectItem', checkbox: true},
        {title: '课程内容', field: 'outline', visible: true, align: 'center', valign: 'middle'},
        {title: '上课日期', field: 'studyDate', visible: true, align: 'center', valign: 'middle',
            formatter: function(val){
                return val.substring(0, 10);
            }
        },
        {title: '上课时间', field: 'classTime', visible: true, align: 'center', valign: 'middle',
            formatter: function(val){
                var time = null
                try{
                    time = parseInt(val, 10);
                }catch(e){}
                if (isNaN(time)){
                    return val;
                }

                if (val.length != 4)
                    return val;

                var hour = parseInt(val.substring(0, 2), 10);
                var minute = parseInt(val.substring(2), 10);

                var message = "";
                if (hour < 12){
                    message = "上午 " + hour + " 点" ;
                }
                if (hour == 12)
                    message = "中午 " + hour + " 点" ;
                if (hour > 12) {
                    hour = hour - 12;
                    message = "下午 " + hour + " 点" ;
                }

                if (0 == minute)
                    message += "整";
                else
                    message += " " + minute;


                return message;
            }
        },
        {title: '下课时间', field: 'endTime', visible: true, align: 'center', valign: 'middle',
            formatter: function(val){
                var time = null
                try{
                    time = parseInt(val, 10);
                }catch(e){}
                if (isNaN(time)){
                    return val;
                }

                if (val.length != 4)
                    return val;

                var hour = parseInt(val.substring(0, 2), 10);
                var minute = parseInt(val.substring(2), 10);

                var message = "";
                if (hour < 12){
                    message = "上午 " + hour + " 点" ;
                }
                if (hour == 12)
                    message = "中午 " + hour + " 点" ;
                if (hour > 12) {
                    hour = hour - 12;
                    message = "下午 " + hour + " 点" ;
                }

                if (0 == minute)
                    message += "整";
                else
                    message += " " + minute;


                return message;
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
SignWizard.set = function(key, val) {
    this.Wizard.postData[key] = (typeof val == "undefined") ? $("#" + key).val() : val;
    return this;
};

/**
 * 清除数据
 */
SignWizard.clearData = function() {
    this.Wizard.postData = {};
};

/**
 * 收集数据
 */
SignWizard.collectData = function() {
    this.Wizard.postData.payType = $('#payType').val();
    this.Wizard.postData.classInfo = {
        code : $('#classCode').val()
    };
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
SignWizard.close = function() {
    parent.layer.close(window.parent.Sign.layerIndex);
};

$(function () {
    console.log('<<< init result');
    var wizard = $('#' + SignWizard.Wizard.id);
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

            if (step > SignWizard.forms.length - 1) {
                console.log(' no validator match');
                return true;
            }

            Feng.initValidator(SignWizard.forms[step].id, SignWizard.forms[step].validateFields, {excludes: [":disabled"]});
            $('#' + SignWizard.forms[step].id).data("bootstrapValidator").resetForm();
            $('#' + SignWizard.forms[step].id).bootstrapValidator('validate');
            return $('#' + SignWizard.forms[step].id).data('bootstrapValidator').isValid();
        },
        onStepChanged: function(event, step, prev){
            console.log('<<< step ' + step + ' change from ' + prev);

            if (step == 1){
                // 进入到"订单确认"步骤
                $('#member_mobileNumber').val($('#mobileNumber').val());
                $('#member_name').val($('#memberName').val());
                $('#student_name').val($('#student').val());
                $('#student_age').val($('#age').val());
                $('#student_gradeName').val($("#grade").find("option:selected").text());
                $('#student_genderName').val($("#gender").find("option:selected").text());
                $('#student_school').val($('#school').val());
                $('#student_targetSchool').val($('#targetSchool').val());
            }

            if (step == 2 && step  >  prev) {
                // 发送验证码
            }
        },
        onFinished: function(){
            SignWizard.clearData();
            SignWizard.collectData();

            //提交信息
            var ajax = new $ax(SignWizard.Wizard.postUrl['order'], function(data){
                Feng.success("保存成功!");
                SignWizard.close();
            },function(data){
                Feng.error("保存失败!" + data.responseJSON.message + "!");
            });
            ajax.setContentType("application/json");
            ajax.setData(JSON.stringify(SignWizard.Wizard.postData));
            ajax.start();
        }
    });

    var displayColumns = SignWizard.ClassPlan.initColumn();
    var table = new BSTable(SignWizard.ClassPlan.id, "/order/sign/plan/list?classCode=" + SignWizard.Wizard.classCode, displayColumns);
    table.setPaginationType("server");
    table.setPaginational(false);

    SignWizard.ClassPlan.table = table.init();

    //初始select选项
    $("#grades").val($("#gradesValue").val());
    $("#ability").val($("#abilityValue").val());
    $("#subject").val($("#subjectValue").val());


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
