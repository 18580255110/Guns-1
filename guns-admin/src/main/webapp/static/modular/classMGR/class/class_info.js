/**
 * 初始化课程管理详情对话框
 */
var ClassInfoDlg = {
    classInfoData : {},
    validateFields: {
        code: {
            validators: {
                notEmpty: {
                    message: '编码不能为空'
                }
            }
        },
        name: {
            validators: {
                notEmpty: {
                    message: '名称不能为空'
                }
            }
        },
        beginDate: {
            validators: {
                notEmpty: {
                    message: '开课起始日期不能为空'
                }
            }
        },
        endDate: {
            validators: {
                notEmpty: {
                    message: '开课结束日期不能为空'
                }
            }
        },
        studyTimeValue: {
            validators: {
                notEmpty: {
                    message: '开课时间不能为空'
                },
                stringLength:{
                    max:4,
                    message: '长度限制'
                }
            }
        },
        beginTime: {
            validators: {
                notEmpty: {
                    message: '开始时间不能为空'
                },
                stringLength:{
                    max:4,
                    message: '长度限制'
                }
            }
        },
        endTime: {
            validators: {
                notEmpty: {
                    message: '结束时间'
                }
            }
        },
        duration: {
            validators: {
                notEmpty: {
                    message: '课时时长'
                }
            }
        },
        period: {
            validators: {
                notEmpty: {
                    message: '课时数'
                }
            }
        },
        classRoomCode: {
            validators: {
                notEmpty: {
                    message: '教室编码'
                }
            }
        },
        quato: {
            validators: {
                notEmpty: {
                    message: '报名人数'
                }
            }
        },
        signEndDate: {
            validators: {
                notEmpty: {
                    message: '报名截止时间'
                }
            }
        },
        courseCode: {
            validators: {
                notEmpty: {
                    message: '教授课程'
                }
            }
        }
    }
};

/**
 * 清除数据
 */
ClassInfoDlg.clearData = function() {
    this.classInfoData = {};
}

/**
 * 设置对话框中的数据
 *
 * @param key 数据的名称
 * @param val 数据的具体值
 */
ClassInfoDlg.set = function(key, val) {
    this.classInfoData[key] = (typeof val == "undefined") ? $("#" + key).val() : val;
    return this;
}

/**
 * 设置对话框中的数据
 *
 * @param key 数据的名称
 * @param val 数据的具体值
 */
ClassInfoDlg.get = function(key) {
    return $("#" + key).val();
}

/**
 * 关闭此对话框
 */
ClassInfoDlg.close = function() {
    parent.layer.close(window.parent.Class.layerIndex);
}

/**
 * 收集数据
 */
ClassInfoDlg.collectData = function() {
    this
    .set('id')
    .set('code')
    .set('name')
    .set('beginDate')
    .set('endDate')
    .set('studyTimeType')
    .set('studyTimeValue')
    .set('beginTime')
    .set('endTime')
    .set('duration')
    .set('period')
    .set('classRoomCode')
    .set('classRoom')
    .set('courseCode')
    .set('courseName')
    .set('star')
    .set('quato')
    .set('signEndDate')
    .set('status');
}

/**
 * 验证数据是否为空
 */
ClassInfoDlg.validate = function () {
    $('#classInfoForm').data("bootstrapValidator").resetForm();
    $('#classInfoForm').bootstrapValidator('validate');
    return $("#classInfoForm").data('bootstrapValidator').isValid();
};

/**
 * 提交添加
 */
ClassInfoDlg.addSubmit = function() {

    this.clearData();
    this.collectData();

    if (!this.validate()) {
        return;
    }

    //提交信息
    var ajax = new $ax(Feng.ctxPath + "/class/add", function(data){
        Feng.success("添加成功!");
        window.parent.Class.table.refresh();
        ClassInfoDlg.close();
    },function(data){
        Feng.error("添加失败!" + data.responseJSON.message + "!");
    });
    ajax.set(this.classInfoData);
    ajax.start();
}

/**
 * 提交修改
 */
ClassInfoDlg.editSubmit = function() {

    this.clearData();
    this.collectData();

    if (!this.validate()) {
        return;
    }

    //提交信息
    var ajax = new $ax(Feng.ctxPath + "/class/update", function(data){
        Feng.success("修改成功!");
        window.parent.Class.table.refresh();
        ClassInfoDlg.close();
    },function(data){
        Feng.error("修改失败!" + data.responseJSON.message + "!");
    });
    ajax.set(this.classInfoData);
    ajax.start();
}

$(function() {
    //非空校验
    Feng.initValidator("classInfoForm", ClassInfoDlg.validateFields);

    //初始select选项
    $("#classRoomCode").val($("#classRoomCodeValue").val());
    $("#status").val($("#statusValue").val());
    $("#studyTimeType").val($("#studyTimeTypeValue").val());
    $("#classRoomCode").val($("#classRoomCodeValue").val());
});
