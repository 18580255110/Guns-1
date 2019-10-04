/**
 * 查分初始化
 */
var Score = {
    id: "ScoreTable",	//表格id
    seItem: null,		//选中的条目
    table: null,
    layerIndex: -1
};

/**
 * 初始化表格的列
 */
Score.initColumn = function () {
    return [
        {field: 'selectItem', radio: true},
        {title: '学员编码', field: 'studentCode', visible: true, align: 'center', valign: 'middle'},
        {title: '学员名称', field: 'student', visible: true, align: 'center', valign: 'middle'},
        {title: '联系电话', field: 'mobileNumber', visible: true, align: 'center', valign: 'middle'},
        {title: '考试名称', field: 'examineName', visible: true, align: 'center', valign: 'middle'},
        {title: '场 次', field: 'round', visible: true, align: 'center', valign: 'middle'},
        {title: '得分值', field: 'score', visible: true, align: 'center', valign: 'middle'},
        {title: '总分值', field: 'totalScore', visible: true, align: 'center', valign: 'middle'},
        {title: '排 名', field: 'rank', visible: true, align: 'center', valign: 'middle'},
        {title: '可报班级', field: 'classNames', visible: true, align: 'center', valign: 'middle'},
        {title: '备注', field: 'remark', visible: true, align: 'center', valign: 'middle'},
    ];
};

/**
 * 查询得分列表
 */
Score.search = function () {
    var queryData = {};
    queryData['student'] = $("#student").val();
    queryData['mobileNumber'] = $("#mobileNumber").val();
    queryData['examine'] = $("#examine").val();
    queryData['beginScore'] = $("#beginScore").val();
    queryData['endScore'] = $("#endScore").val();
    Score.table.refresh({query: queryData});
    Score.table.setQueryParams(queryData);
};

$(function () {
    var defaultColunms = Score.initColumn();
    var table = new BSTable(Score.id, "/examine/achievement/score/list", defaultColunms);
    table.setPaginationType("server");
    Score.table = table.init();
});
