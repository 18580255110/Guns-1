/**
 * 初始化课程管理详情对话框
 */
var ClassInfoDlg = {
    courseTable : {
        id : 'courseTable'
    },
    layerIndex: -1,
    currClassId: $('#id').val(),
    currClass: $('#code').val(),
    otherPlanList: new Array(),
    minePlanList: new Array(),
    calendar: null,
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
        price: {
            validators: {
                notEmpty: { message: '金额不能为空'},
                numeric: { message: '无效的金额'},
                between: { min: 0.00, max: 9999.99, message: '无效的金额'}
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
                },
                regexp: {
                    regexp: /^[1-9][0-9]{0,9}$/,
                    message: '9位数字内'
                }
            }
        },
        signStartDate: {
            validators: {
                notEmpty: {
                    message: '报名开始时间'
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
        star: {
            validators: {
                notEmpty: {
                    message: '关注度'
                },
                regexp: {
                    regexp: /^[0-9]{0,2}$/,
                    message: '2位数字内'
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
 * 初始化表格的列
 */
ClassInfoDlg.courseTable.initColumn = function () {
    return [
        {field: 'selectItem', radio: true},
        {title: '课程编码', field: 'code', visible: false, align: 'center', valign: 'middle'},
        {title: '课程名称', field: 'name', visible: true, align: 'center', valign: 'middle'},
        {title: '授课方式', field: 'methodName', visible: false, align: 'center', valign: 'middle'},
        {title: '授课年级', field: 'gradeName', visible: false, align: 'center', valign: 'middle'},
        {title: '学科', field: 'subjectName', visible: false, align: 'center', valign: 'middle'},
        {title: '课时数', field: 'period', visible: false, align: 'center', valign: 'middle'},
    ];
};

/**
 * 清除数据
 */
ClassInfoDlg.clearData = function() {
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
};

/**
 * 收集数据
 */
ClassInfoDlg.collectData = function() {

    var submitPrice = Math.floor(100 * parseFloat($('#price').val(), 10));

    this
        .set('id')
        .set('code')
        .set('name')
        .set('grade')
        .set('cycle')
        .set('ability')
        .set('duration')
        .set('period')
        .set('courseCode')
        .set('courseName')
        .set('star')
        .set('price')
        .set('quato')
        .set('signStartDate')
        .set('signEndDate')
        .set('status')
        .set('teacherCode')
        .set('teacher')
        .set('teacherSecondCode')
        .set('teacherSecond')
        .set('classRoomCode')
        .set('classRoom')
        .set('academicYear')
        .set('studyTimeDesp');

    this.classInfoData['price'] = submitPrice;
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
    console.log('<<< add submit');
    this.clearData();
    if (!this.validate()) {
        return;
    }
    $("#classRoom").val($("#classRoomCode option:selected").text());
    $("#teacher").val($("#teacherCode option:selected").text());
    $("#teacherSecond").val($("#teacherSecondCode option:selected").text());

    this.collectData();
    
    //提交信息
    var ajax = new $ax(Feng.ctxPath + "/class/add", function(data){
        Feng.success("添加成功!");
        window.parent.Class.table.refresh();
        ClassInfoDlg.close();
    },function(data){
        Feng.error("添加失败!" + data.responseJSON.message + "!");
    });
    console.log(this.classInfoData);
    console.log(ClassInfoDlg.minePlanList);
    ajax.set(this.classInfoData);
    ajax.set('planList', JSON.stringify(ClassInfoDlg.minePlanList));
    ajax.start();
}

/**
 * 提交修改
 */
ClassInfoDlg.editSubmit = function() {
    console.log('<<< edit submit');
    this.clearData();
    if (!this.validate()) {
        return;
    }
    $("#classRoom").val($("#classRoomCode option:selected").text());
    $("#teacher").val($("#teacherCode option:selected").text());
    $("#teacherSecond").val($("#teacherSecondCode option:selected").text());

    this.collectData();
    //提交信息
    var ajax = new $ax(Feng.ctxPath + "/class/update", function(data){
        Feng.success("修改成功!");
        window.parent.Class.table.refresh();
        ClassInfoDlg.close();
    },function(data){
        Feng.error("修改失败!" + data.responseJSON.message + "!");
    });
    ajax.set(this.classInfoData);
    ajax.set('planList', JSON.stringify(this.minePlanList));
    ajax.start();
}

ClassInfoDlg.initCalendar = function(options){

    var me = this;
    console.log('<<< init calendar >>>');
    console.log(options);
    me.calendar = $('#calendar').fullCalendar({
        //isRTL: true,
        eventLimit: true,
        defaultDate: options.today,
        buttonText: {
            today: '今天',
            month: '月视图',
            week: '周视图',
            day: '日视图'
        },
        allDayText: "全天",
        timeFormat: {
            '': 'H(:mm)'
        },
        axisFormat: 'H时(:mm分)',
        firstHour: 8,
        minTime: 8,
        buttonHtml: {
            prev: '<i class="ace-icon fa fa-chevron-left"></i>',
            next: '<i class="ace-icon fa fa-chevron-right"></i>'
        },
        titleFormat: {
            month: 'YYYY年 MMMM月',
            day: 'YYYY年 MMMM月DD日 dddd'
        },
        monthNames: ["一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二"],
        dayNames: ["星期天", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"],
        dayNamesShort: ["星期天", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"],
        header: {
            left: 'prev,next today',
            center: 'title',
            right: 'month,agendaDay'
        },
        events: options.calEvents
        ,
        selectable: true,
        selectHelper: true,
        select: function(start, end, allDay) {

            var currView = me.calendar.fullCalendar('getView');
            if ('month' == currView.name){
                // 跳转视图到day
                me.calendar.fullCalendar('changeView', 'agendaDay');
                me.calendar.fullCalendar('gotoDate', start.format('YYYY-MM-DD'));
            }else if ('agendaDay' == currView.name){
                bootbox.prompt("排班描述:", function(title) {
                    if (title !== null) {

                        var beginTime = start.format('H:mm');
                        var endTime = end.format('H:mm');
                        var eventId = new Date().getTime();
                        me.calendar.fullCalendar('renderEvent',
                            {
                                id: eventId,
                                title: '|' + beginTime + ' ~ ' + endTime + ': ' + title,
                                start: start,
                                end: end
                            },
                            true // make the event "stick"
                        );

                        me.minePlanList.push({
                            id: eventId,
                            studyDate: start.format('YYYY-MM-DD'),
                            classTime: start.format('HHmm'),
                            endTime: end.format('HHmm'),
                            week: start.format('E')
                        });

                        console.log(ClassInfoDlg.minePlanList);

                        me.calendar.fullCalendar('changeView', 'month');
                    }
                });
            }

            me.calendar.fullCalendar('unselect');
        }
        ,
        editable: true
        ,
        eventDrop: function(event, delta, revertFunc, jsEvent, ui, vie){
            console.log('<<< event drop')
            console.log(event);
            var newEvent = $.extend({}, event);
            newEvent.id = (new Date()).getTime();
            console.log('new event' + newEvent.id);
            me.calendar.fullCalendar('renderEvent',
                {
                    id: newEvent.id,
                    title: event.title,
                    start: event.start,
                    end: event.end
                },
                true // make the event "stick"
            );

            me.minePlanList.push({
                id: newEvent.id,
                studyDate: event.start.format('YYYY-MM-DD'),
                classTime: event.start.format('HHmm'),
                endTime: event.end.format('HHmm'),
                week: event.start.format('E')
            });

            revertFunc();
        }
        ,
        eventClick: function(event, jsEvent, view) {
            var eventId = parseInt(event.id, 10);
            var _id = me.currClass + '_' + me.currClassId;
            if (isNaN(eventId)) {
                if ( 0 != event.id.indexOf(me.currClass) )
                    return false;
            }
            //display a modal
            var modal =
                '<div class="modal fade">\
                  <div class="modal-dialog">\
                   <div class="modal-content">\
                     <div class="modal-body">\
                       <button type="button" class="close" data-dismiss="modal" style="margin-top:-10px;">&times;</button>\
                       <form class="no-margin">\
                          <label>修改 &nbsp;</label>\
                          <input class="middle" autocomplete="off" type="text" value="' + event.title + '" />\
					 <button type="submit" class="btn btn-sm btn-success"><i class="ace-icon fa fa-check"></i> 保存修改</button>\
				   </form>\
				 </div>\
				 <div class="modal-footer">\
					<button type="button" class="btn btn-sm btn-danger" data-action="delete"><i class="ace-icon fa fa-trash-o"></i> 删 除</button>\
					<button type="button" class="btn btn-sm" data-dismiss="modal"><i class="ace-icon fa fa-times"></i> 取 消</button>\
				 </div>\
			  </div>\
			 </div>\
			</div>';


            var modal = $(modal).appendTo('body');
            modal.find('form').on('submit', function(ev){
                ev.preventDefault();

                event.title = $(this).find("input[type=text]").val();
                me.calendar.fullCalendar('updateEvent', event);
                modal.modal("hide");
            });
            modal.find('button[data-action=delete]').on('click', function() {

                me.calendar.fullCalendar('removeEvents' , function(ev){
                    return (ev._id == event._id);
                });

                var newMinePlanList = new Array();

                console.log('<<< start remove minePlanList');
                console.log(me.minePlanList);
                $.each(me.minePlanList, function(idx, eo){

                    if (eo.id == event._id || me.currClass + '_' + eo.id == event._id){
                        return true;
                    }

                    newMinePlanList.push({
                        id: eo.id,
                        studyDate: eo.studyDate,
                        classTime: eo.classTime,
                        endTime: eo.endTime,
                        week: eo.week
                    })
                });

                me.minePlanList = newMinePlanList;
                console.log('>>> update over');
                console.log(me.minePlanList);

                modal.modal("hide");
            });

            modal.modal('show').on('hidden', function(){
                modal.remove();
            });
        }
    });
};


$(function() {

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
    var initEvents = new Array();

    $.ajax({
        async: true,
        url: Feng.ctxPath + '/class/plan/list',
        dataType: 'json',
        data: {classCode: ClassInfoDlg.currClass},
        success: function(response) {
            console.log('<<< load events');

            console.log(response.classPlanList);
            ClassInfoDlg.otherPlanList = ClassInfoDlg.otherPlanList.concat(response.allClassPlanList);
            ClassInfoDlg.minePlanList = ClassInfoDlg.minePlanList.concat(response.classPlanList);

            console.log(ClassInfoDlg.minePlanList);
/*
            $(response.allClassPlanList).each(function(idx, eo) {
                initEvents.push({
                    id: eo.classCode + '_' + eo.id,
                    title: eo.description,
                    start: eo.classBeginTime,
                    end: eo.classEndTime,
                    editable: false,
                    color: '#ffffff',
                    backgroundColor: '#a0a0a0'
                });
            });
            */
            var newToday = null;
            $(response.classPlanList).each(function(idx, eo) {
                initEvents.push({
                    id: eo.classCode + '_' + eo.id,
                    title: eo.description,
                    start: eo.classBeginTime,
                    end: eo.classEndTime,
                    color: '#ffffff',
                    backgroundColor: '#82af6f'
                });
                if (null == newToday)
                    newToday = eo.classBeginTime;
            });

            if (null == newToday)
                newToday = today;

            console.log(newToday);
            ClassInfoDlg.initCalendar({
                today: newToday,
                calEvents: initEvents
            });
        }
    });

    // 课程列表初始化
    if ($('#' + ClassInfoDlg.courseTable.id)) {
        var courseDisplaColumns = ClassInfoDlg.courseTable.initColumn();
        var table = new BSTable(ClassInfoDlg.courseTable.id, "/course/list", courseDisplaColumns);
        table.setPageSize(6);
        table.setItemSelectCallback(function (row) {
            console.log(row);
            $('#courseCode').val(row.code);
            $('#courseName').val(row.name);
            $('#subject').val(row.subject);
            $('#subjectName').val(row.subjectName);
            $('#grade').val(row.grade);
            $('#gradeName').val(row.gradeName);
            $('#method').val(row.method);
            $('#methodName').val(row.methodName);
            $('#period').val(row.period);
        });
        table.setPaginationType("server");
        ClassInfoDlg.courseTable.table = table.init();
    }
    //日期控件初始化
    laydate.render({elem: '#signStartDate', min: today});
    laydate.render({elem: '#signEndDate', min: today});

    //非空校验
    Feng.initValidator("classInfoForm", ClassInfoDlg.validateFields);

    /*  学年 */
    var html = "";
    var academicYears = JSON.parse($('#academicYearsValue').val());
    academicYears.forEach(function (item) {
        html +="<option value="+item+">"+item+"学年</option>";
    });
    $("#academicYear").append(html);
    /* 教室 */
    var html = "";
    var ajax = new $ax(Feng.ctxPath + "/classroom/listRoom", function (data) {
        data.forEach(function (item) {
            html +="<option value="+item.code+">"+item.name+"</option>";
        })
    }, function (data) {
        Feng.error("修改失败!" + data.responseJSON.message + "!");
    });
    ajax.start();
    $("#classRoomCode").append(html);
    /* 老师*/
    var html = "";
    var ajax = new $ax(Feng.ctxPath + "/teacher/listAll", function (data) {
        data.forEach(function (item) {
            html +="<option value="+item.code+">"+item.name+"</option>";
        })
    }, function (data) {
        Feng.error("修改失败!" + data.responseJSON.message + "!");
    });
    ajax.start();
    $("#teacherCode").append(html);
    $("#teacherSecondCode").append(html);

    //初始select选项
    $("#classRoomCode").val($("#classRoomCodeValue").val());
    $('#cycle').val($('#cycleValue').val());
    $('#ability').val($('#abilityValue').val());
    $("#teacherCode").val($("#teacherCodeValue").val());
    $("#teacherSecondCode").val($("#teacherSecondCodeValue").val());
    $("#academicYear").val($("#academicYearValue").val());

});
