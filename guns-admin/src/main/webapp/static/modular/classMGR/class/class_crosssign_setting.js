/**
 *
 */
var ClassCrosssignSetting = {
    classInfoData : {},
    validateFields: {
        crossable: {
            validators: {
                cross_v: {
                    message: ' ',
                    onError: function(e, data){
                        Feng.error('请设置跨报时间');
                    }
                }
            }
        }
    }
};


/**
 * 清除数据
 */
ClassCrosssignSetting.clearData = function() {
    console.log('<--- clear Data --->');
    this.classInfoData = {};
    console.log('<--- clear Data over --->');
}

/**
 * 设置对话框中的数据
 *
 * @param key 数据的名称
 * @param val 数据的具体值
 */
ClassCrosssignSetting.set = function(key, val) {
    this.classInfoData[key] = (typeof val == "undefined") ? $("#" + key).val() : val;
    return this;
}

/**
 * 设置对话框中的数据
 *
 * @param key 数据的名称
 * @param val 数据的具体值
 */
ClassCrosssignSetting.get = function(key) {
    return $("#" + key).val();
}

/**
 * 关闭此对话框
 */
ClassCrosssignSetting.close = function() {
    parent.layer.close(window.parent.Class.layerIndex);
};

/**
 * 收集数据
 */
ClassCrosssignSetting.collectData = function() {

    this
        .set('id')
        .set('code')
    ;
    var crossable = $(':radio[name="crossable"]:checked').val();
    if (1 == crossable ){
        this
            .set('crossStartDate')
            .set('crossEndDate')
        ;
    }
    this.classInfoData['crossable'] = crossable;
}

/**
 * 验证数据是否为空
 */
ClassCrosssignSetting.validate = function () {
    $('#classInfoForm').data("bootstrapValidator").resetForm();
    $('#classInfoForm').bootstrapValidator('validate');
    return $("#classInfoForm").data('bootstrapValidator').isValid();
};

/**
 * 提交添加
 */
ClassCrosssignSetting.doSubmit = function() {
    console.log('<<< add submit');
    this.clearData();
    if (!this.validate()) {
        return;
    }

    this.collectData();

    //提交信息
    var ajax = new $ax(Feng.ctxPath + "/class/crosssign_setting/save", function(data){
        Feng.success("设置成功!");
        ClassCrosssignSetting.close();
    },function(data){
        Feng.error("添加失败!" + data.responseJSON.message + "!");
    });
    console.log(this.classInfoData);
    ajax.set(this.classInfoData);
    ajax.start();
}

$(function() {
    // 注册验证方法
    $.fn.bootstrapValidator.validators.cross_v = {
        validate: function() {
            var crossable = $(':radio[name="crossable"]:checked').val();
            var crossBeginDate = $('#crossStartDate').val();
            var crossEndDate = $('#crossEndDate').val();
            return 1 == crossable ? (crossBeginDate.length > 0 && crossEndDate.length > 0): true;
        }
    };

    //非空校验
    Feng.initValidator("classInfoForm", ClassCrosssignSetting.validateFields);

    var now = new Date();
    var year = now.getFullYear();
    var month = 1 + now.getMonth();
    if (month < 10)
        month = '0' + month;
    var day = now.getDate();
    if (day < 10)
        day = '0' + day;

    var today = year + '-' + month + '-' + day;
    console.log('Calendar today : ' + today);

    $(':radio[name="crossable"]').bind('click', function(){
        console.log($(this).val());
        var switchValue = $(this).val();

        if (1 == switchValue){
            $('#crossStartDate').parents('.form-group').show();
            $('#crossEndDate').parents('.form-group').show();
        }else{
            $('#crossStartDate').parents('.form-group').hide();
            $('#crossEndDate').parents('.form-group').hide();
        }
    });
    // 初始化
    var crossable = parseInt($('#crossableValue').val(), 10);

    if (isNaN(crossable))
        crossable = 0;

    if (crossable > 0){
        $('#crossStartDate').parents('.form-group').show();
        $('#crossEndDate').parents('.form-group').show();

        $(':radio[value="1"]').prop('checked', true);
    }
    //日期控件初始化
    laydate.render({elem: '#crossStartDate', min: today});
    laydate.render({elem: '#crossEndDate', min: today});
});
