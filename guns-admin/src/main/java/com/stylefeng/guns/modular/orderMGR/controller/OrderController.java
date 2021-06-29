package com.stylefeng.guns.modular.orderMGR.controller;

import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.common.constant.state.GenericState;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.base.controller.BaseController;
import com.stylefeng.guns.core.base.tips.ErrorTip;
import com.stylefeng.guns.core.base.tips.Tip;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.core.shiro.ShiroKit;
import com.stylefeng.guns.log.LogObjectHolder;
import com.stylefeng.guns.modular.classMGR.service.IClassService;
import com.stylefeng.guns.modular.orderMGR.service.ICourseCartService;
import com.stylefeng.guns.modular.orderMGR.service.IOrderService;
import com.stylefeng.guns.modular.orderMGR.warpper.OrderDetail;
import com.stylefeng.guns.modular.orderMGR.warpper.OrderWrapper;
import com.stylefeng.guns.modular.system.model.*;
import com.stylefeng.guns.modular.system.model.Class;
import com.stylefeng.guns.modular.system.service.IAttachmentService;
import com.stylefeng.guns.util.DateUtil;
import com.stylefeng.guns.util.PathUtil;
import com.stylefeng.guns.util.ToolUtil;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static com.stylefeng.guns.util.ExcelUtil.addCell;
import static com.stylefeng.guns.util.ExcelUtil.addCells;

/**
 * 订单管理控制器
 *
 * @author fengshuonan
 * @Date 2018-10-18 22:46:16
 */
