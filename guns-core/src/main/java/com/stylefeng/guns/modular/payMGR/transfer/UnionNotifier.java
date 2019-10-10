package com.stylefeng.guns.modular.payMGR.transfer;

import com.stylefeng.guns.modular.payMGR.sdk.AcpService;
import com.stylefeng.guns.modular.payMGR.service.impl.PayServiceImpl;
import com.stylefeng.guns.modular.system.model.PayMethodEnum;
import org.apache.xmlbeans.impl.xb.xsdschema.UnionDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/1/30 16:36
 * @Version 1.0
 */
public class UnionNotifier extends PayNotifier{
    private static final Logger log = LoggerFactory.getLogger(UnionNotifier.class);

    private boolean paySuccess;

    private String orderNo;

    private String resultMessage;

    UnionNotifier(){
        channel = PayMethodEnum.unionpay;
    }

    public boolean isPaySuccess() {
        return paySuccess;
    }

    public void setPaySuccess(boolean paySuccess) {
        this.paySuccess = paySuccess;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public static UnionNotifier parse(Map<String, String> reqParam, String encoding) {
        UnionNotifier notifier = null;
        //重要！验证签名前不要修改reqParam中的键值对的内容，否则会验签不过
        if (!AcpService.validate(reqParam, encoding)) {
            log.error("验证签名结果[失败].");
            //验签失败，需解决验签问题
        } else {
            log.info("验证签名结果[成功].");
            //【注：为了安全验签成功才应该写商户的成功处理逻辑】交易成功，更新商户订单状态
            notifier = new UnionNotifier();

            String orderId =reqParam.get("orderId"); //获取后台通知的数据，其他字段也可用类似方式获取
            String respCode = reqParam.get("respCode");
            String respMsg = reqParam.get("respMsg");
            //判断respCode=00、A6后，对涉及资金类的交易，请再发起查询接口查询，确定交易成功后更新数据库。
            if ("00".equals(respCode)){
                notifier.setPaySuccess(true);
                notifier.setOrderNo(orderId);
            }else if ("A6".equals(respCode)){
                // de
                notifier.setPaySuccess(true);
                notifier.setOrderNo(orderId);
            }else{
                notifier.setPaySuccess(false);
                notifier.setResultMessage(respMsg);
            }
            log.info("Pay order id = {}", orderId);
            log.info("Pay order response = {}", respCode);
        }

        return notifier;
    }

    @Override
    public boolean paySuccess() {
        return this.paySuccess;
    }

    @Override
    public String getOrder() {
        return this.orderNo;
    }

    @Override
    public String getMessage() {
        return this.resultMessage;
    }
}
