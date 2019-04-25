/**
 * 试卷管理
 */
var ProcessWizard = {
    Service: $('#service').val(),
    Uploader: {},
    Wizard: {
        id: 'wizard',
        postData: {}
    }
};
/**
 * 清理表单
 */
ProcessWizard.clearData = function(){
    this.Wizard.postData = {};
}


/**
 * 设置对话框中的数据
 *
 * @param key 数据的名称
 * @param val 数据的具体值
 */
ProcessWizard.set = function(key, val) {
    this.Wizard.postData[key] = (typeof val == "undefined") ? $("#" + key).val() : val;
    return this;
}

/**
 * 获取对话框中的数据
 *
 * @param key 数据的名称
 * @param val 数据的具体值
 */
ProcessWizard.get = function(key) {
    return $("#" + key).val();
}

/**
 * 收集数据
 */
ProcessWizard.collectData = function() {
    this
        .set('masterName')
        .set('masterCode')
        .set('description');

    this.Wizard.postData['service'] = $('#serviceCode').val();
};

/**
 * 关闭此对话框
 */
ProcessWizard.close = function() {
    parent.layer.close(window.parent.Process.layerIndex);
};

ProcessWizard.downloadTemplate = function(template){
    var ajax = new $ax(Feng.ctxPath + "/batch/template/" + ProcessWizard.Service.toLowerCase(), function(result){
        // var url = 'http://www.kecui.com.cn/download?masterName=' + result.data.name + '&masterCode=' + result.data.code;
        var url = 'http://localhost:8080/admin/attachment/download?masterName=' + result.data.name + '&masterCode=' + result.data.code;

        window.location.href = url;
    },function(data){
        Feng.error("模板下载失败!" + data.responseJSON.message + "!");
    });
    ajax.start();
}

ProcessWizard.Uploader.bindEvent = function(bindedObj){
    var me =  this;
    bindedObj.on('fileQueued', function(file) {
        console.log(file);
        $("#file-info").val(file.name);

    });

    // 文件上传过程中创建进度条实时显示。
    bindedObj.on('uploadProgress', function(file, percentage) {
        $("#progressTipArea").css("width",percentage * 100 + "%");
    });

    // 文件上传成功，给item添加成功class, 用样式标记上传成功。
    bindedObj.on('uploadSuccess', function(file,response) {
        Feng.success("上传成功");
        //$("#" + me.pictureId).val(response);
        console.log(response);
        $('#masterName').val(response.data.name);
        $('#masterCode').val(response.data.code);
    });

    // 文件上传失败，显示上传出错。
    bindedObj.on('uploadError', function(file) {
        Feng.error("上传失败");
    });

    // 其他错误
    bindedObj.on('error', function(type) {
        if ("Q_EXCEED_SIZE_LIMIT" == type) {
            Feng.error("文件大小超出了限制");
        } else if ("Q_TYPE_DENIED" == type) {
            Feng.error("文件类型不满足");
        } else if ("Q_EXCEED_NUM_LIMIT" == type) {
            Feng.error("上传数量超过限制");
        } else if ("F_DUPLICATE" == type) {
            Feng.error("图片选择重复");
        } else {
            Feng.error("上传过程中出错");
        }
    });

    // 完成上传完了，成功或者失败
    bindedObj.on('uploadComplete', function(file) {
    });
}

$(function () {
    // 注册验证方法
    // 初始化附件上传
    //
    var form = $('#' + ProcessWizard.Wizard.id);
    form.steps({
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
            return true;
        },
        onStepChanged: function(event, step, prev){
            console.log('<<< step ' + step + ' change from ' + prev);
            var uploader = WebUploader.create({
                fileVal: 'files',
                auto : true,
                pick : {
                    id : '#picker',
                    multiple : false// 只上传一个
                },
                accept : {
                    title : 'Excel',
                    extensions : 'xlsx',
                    mimeTypes : 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
                },
                swf : Feng.ctxPath + '/static/js/plugins/webuploader/Uploader.swf',
                disableGlobalDnd : true,
                duplicate : true,
                server : Feng.ctxPath + '/attachment/upload/async',
                fileSingleSizeLimit : 2 * 1024 * 1024
            });
            ProcessWizard.Uploader.bindEvent(uploader);
        },
        onFinished: function(){
            ProcessWizard.clearData();
            ProcessWizard.collectData();

            //提交信息
            var ajax = new $ax(Feng.ctxPath + "/batch/import/" + ProcessWizard.Service.toLowerCase(), function(data){
                Feng.success("保存成功!");
                window.parent.Process.table.refresh();
                ProcessWizard.close();
            },function(data){
                Feng.error("保存失败!" + data.responseJSON.message + "!");
            });
            ajax.set(ProcessWizard.Wizard.postData);

            ajax.start();
        }
    });
});