@Controller
@RequestMapping("/order")
public class OrderController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private static final BigDecimal TRANS_UNIT = new BigDecimal(100);

    private String PREFIX = "/orderMGR/order/";

    @Value("${application.attachment.visit-url}")
    private String viewPath = "/";

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IClassService classService;

    @Autowired
    private ICourseCartService courseCartService;

    @Autowired
    private IAttachmentService attachmentService;


    private static final List<String> ORDER_HEADER_DEFINE = new ArrayList<String>(){
        private static final long serialVersionUID = -5456067913018841267L;
        {
            add("序号");
            add("订单号");
            add("总金额");
            add("学生姓名");
            add("所报班级");
            add("授课老师");
            add("会员");
            add("会员联系电话");
            add("支付渠道");
            add("支付结果");
            add("下单时间");
            add("支付时间");
        }
    };

    /**
     * 跳转到订单管理首页
     */
    @RequestMapping("/class")
    public String index() {
        return PREFIX + "order.html";
    }

    /**
     * 跳转到修改订单管理
     */
    @RequestMapping("/class/order_update/{orderId}")
    public String orderUpdate(@PathVariable Integer orderId, Model model) {
        Order order = orderService.selectById(orderId);
        model.addAttribute("item", order);
        LogObjectHolder.me().set(order);
        return PREFIX + "order_edit.html";
    }

    /**
     * 获取订单管理列表
     */
    @RequestMapping(value = "/class/list")
    @ResponseBody
    public Object list(@RequestParam  Map<String, Object> queryParmas) {
        List<String> roleList = ShiroKit.getUser().getRoleNames();

        if (!hasCondition(queryParmas) && !roleList.contains("超级管理员") && !roleList.contains("系统管理员")){
            queryParmas.put("modQuery", true);
        }
        //分页查詢
        Page<Map<String, Object>> pageMap = orderService.selectMapsPage(queryParmas);
        //包装数据
        new OrderWrapper(pageMap.getRecords()).warp();
        return super.packForBT(pageMap);
    }

    /**
     * 关闭订单管理
     */
    @RequestMapping(value = "/class/close")
    @ResponseBody
    public Object delete(@RequestParam String orderNo) {

        if (null == orderNo)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"订单号"});

        orderService.cancel(orderNo);

        return SUCCESS_TIP;
    }


    /**
     * 撤销订单管理
     */
    @RequestMapping(value = "/class/doReverse/{orderNo}")
    @ResponseBody
    public Object reverse(@PathVariable("orderNo") String orderNo, @RequestParam("classCode") String classCode, @RequestParam("desc") String desc) {
        if (null == orderNo)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"订单号"});

        orderService.doReverse(orderNo, classCode, desc);

        return SUCCESS_TIP;
    }


    /**
     * 导出订单管理
     */
    @RequestMapping(value = "/class/export")
    @ResponseBody
    public Object export(@RequestParam Map<String, Object> queryParams) {
        List<String> roleList = ShiroKit.getUser().getRoleNames();

        if (!hasCondition(queryParams) && !roleList.contains("超级管理员") && !roleList.contains("系统管理员")){
            queryParams.put("modQuery", true);
        }

        Map<String, Object> resultMap = new HashMap<String, Object>();

        List<Map<String, Object>> orderList = orderService.queryForList(queryParams);

        if (null == orderList){
            orderList = new ArrayList<Map<String, Object>>();
        }

        Long totalAmount = 0L;
        for(Map<String, Object> orderMap : orderList){
            if (orderMap.containsKey("amount")){
                totalAmount += (Long)orderMap.get("amount");
            }
        }

        resultMap.put("dataResult", orderList);
        resultMap.put("totalAmount", new BigDecimal(totalAmount).divide(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_DOWN).toString());
        resultMap.put("totalCount", orderList.size());

        XSSFWorkbook workbook = buildOrderExportWorkbook(ORDER_HEADER_DEFINE, resultMap);

        File storeFolder = attachmentService.getStoreFolder();
        String filename = attachmentService.getFilename();
        File destFile = new File(storeFolder, filename);

        Tip result = null;
        boolean exportSucceed = false;
        try {
            workbook.write(new FileOutputStream(destFile));
            exportSucceed = true;
        } catch (IOException e) {
            e.printStackTrace();
            result = new ErrorTip(500, "导出失败");
        }

        if (exportSucceed) {
            log.info("Order export file success");
            Attachment attachment = new Attachment();
            attachment.setMasterName("STATISTIC_ORDER_EXPORT");
            attachment.setMasterCode(filename);
            attachment.setStatus(GenericState.Valid.code);
            attachment.setFileName("Order-" + DateUtil.getyyMMddHHmmss() + ".xlsx");
            attachment.setAttachmentName(filename);
            attachment.setType("excel");
            attachment.setPath(destFile.getAbsolutePath());
            attachment.setSize(0L);
            attachmentService.insert(attachment);

            result = SUCCESS_TIP;
            result.setMessage(PathUtil.generate(viewPath, String.valueOf(attachment.getId())));
        }
        return result;
    }


    private boolean hasCondition(Map<String, Object> queryParams) {
        Iterator<Map.Entry<String, Object>> entryIterator = queryParams.entrySet().iterator();
        boolean hasCondition = false;
        while(entryIterator.hasNext()) {
            Map.Entry<String, Object> entry = entryIterator.next();
            String key = entry.getKey();
            if ("offset".equals(key) || "limit".equals(key) || "order".equals(key)){
                continue;
            }

            Object value = entry.getValue();
            if (ToolUtil.isEmpty(value)){
                continue;
            }

            hasCondition = true;
        }
        return hasCondition;
    }

    /**
     * 订单管理详情
     */
    @RequestMapping(value = "/class/detail/{orderNo}")
    public Object detail(@PathVariable("orderNo") String orderNo, Model model) {

        if (null == orderNo)
            throw new ServiceException(MessageConstant.MessageCode.SYS_MISSING_ARGUMENTS, new String[]{"订单号"});

        Order order = orderService.get(orderNo);
        if (null == order)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"订单不存在"});

        OrderDetail orderDetail = OrderDetail.me(order).warp();
        model.addAttribute("order", orderDetail);

        Member orderMember = orderService.getMemberInfo(orderNo);
        model.addAttribute("orderMember", orderMember);

        List<OrderItem> orderItemList = orderService.listItems(orderNo, OrderItemTypeEnum.Course);
        List<Map<String, Object>> itemInfoList = new ArrayList<Map<String, Object>>();
        for(OrderItem orderItem : orderItemList){
            Map<String, Object> itemInfo = new HashMap<String, Object>();
            Class classInfo = classService.get(orderItem.getItemObjectCode());
            itemInfo.put("classInfo", classInfo);
            CourseCart courseCart = courseCartService.get(orderItem.getCourseCartCode());
            itemInfo.put("courseCartInfo", courseCart);

            itemInfoList.add(itemInfo);
        }
        model.addAttribute("orderItemList", itemInfoList);

        return PREFIX + "order_detail.html";
    }

    private XSSFWorkbook buildOrderExportWorkbook(List<String> headers, Map<String, Object> result) {
        XSSFWorkbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("订单统计");

        // 合并列，显示统计信息： 总数量、总金额
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 11));
        Row top = sheet.createRow(0);
        top.setHeight((short) (40 * 20));
        Font topFont = workbook.createFont();
        topFont.setFontHeightInPoints((short) 20);
        topFont.setFontName("黑体");
        topFont.setColor(HSSFColor.BLACK.index);
        XSSFCellStyle topStyle = workbook.createCellStyle();
        topStyle.setFont(topFont);
        topStyle.setAlignment(CellStyle.ALIGN_LEFT);
        topStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        topStyle.setIndention((short) 2);
        // 添加统计信息
        String totalCount = String.valueOf(result.get("totalCount"));
        String totalAmount = (String) result.get("totalAmount");

        addCell(top, "", topStyle);
        addCell(top, "订单总数： " + totalCount + "    订单总金额：" + totalAmount + " 元", topStyle);

        Row header = sheet.createRow(1);
        header.setHeight((short) (35 * 20));

        Font headerFont = workbook.createFont();
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setFontName("黑体");
        headerFont.setColor(HSSFColor.BLACK.index);
        XSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        headerStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        // 添加头部
        addCells(header, headers, headerStyle);

        int valueStartRow = 2;
        int valueIndex = 1;

        Font valueFont = workbook.createFont();
        valueFont.setFontHeightInPoints((short) 9);
        valueFont.setFontName("宋体");
        valueFont.setColor(HSSFColor.BLACK.index);
        XSSFCellStyle valueStyle = workbook.createCellStyle();
        valueStyle.setFont(valueFont);
        valueStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        valueStyle.setIndention((short) 1);
        valueStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

        List<Map<String, Object>> resultList = (List<Map<String, Object>>) result.get("dataResult");

        for(Map<String, Object> order : resultList){
            Row valueRow = sheet.createRow(valueStartRow++);

            Double amount = 0.00D;
            try {
                amount = new BigDecimal(String.valueOf(order.get("amount"))).divide(TRANS_UNIT).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            }catch(Exception e){}

            String payChannel = "";
            try {
                payChannel = PayMethodEnum.instanceOf((Integer) order.get("payMethod")).text;
            }catch(Exception e){}

            String acceptDate = "";
            try{
                acceptDate = DateUtil.format((Date)order.get("acceptDate"), "yyyy-MM-dd HH:mm:ss");
            }catch(Exception e){}

            String payDate = "";
            try{
                payDate = DateUtil.format((Date)order.get("payDate"), "yyyy-MM-dd HH:mm:ss");
            }catch(Exception e){}

            addCell(valueRow, valueIndex++, valueStyle);
            addCell(valueRow, order.get("acceptNo"), valueStyle);
            addCell(valueRow, amount, valueStyle);
            addCell(valueRow, order.get("studentName"), valueStyle);
            addCell(valueRow, order.get("className"), valueStyle);
            addCell(valueRow, order.get("teacherName"), valueStyle);
            addCell(valueRow, order.get("userName"), valueStyle);
            addCell(valueRow, "", valueStyle);
            addCell(valueRow, payChannel, valueStyle);
            addCell(valueRow, null == order.get("payResult") ? " " : order.get("payResult"), valueStyle);
            addCell(valueRow, acceptDate, valueStyle);
            addCell(valueRow, payDate, valueStyle);
        }

        //
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i,sheet.getColumnWidth(i)*11/10);
        }

        return workbook;
    }

}
