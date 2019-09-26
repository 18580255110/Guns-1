/**
 * 初始化资讯管理详情对话框
 */
var ContentInfoDlg = {
    contentInfoData : {},
    editor: null,
    validateFields: {
        code: {
            validators: {
                notEmpty: {
                    message: '编码不能为空'
                }
            }
        },
        type: {
            validators: {
                notEmpty: {
                    message: '类型不能为空'
                }
            }
        },
        timage: {
            validators: {
                notEmpty: {
                    message: '标题图片不能为空'
                }
            }
        },
        introduce: {
            validators: {
                notEmpty: {
                    message: '简介不能为空'
                }
            }
        },
        publishType: {
            validators: {
                notEmpty: {
                    message: '发布类型不能为空'
                }
            }
        },
        status: {
            validators: {
                notEmpty: {
                    message: '状态不能为空'
                }
            }
        }
    }
};

/**
 * 清除数据
 */
ContentInfoDlg.clearData = function() {
    this.contentInfoData = {};
}

/**
 * 设置对话框中的数据
 *
 * @param key 数据的名称
 * @param val 数据的具体值
 */
ContentInfoDlg.set = function(key, val) {
    this.contentInfoData[key] = (typeof val == "undefined") ? $("#" + key).val() : val;
    return this;
}

/**
 * 设置对话框中的数据
 *
 * @param key 数据的名称
 * @param val 数据的具体值
 */
ContentInfoDlg.get = function(key) {
    return $("#" + key).val();
}

/**
 * 关闭此对话框
 */
ContentInfoDlg.close = function() {
    parent.layer.close(window.parent.Content.layerIndex);
}

/**
 * 收集数据
 */
ContentInfoDlg.collectData = function() {
    this.contentInfoData['content'] = ContentInfoDlg.editor.txt.html();
    console.log(ContentInfoDlg.editor.txt.html());
    this
    .set('id')
    .set('code')
    .set('type')
    .set('timage')
    .set('title')
    .set('introduce')
    .set('author')
    .set('publishType')
    .set('createDate')
    .set('deadDate')
    .set('status')
        .set('masterName')
        .set('masterCode');
}

/**
 * 提交添加
 */
ContentInfoDlg.addSubmit = function() {

    this.clearData();
    this.collectData();
    //提交信息
    var ajax = new $ax(Feng.ctxPath + "/content/add", function(data){
        Feng.success("添加成功!");
        window.parent.Content.table.refresh();
        ContentInfoDlg.close();
    },function(data){
        Feng.error("添加失败!" + data.responseJSON.message + "!");
    });
    ajax.set(this.contentInfoData);
    ajax.start();
}

/**
 * 提交修改
 */
ContentInfoDlg.editSubmit = function() {

    this.clearData();
    this.collectData();

    //提交信息
    var ajax = new $ax(Feng.ctxPath + "/content/update", function(data){
        Feng.success("修改成功!");
        window.parent.Content.table.refresh();
        ContentInfoDlg.close();
    },function(data){
        Feng.error("修改失败!" + data.responseJSON.message + "!");
    });
    ajax.set(this.contentInfoData);
    ajax.start();
}

$(function() {
    //非空校验
    Feng.initValidator("contentInfoForm", ContentInfoDlg.validateFields);

    //初始select选项
    $("#status").val($("#statusValue").val());
    $("#type").val($("#typeValue").val());
    $("#publishType").val($("#publishTypeValue").val());


    // 初始化图片上传
    var avatarUp = new $WebUpload("timage");
    avatarUp.setUploadBarId("progressBar");
    avatarUp.init();

    //日期
    laydate.render({
        elem: '#deadDate'
    });

    //初始化编辑器
    var E = window.wangEditor;
    var editor = new E('#editor');
    // 配置服务器端地址
    editor.customConfig.uploadImgServer = Feng.ctxPath + '/attachment/upload/async';
    editor.customConfig.uploadFileName = 'files';
    editor.customConfig.uploadImgHooks = {
        customInsert: function (insertImg, result, editor) {
            // 图片上传并返回结果，自定义插入图片的事件（而不是编辑器自动插入图片！！！）
            // insertImg 是插入图片的函数，editor 是编辑器对象，result 是服务器端返回的结果

            // 举例：假如上传图片成功后，服务器端返回的是 {url:'....'} 这种格式，即可这样插入图片：
            var url = 'http://www.kecui.com.cn/download?masterName=' + result.data.name + '&masterCode=' + result.data.code;
            insertImg(url);

            // result 必须是一个 JSON 格式字符串！！！否则报错
        }
    }
    editor.create();
    editor.txt.html($("#contentVal").val());
    ContentInfoDlg.editor = editor;

});
