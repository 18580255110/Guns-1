package com.stylefeng.guns.modular.payMGR;

import com.alibaba.fastjson.JSON;
import com.stylefeng.guns.modular.system.model.Order;
import com.stylefeng.guns.util.DateUtil;
import com.stylefeng.guns.util.MD5Util;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/1/3 00:51
 * @Version 1.0
 */
public class WxPayRequestBuilder extends PayRequestBuilder {
    private static final Logger log = LoggerFactory.getLogger(WxPayRequestBuilder.class);

    /**
     * 商户号
     */
    private String mchId;

    /**
     * 应用号
     */
    private String appId;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 密钥
     */
    private String appSecret;

    /**
     * 设备号
     */
    private String deviceId;

    /**
     * 支付下单服务器IP
     */
    private String createHost;

    /**
     * 通知URL
     */
    private String notifyUrl;

    public WxPayRequestBuilder(Properties weixinProperties) {
        Field[] fields = WxPayRequestBuilder.class.getDeclaredFields();

        for(Field field : fields){
            String fieldName = field.getName();

            if (weixinProperties.containsKey(fieldName)){
                field.setAccessible(true);
                try {
                    field.set(this, weixinProperties.getProperty(fieldName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        fields = PayRequestBuilder.class.getDeclaredFields();
        for(Field field : fields){
            String fieldName = field.getName();

            if (weixinProperties.containsKey(fieldName)){
                char[] cs = fieldName.toCharArray();
                cs[0] -= 32;
                String setMethod = "set" + String.valueOf(cs);
                try {
                    Method method = this.getClass().getMethod(setMethod, String.class);
                    method.invoke(this, weixinProperties.getProperty(fieldName));
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

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getCreateHost() {
        return createHost;
    }

    public void setCreateHost(String createHost) {
        this.createHost = createHost;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    @Override
    public PostRequest order(Order merchantOrder) {
        String nonce = randomCode();
        WxPostRequest postRequest = new WxPostRequest(nonce);

        Date now = new Date();

        Map<String, Object> postData = new HashMap<String, Object>();
        postData.put("appid", this.appId);
        postData.put("mch_id", this.mchId);
        postData.put("device_info", this.deviceId);
        postData.put("nonce_str", nonce);
        postData.put("body", appName + "-" + merchantOrder.getDesc());
        postData.put("detail", merchantOrder.getAcceptNo());
        postData.put("out_trade_no", merchantOrder.getAcceptNo());
        postData.put("fee_type", "CNY");
        postData.put("total_fee", merchantOrder.getAmount());
        postData.put("spbill_create_ip", createHost);
        postData.put("time_start", DateUtil.format(now, "yyyyMMddHHmmss"));
        postData.put("time_expire", DateUtil.format(DateUtil.add(now, Calendar.MINUTE, 110), "yyyyMMddHHmmss"));
        postData.put("notify_url", notifyUrl);
        postData.put("sign_type", "MD5");
        postData.put("trade_type", "APP");

        postData.put("sign", signPost(appSecret, postData));

        XStream xStream = new XStream(new StaxDriver(new NoNameCoder()));
        xStream.alias("xml", Map.class);
        xStream.registerConverter(new MapEntryConvert());

        postRequest.setDatagram(StringEscapeUtils.unescapeXml(xStream.toXML(postData)));
        postRequest.setUrl(getOrderUrl());
        return postRequest;
    }

    private String signPost(String appSecret, Map<String, Object> postData) {

        Set<String> postKeySet = new TreeSet<String>();

        log.info("Payment post data = " + JSON.toJSONString(postData));
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

        stringSignBuilder.append("&key=").append(appSecret);
        String sign = MD5Util.encrypt(stringSignBuilder.toString()).toUpperCase();
        log.debug("sign ===> {}", sign);
        return sign;
    }

    public static void main(String[] args){
        Date now = new Date();
        Map<String, Object> postData = new HashMap<String, Object>();
        postData.put("appid", "dfewrwerewrwe");
        postData.put("mch_id", "fdfweeeeeeeeeee");
        postData.put("device_info", "eeeeeeeeeeee");
        postData.put("nonce_str", "nonce_str");
        postData.put("body", "");
        postData.put("detail", "");
        postData.put("out_trade_no", "merchant_order_no");
        postData.put("fee_type", "CNY");
        postData.put("total_fee", 200);
        postData.put("spbill_create_ip", "39.98.48.194");
        postData.put("time_start", DateUtil.format(now, "yyyyMMddHHmmss"));
        postData.put("time_expire", DateUtil.format(DateUtil.add(now, Calendar.HOUR, 1), "yyyyMMddHHmmss"));
        postData.put("notify_url", "");
        postData.put("sign_type", "MD5");

        XStream xStream = new XStream();
        xStream.alias("xml", Map.class);
        xStream.registerConverter(new MapEntryConvert());

        System.out.println(xStream.toXML(postData));

        //WxPayRequestBuilder builder = new WxPayRequestBuilder();
        //System.out.println("Sing = " + builder.signPost(postData));


    }
}
