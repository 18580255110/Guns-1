/**
 * 报名管理
 */
var AuthWizard = {
    Wizard: {
        id: 'wizard',
        classCode: $('#classCode').val(),
        postData: {},
        postUrl: {
            'authenticate': Feng.ctxPath + "/order/authority/doAuth",
        }
    },
    forms: [
        {
            id: 'basicForm',
            validateFields: {
                studentCode: {
                    feedbackIcons: false,
                    validators: {
                        notEmpty: {
                            message: '学员编码不能为空'
                        }
                    }
                },
            }
        },
        {
            id: 'confirmForm',
            validateFields: {
                student: {
                    feedbackIcons: false,
                    validators: {
                        notEmpty: {
                            message: '学员不能为空'
                        }
                    }
                }
            }
        }
    ]
};

/**
 * 设置对话框中的数据
 *
 * @param key 数据的名称
 * @param val 数据的具体值
 */
AuthWizard.set = function(key, val) {
    this.Wizard.postData[key] = (typeof val == "undefined") ? $("#" + key).val() : val;
    return this;
};

/**
 * 清除数据
 */
AuthWizard.clearData = function() {
    this.Wizard.postData = {};
};

/**
 * 收集数据
 */
AuthWizard.collectData = function() {
    this.Wizard.postData.classInfo = {
        code : $('#classCode').val()
    };
    this.Wizard.postData.student = {
        code : $('#studentCode').val(),
        name : $('#student').val()
    };

};

/**
 * 关闭此对话框
 */
AuthWizard.close = function() {
    parent.layer.close(window.parent.Authority.layerIndex);
};

$(function () {
    console.log('<<< init result');
    var wizard = $('#' + AuthWizard.Wizard.id);
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

            if (step > AuthWizard.forms.length - 1) {
                console.log(' no validator match');
                return true;
            }

            Feng.initValidator(AuthWizard.forms[step].id, AuthWizard.forms[step].validateFields, {excludes: [":disabled"]});
            $('#' + AuthWizard.forms[step].id).data("bootstrapValidator").resetForm();
            $('#' + AuthWizard.forms[step].id).bootstrapValidator('validate');
            return $('#' + AuthWizard.forms[step].id).data('bootstrapValidator').isValid();
        },
        onStepChanged: function(event, step, prev){
            console.log('<<< step ' + step + ' change from ' + prev);
        },
        onFinished: function(){
            AuthWizard.clearData();
            AuthWizard.collectData();

            //提交信息
            var ajax = new $ax(AuthWizard.Wizard.postUrl['authenticate'], function(data){
                Feng.success("保存成功!");
                AuthWizard.close();
            },function(data){
                Feng.error("保存失败!" + data.responseJSON.message + "!");
            });
            ajax.setContentType("application/json");
            ajax.setData(JSON.stringify(AuthWizard.Wizard.postData));
            ajax.start();
        }
    });

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
                    if (! (data.responseJSON) )
                        Feng.error("操作失败! 无效的学员编码");
                    else
                        Feng.error("操作失败!" + data.responseJSON.message + "!");
                });
                memberReq.start();
            }, function (data) {
                if (! (data.responseJSON) )
                    Feng.error("操作失败! 无效的学员编码!");
                else
                    Feng.error("操作失败!" + data.responseJSON.message + "!");
            });

            ajax.start();
        }
    });
});
