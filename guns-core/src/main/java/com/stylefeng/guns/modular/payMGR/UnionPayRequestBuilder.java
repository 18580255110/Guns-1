package com.stylefeng.guns.modular.payMGR;

import com.stylefeng.guns.modular.payMGR.sdk.AcpService;
import com.stylefeng.guns.modular.payMGR.sdk.SDKUtil;
import com.stylefeng.guns.modular.system.model.Order;
import com.stylefeng.guns.util.DateUtil;
import com.stylefeng.guns.util.MD5Util;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.apache.commons.lang.StringEscapeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 银联支付
 *
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/1/3 00:51
 * @Version 1.0
 */
public class UnionPayRequestBuilder extends PayRequestBuilder {
    /**
     * 版本号
     */
    private String version = "5.1.0";
    /**
     * 编码方式
     */
    private String encoding = "UTF-8";
    /**
     * 交易类型
     *
     * 01 消费
     *
     */
    private String txnType = "01";
    /**
     * 产品类型
     * 000201  B2C 网关支付
     */
    private String bizType = "000201";
    /**
     * 渠道类型
     * 05 语音
     * 07 互联网
     * 08 移动
     */
    private String channelType = "08";
    /**
     * 交易子类型
     * 01 自助交易
     * 03 分期交易
     */
    private String txnSubType = "01";
    /**
     * 签名方法
     * 证书方式固定01
     */
    private String signMethod = "01";
    /**
     * 接入类型
     * 0 直连商户
     * 1 收单机构
     * 2 平台商户
     */
    private String accessType = "0";
    /**
     * 账号类型
     * 01 银行卡
     * 02 存折
     * 03 IC卡账号类型(卡介质)
     */
    private String accType = "01";
    /**
     * 超时时间， 单位： 秒
     */
    private Integer payTimeout = 3600;
    /**
     * 交易币种
     * 156 人民币
     */
    private String currencyCode = "156";
    /**
     * 证书ID
     */
    private String certId;
    /**
     * 商户号
     */
    private String merId;
    /**
     * 账户号
     */
    private String accNo;
    /**
     * 通知URL
     */
    private String notifyUrl;

    public String getCertId() {
        return certId;
    }

    public void setCertId(String certId) {
        this.certId = certId;
    }

    public String getMerId() {
        return merId;
    }

    public void setMerId(String merId) {
        this.merId = merId;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public UnionPayRequestBuilder(Properties unionProperties){
        Field[] fields = UnionPayRequestBuilder.class.getDeclaredFields();

        for(Field field : fields){
            String fieldName = field.getName();

            if (unionProperties.containsKey(fieldName)){
                field.setAccessible(true);
                try {
                    field.set(this, unionProperties.getProperty(fieldName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        fields = PayRequestBuilder.class.getDeclaredFields();
        for(Field field : fields){
            String fieldName = field.getName();

            if (unionProperties.containsKey(fieldName)){
                char[] cs = fieldName.toCharArray();
                cs[0] -= 32;
                String setMethod = "set" + String.valueOf(cs);
                try {
                    Method method = this.getClass().getMethod(setMethod, String.class);
                    method.invoke(this, unionProperties.getProperty(fieldName));
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public PostRequest order(Order merchantOrder) {

        UnionPostRequest postRequest = new UnionPostRequest("1");

        Date now = new Date();

        Map<String, String> contentData = new HashMap<String, String>();
        contentData.put("version", this.version);
        contentData.put("encoding", this.encoding);
        contentData.put("signMethod", this.signMethod);
        contentData.put("txnType", this.txnType);
        contentData.put("txnSubType", this.txnSubType);
        contentData.put("bizType", this.bizType);
        contentData.put("channelType", channelType);

        contentData.put("merId", this.merId); // 在application.yml 中 application.pay.unionpay中配置
        contentData.put("accessType", this.accessType);
        contentData.put("orderId", merchantOrder.getAcceptNo());
        contentData.put("txnTime", DateUtil.format(now, "yyyyMMddHHmmss"));
        contentData.put("accType", this.accType);
        contentData.put("txnAmt", String.valueOf(merchantOrder.getAmount()));
        contentData.put("currencyCode", this.currencyCode); // 人民币
        contentData.put("orderDesc", merchantOrder.getDesc());

        contentData.put("backUrl", this.notifyUrl);

        postRequest.pushPostData(AcpService.sign(contentData, this.encoding));
        postRequest.setUrl(getOrderUrl());
        return postRequest;
    }


    private String signPost(Map<String, Object> postData) {

        Set<String> postKeySet = new TreeSet<String>();
        Iterator<String> postKeyIter = postData.keySet().iterator();
        while(postKeyIter.hasNext()){
            postKeySet.add(postKeyIter.next());
        }

        Iterator<String> sortedKeyIter = postKeySet.iterator();
        StringBuilder stringSignBuilder = new StringBuilder();
        while(sortedKeyIter.hasNext()){
            String key = sortedKeyIter.next();
            Object value = postData.get(key);
            if (null == value)
                continue;

            if (stringSignBuilder.length() > 0)
                stringSignBuilder.append("&");

            stringSignBuilder.append(key + "=" + value);
        }

//        stringSignBuilder.append("&key=").append(appSecret);
        String sign = MD5Util.encrypt(stringSignBuilder.toString()).toUpperCase();
//        log.debug("sign ===> {}", sign);
        return sign;
    }
}
