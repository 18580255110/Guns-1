/**
 * 初始化成绩单管理详情对话框
 */
var ExamineAnswerInfoDlg = {
    examineAnswerInfoData : {},
    validateFields: {
        paperCode: {
            validators: {
                notEmpty: {
                    message: '试卷不能为空'
                }
            }
        },
        classCode: {
            validators: {
                notEmpty: {
                    message: '班级不能为空'
                }
            }
        },
        studentCode: {
            validators: {
                notEmpty: {
                    message: '学生编码不能为空'
                }
            }
        },
        quota: {
            validators: {
                notEmpty: {
                    message: '题目数量不能为空'
                },
                regexp: {
                    regexp: '^[1-9][0-9]*$',
                    message: '题目数量只能为数字'
                }
            }
        },
        totalScore: {
            validators: {
                notEmpty: {
                    message: '总分不能为空'
                },
                regexp: {
                    regexp: '^[1-9][0-9]*$',
                    message: '总分只能为数字'
                }
            }
        },
        examTime: {
            validators: {
                notEmpty: {
                    message: '测试时间不能为空'
                },
                regexp: {
                    regexp: '^[1-9][0-9]*$',
                    message: '测试时间为数字'
                }
            }
        },
        answerQuota: {
            validators: {
                notEmpty: {
                    message: '答题数不能为空'
                },
                regexp: {
                    regexp: '^[1-9][0-9]*$',
                    message: '答题数只能为数字'
                }
            }
        },
        score: {
            validators: {
                notEmpty: {
                    message: '得分不能为空'
                },
                regexp: {
                    regexp: '^[1-9][0-9]*$',
                    message: '得分只能为数字'
                }
            }
        },
        beginDate: {
            validators: {
                notEmpty: {
                    message: '开始时间不能为空'
                }
            }
        },
        teacher: {
            validators: {
                notEmpty: {
                    message: '批改人不能为空'
                }
            }
        },
    }
};

/**
 * 清除数据
 */
ExamineAnswerInfoDlg.clearData = function() {
    this.examineAnswerInfoData = {};
}

/**
 * 设置对话框中的数据
 *
 * @param key 数据的名称
 * @param val 数据的具体值
 */
ExamineAnswerInfoDlg.set = function(key, val) {
    this.examineAnswerInfoData[key] = (typeof val == "undefined") ? $("#" + key).val() : val;
    return this;
}

/**
 * 设置对话框中的数据
 *
 * @param key 数据的名称
 * @param val 数据的具体值
 */
ExamineAnswerInfoDlg.get = function(key) {
    return $("#" + key).val();
}

/**
 * 关闭此对话框
 */
ExamineAnswerInfoDlg.close = function() {
    parent.layer.close(window.parent.Answer.layerIndex);
}

/**
 * 收集数据
 */
ExamineAnswerInfoDlg.collectData = function() {
    this
    .set('id')
    .set('code')
    .set('paperCode')
    .set('classCode')
    .set('userName')
    .set('studentCode')
    .set('quota')
    .set('totalScore')
    .set('examTime')
    .set('answerQuota')
    .set('lastAnswerQuestion')
    .set('lastAnswerDate')
    .set('status')
    .set('createDate')
    .set('score')
    .set('beginDate')
    .set('endDate')
    .set('duration')
    .set('teacher');
}

/**
 * 验证数据是否为空
 */
ExamineAnswerInfoDlg.validate = function () {
    $('#anwserInfoForm').data("bootstrapValidator").resetForm();
    $('#anwserInfoForm').bootstrapValidator('validate');
    return $("#anwserInfoForm").data('bootstrapValidator').isValid();
};

/**
 * 提交添加
 */
ExamineAnswerInfoDlg.addSubmit = function() {

    this.clearData();
    this.collectData()
    // 默认值：线下已经测试
    ExamineAnswerInfoDlg.set('code',"DJ00000000");
    ExamineAnswerInfoDlg.set('status',"4");
    if (!this.validate()) {
        return;
    }

    //提交信息
    var ajax = new $ax(Feng.ctxPath + "/examine/answer/add", function(data){
        Feng.success("添加成功!");
        window.parent.Answer.table.refresh();
        ExamineAnswerInfoDlg.close();
    },function(data){
        Feng.error("添加失败!" + data.responseJSON.message + "!");
    });
    ajax.set(this.examineAnswerInfoData);
    ajax.start();
}

/**
 * 提交修改
 */
ExamineAnswerInfoDlg.editSubmit = function() {

    this.clearData();
    this.collectData();
    if (!this.validate()) {
        return;
    }

    //提交信息
    var ajax = new $ax(Feng.ctxPath + "/examine/answer/update", function(data){
        Feng.success("修改成功!");
        window.parent.Answer.table.refresh();
        ExamineAnswerInfoDlg.close();
    },function(data){
        Feng.error("修改失败!" + data.responseJSON.message + "!");
    });
    ajax.set(this.examineAnswerInfoData);
    ajax.start();
}

$(function() {
    //非空校验
    Feng.initValidator("anwserInfoForm", ExamineAnswerInfoDlg.validateFields);
    
// 日期条件初始化
    laydate.render({elem: '#beginDate'});
    laydate.render({elem: '#endDate'});
});
