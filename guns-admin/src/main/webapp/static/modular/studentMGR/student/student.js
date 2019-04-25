/**
 * 学生管理管理初始化
 */
var Student = {
    id: "StudentTable",	//表格id
    seItem: null,		//选中的条目
    table: null,
    layerIndex: -1
};

/**
 * 初始化表格的列
 */
Student.initColumn = function () {
    return [
        {field: 'selectItem', radio: true},
            {title: '头像', field: 'avatar', visible: true, align: 'center', valign: 'middle',
                formatter:function (value,row,index) {
                    return '<img alt="image" class="img-circle" src="'+Feng.ctxPath+'/attachment/download?masterName=Student&masterCode='+row.code+'" width="64px" height="64px">';
                }
            },
            {title: '学员编码', field: 'code', visible: true, align: 'center', valign: 'middle', sortable: true},
            {title: '学员名称', field: 'name', visible: true, align: 'center', valign: 'middle'},
            {title: '性别', field: 'genderName', visible: true, align: 'center', valign: 'middle'},
            {title: '在读年级', field: 'gradeName', visible: true, align: 'center', valign: 'middle'},
            {title: '在读学校', field: 'school', visible: true, align: 'center', valign: 'middle'},
            {title: '目标学校', field: 'targetSchool', visible: true, align: 'center', valign: 'middle'},
            {title: '家长账号', field: 'userName', visible: true, align: 'center', valign: 'middle'},
            {title: '家长手机号', field: 'parentPhone', visible: true, align: 'center', valign: 'middle'},
            {title: '状态', field: 'status', visible: true, align: 'center', valign: 'middle',
                formatter: function(value, row){
                    if (1 == value)
                        return '<input type="checkbox" class="js-switch" data-code="'+row.code+'" checked />';
                    else
                        return '<input type="checkbox" class="js-switch" data-code="'+row.code+'" />';
                }
            }
    ];
};

/**
 * 检查是否选中
 */
Student.check = function () {
    var selected = $('#' + this.id).bootstrapTable('getSelections');
    if(selected.length == 0){
        Feng.info("请先选中表格中的某一记录！");
        return false;
    }else{
        Student.seItem = selected[0];
        return true;
    }
};

/**
 * 点击添加学生管理
 */
Student.openAddStudent = function () {
    var index = layer.open({
        type: 2,
        title: '添加学生管理',
        area: ['800px', '640px'], //宽高
        fix: false, //不固定
        maxmin: true,
        content: Feng.ctxPath + '/student/student_add'
    });
    layer.full(index);
    this.layerIndex = index;
};

/**
 * 打开查看学生管理详情
 */
Student.openStudentDetail = function () {
    if (this.check()) {
        var index = layer.open({
            type: 2,
            title: '学生管理详情',
            area: ['850px', '650px'], //宽高
            fix: false, //不固定
            maxmin: true,
            content: Feng.ctxPath + '/student/student_update/' + Student.seItem.id
        });
        layer.full(index);
        this.layerIndex = index;
    }
};

/**
 * 查询学生管理列表
 */
Student.search = function () {
    var queryData = {};
    queryData['condition'] = $("#condition").val();
    queryData['status'] = $("#status").val();
    Student.table.refresh({query: queryData});
};

Student.doUpdate = function(reqUrl, data){
    var ajax = new $ax(reqUrl, function (data) {
    }, function (data) {
        Feng.error("操作失败!" + data.responseJSON.message + "!");
    });

    ajax.setData(data);
    ajax.start();
}

$(function () {
    var defaultColunms = Student.initColumn();
    var table = new BSTable(Student.id, "/student/list", defaultColunms);
    table.setPaginationType("server");
    table.setLoadSuccessCallback(function(){
        var switchers = Array.prototype.slice.call(document.querySelectorAll('.js-switch'));

        switchers.forEach(function(switcher) {
            var switchery = new Switchery(switcher, {
                size: 'small'
            });
            switcher.onchange = function(){
                var state = switcher.checked;
                console.log('<<< change student state' + $(switcher).attr('data-code') + ' : ' + state);

                var reqUrl = '';
                var postData = {
                    code : $(switcher).attr('data-code')
                };

                if (state){
                    // true 启用
                    reqUrl = Feng.ctxPath + "/student/resume";
                    Student.doUpdate(reqUrl, postData);
                }else{
                    // false 停用
                    reqUrl = Feng.ctxPath + "/student/pause";
                    var tip = '停用学员将导致所有该名学员的选课、测试信息失效，是否继续？';
                    parent.layer.confirm(tip, {
                        btn: ['确定', '取消']
                    }, function (index) {
                        Student.doUpdate(reqUrl, postData);
                        parent.layer.close(index);
                    }, function (index) {
                        switchery.setPosition(true);
                        switchery.handleOnchange(true);

                        parent.layer.close(index);
                    });
                }
            }
        });
    });
    Student.table = table.init();
});
