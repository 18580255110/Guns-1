/**
 * 初始化学生管理详情对话框
 */
var StudentInfoDlg = {
    studentInfoData : {},
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
                    message: '姓名不能为空'
                }
            }
        },
        gender: {
            validators: {
                notEmpty: {
                    message: '性别不能为空'
                }
            }
        },
        grade: {
            validators: {
                notEmpty: {
                    message: '在读年级不能为空'
                }
            }
        },
        parentPhone: {
            validators: {
                notEmpty: {
                    message: '家长手机号不能为空'
                }
            }
        },
    }
};

/**
 * 清除数据
 */
StudentInfoDlg.clearData = function() {
    this.studentInfoData = {};
}

/**
 * 设置对话框中的数据
 *
 * @param key 数据的名称
 * @param val 数据的具体值
 */
StudentInfoDlg.set = function(key, val) {
    this.studentInfoData[key] = (typeof val == "undefined") ? $("#" + key).val() : val;
    return this;
}

/**
 * 设置对话框中的数据
 *
 * @param key 数据的名称
 * @param val 数据的具体值
 */
StudentInfoDlg.get = function(key) {
    return $("#" + key).val();
}

/**
 * 关闭此对话框
 */
StudentInfoDlg.close = function() {
    parent.layer.close(window.parent.Student.layerIndex);
}

/**
 * 收集数据
 */
StudentInfoDlg.collectData = function() {
    this
    .set('id')
    .set('code')
    .set('name')
    .set('avatar')
    .set('gender')
    .set('grade')
    .set('school')
    .set('targetSchool')
    .set('status')
    .set('masterName')
    .set('masterCode')
    .set('userName')
    .set('parentPhone')
    ;
}

/**
 * 验证数据是否为空
 */
StudentInfoDlg.validate = function () {
    $('#studentInfoForm').data("bootstrapValidator").resetForm();
    $('#studentInfoForm').bootstrapValidator('validate');
    return $("#studentInfoForm").data('bootstrapValidator').isValid();
};

/**
 * 提交添加
 */
StudentInfoDlg.addSubmit = function() {

    this.clearData();
    this.collectData();

    if (!this.validate()) {
        return;
    }

    //提交信息
    var ajax = new $ax(Feng.ctxPath + "/student/add", function(data){
        Feng.success("添加成功!");
        window.parent.Student.table.refresh();
        StudentInfoDlg.close();
    },function(data){
        Feng.error("添加失败!" + data.responseJSON.message + "!");
    }); 
    ajax.set(this.studentInfoData);
    ajax.set("parentPhone",studentInfoData["parentPhone"]);
    ajax.start();
}

/**
 * 提交修改
 */
StudentInfoDlg.editSubmit = function() {

    this.clearData();
    this.collectData();

    if (!this.validate()) {
        return;
    }
    
    //提交信息
    var ajax = new $ax(Feng.ctxPath + "/student/update", function(data){
        Feng.success("修改成功!");
        window.parent.Student.table.refresh();
        StudentInfoDlg.close();
    },function(data){
        Feng.error("修改失败!" + data.responseJSON.message + "!");
    });
    ajax.set(this.studentInfoData);
    ajax.start();
}

$(function() {
    //非空校验
    Feng.initValidator("studentInfoForm", StudentInfoDlg.validateFields);

    //初始select选项
    $("#gender").val($("#genderValue").val());
    $("#type").val($("#typeValue").val());
    $("#grade").val($("#gradeValue").val());


    // 初始化头像上传
    var avatarUp = new $WebUpload("avatar");
    avatarUp.setUploadBarId("progressBar");
    avatarUp.init();

});
