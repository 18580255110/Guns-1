/**
 * 教师管理管理初始化
 */
var Teacher = {
    id: "TeacherTable",	//表格id
    seItem: null,		//选中的条目
    table: null,
    layerIndex: -1
};

/**
 * 初始化表格的列
 */
Teacher.initColumn = function () {
    return [
        {field: 'selectItem', radio: true},
        {title: '头像', field: 'avatar', visible: true, align: 'center', valign: 'middle', sortable: true,
            formatter:function (value,row,index) {
                return '<img alt="image" class="img-circle" src="'+Feng.ctxPath+'/attachment/download?masterName=Teacher&masterCode='+row.id+'" width="64px" height="64px">';
            }
        },
        {title: '教师编码', field: 'code', visible: true, align: 'center', valign: 'middle', sortable: true},
        {title: '教师名称', field: 'name', visible: true, align: 'center', valign: 'middle', sortable: true},
        {title: '教师类型', field: 'typeName', visible: true, align: 'center', valign: 'middle'},
        {title: '性别', field: 'genderName', visible: true, align: 'center', valign: 'middle'},
        {title: '毕业院校', field: 'graduate', visible: true, align: 'center', valign: 'middle', sortable: true},
        {title: '授课年级', field: 'gradeName', visible: true, align: 'center', valign: 'middle'},
        {title: '教学成果', field: 'havest', visible: true, align: 'center', valign: 'middle', sortable: true},
        {title: '教学经验', field: 'experience', visible: true, align: 'center', valign: 'middle', sortable: true},
        {title: '教学特点', field: 'feature', visible: true, align: 'center', valign: 'middle', sortable: true}
    ];
};

/**
 * 检查是否选中
 */
Teacher.check = function () {
    var selected = $('#' + this.id).bootstrapTable('getSelections');
    if(selected.length == 0){
        Feng.info("请先选中表格中的某一记录！");
        return false;
    }else{
        Teacher.seItem = selected[0];
        return true;
    }
};

/**
 * 点击添加教师管理
 */
Teacher.openAddTeacher = function () {
    var index = layer.open({
        type: 2,
        title: '添加教师管理',
        area: ['640px', '500px'], //宽高
        fix: false, //不固定
        maxmin: true,
        content: Feng.ctxPath + '/teacher/teacher_add'
    });
    layer.full(index);
    this.layerIndex = index;
};

/**
 * 打开查看教师管理详情
 */
Teacher.openTeacherDetail = function () {
    if (this.check()) {
        var index = layer.open({
            type: 2,
            title: '教师管理详情',
            area: ['640px', '500px'], //宽高
            fix: false, //不固定
            maxmin: true,
            content: Feng.ctxPath + '/teacher/teacher_update/' + Teacher.seItem.id
        });
        layer.full(index);
        this.layerIndex = index;
    }
};

/**
 * 删除教师管理
 */
Teacher.delete = function () {
    if (this.check()) {
        var ajax = new $ax(Feng.ctxPath + "/teacher/delete", function (data) {
            Feng.success("删除成功!");
            Teacher.table.refresh();
        }, function (data) {
            Feng.error("删除失败!" + data.responseJSON.message + "!");
        });
        ajax.set("code",this.seItem.code);
        ajax.start();
    }
};

/**
 * 查询教师管理列表
 */
Teacher.search = function () {
    var queryData = {};
    queryData['condition'] = $("#condition").val();
    Teacher.table.refresh({query: queryData});
};

$(function () {
    var defaultColunms = Teacher.initColumn();
    var table = new BSTable(Teacher.id, "/teacher/list", defaultColunms);
    table.setPaginationType("server");
    Teacher.table = table.init();
});
