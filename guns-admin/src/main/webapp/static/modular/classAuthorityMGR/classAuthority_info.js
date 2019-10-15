/**
 * 初始化详情对话框
 */
var ClassAuthorityInfoDlg = {
    classAuthorityInfoData : {}
};

/**
 * 清除数据
 */
ClassAuthorityInfoDlg.clearData = function() {
    this.classAuthorityInfoData = {};
}

/**
 * 设置对话框中的数据
 *
 * @param key 数据的名称
 * @param val 数据的具体值
 */
ClassAuthorityInfoDlg.set = function(key, val) {
    this.classAuthorityInfoData[key] = (typeof val == "undefined") ? $("#" + key).val() : val;
    return this;
}

/**
 * 设置对话框中的数据
 *
 * @param key 数据的名称
 * @param val 数据的具体值
 */
ClassAuthorityInfoDlg.get = function(key) {
    return $("#" + key).val();
}

/**
 * 关闭此对话框
 */
ClassAuthorityInfoDlg.close = function() {
    parent.layer.close(window.parent.ClassAuthority.layerIndex);
}

/**
 * 收集数据
 */
ClassAuthorityInfoDlg.collectData = function() {
    this
    .set('id')
    .set('classCode')
    .set('className')
    .set('studentCode')
    .set('studentName');
}

/**
 * 提交添加
 */
ClassAuthorityInfoDlg.addSubmit = function() {

    this.clearData();
    this.collectData();

    //提交信息
    var ajax = new $ax(Feng.ctxPath + "/classAuthority/add", function(data){
        Feng.success("添加成功!");
        window.parent.ClassAuthority.table.refresh();
        ClassAuthorityInfoDlg.close();
    },function(data){
        Feng.error("添加失败!" + data.responseJSON.message + "!");
    });
    ajax.set(this.classAuthorityInfoData);
    ajax.start();
}

/**
 * 提交修改
 */
ClassAuthorityInfoDlg.editSubmit = function() {

    this.clearData();
    this.collectData();

    //提交信息
    var ajax = new $ax(Feng.ctxPath + "/classAuthority/update", function(data){
        Feng.success("修改成功!");
        window.parent.ClassAuthority.table.refresh();
        ClassAuthorityInfoDlg.close();
    },function(data){
        Feng.error("修改失败!" + data.responseJSON.message + "!");
    });
    ajax.set(this.classAuthorityInfoData);
    ajax.start();
}

/**
 * 关联学生
 */
ClassAuthorityInfoDlg.openAddClassAuthorityStudent = function () {
    console.log("弹出学生框");
    var index = layer.open({
        type: 0,
        title: '选择学生',
        area: ['1200px', '820px'], //宽高
        fix: false, //不固定
        maxmin: true,
        content: Feng.ctxPath + '/classAuthority/classAuthority_add_student'
    });
    this.layerIndex = index;
};


$(function() {

});
