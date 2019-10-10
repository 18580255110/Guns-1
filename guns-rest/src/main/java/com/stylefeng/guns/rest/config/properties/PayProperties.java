package com.stylefeng.guns.rest.config.properties;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/1/19 14:34
 * @Version 1.0
 */
@Configuration
@ConfigurationProperties(prefix = PayProperties.AUTH_PREFIX)
public class PayProperties {
    public static final String AUTH_PREFIX = "application.pay";

    private WeixinProperties weixin;

    private UnionProperties union;

    public WeixinProperties getWeixin() {
        return weixin;
    }

    public void setWeixin(WeixinProperties weixin) {
        this.weixin = weixin;
    }

    public UnionProperties getUnion() {
        return union;
    }

    public void setUnion(UnionProperties union) {
        this.union = union;
    }

    public Properties getWeixinProperties() {

        Properties properties = new Properties();

        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(WeixinProperties.class);
        for(PropertyDescriptor propertyDescriptor : propertyDescriptors){
            if (!String.class.equals(propertyDescriptor.getPropertyType())){
                continue;
            }
            String key = propertyDescriptor.getName();
            String value = null;
            try {
                value = (String)propertyDescriptor.getReadMethod().invoke(this.weixin, new Object[0]);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            if (null != value)
                properties.setProperty(key, value);
        }
        return properties;
    }

    public Properties getUnionProperties() {

        Properties properties = new Properties();

        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(UnionProperties.class);
        for(PropertyDescriptor propertyDescriptor : propertyDescriptors){
            if (!String.class.equals(propertyDescriptor.getPropertyType())){
                continue;
            }

            String key = propertyDescriptor.getName();
            String value = null;
            try {
                value = (String)propertyDescriptor.getReadMethod().invoke(this.union, new Object[0]);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            if (null != value)
                properties.setProperty(key, value);
        }
        return properties;
    }

    public static class UnionProperties {
        private String mchId;
        private String mchName;

        private String appId;
        private String appName;

        private String orderUrl;
        private String notifyUrl;

        public String getMchId() {
            return mchId;
        }

        public void setMchId(String mchId) {
            this.mchId = mchId;
        }

        public String getMchName() {
            return mchName;
        }

        public void setMchName(String mchName) {
            this.mchName = mchName;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public String getOrderUrl() {
            return orderUrl;
        }

        public void setOrderUrl(String orderUrl) {
            this.orderUrl = orderUrl;
        }

        public String getNotifyUrl() {
            return notifyUrl;
        }

        public void setNotifyUrl(String notifyUrl) {
            this.notifyUrl = notifyUrl;
        }
    }

    public static class WeixinProperties  {
        private String mchId;
        private String mchName;

        private String appId;
        private String appName;
        private String appSecret;

        private String deviceId;

        private String orderUrl;
        private String notifyUrl;

        public String getMchId() {
            return mchId;
        }

        public void setMchId(String mchId) {
            this.mchId = mchId;
        }

        public String getMchName() {
            return mchName;
        }

        public void setMchName(String mchName) {
            this.mchName = mchName;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
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

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getOrderUrl() {
            return orderUrl;
        }

        public void setOrderUrl(String orderUrl) {
            this.orderUrl = orderUrl;
        }

        public String getNotifyUrl() {
            return notifyUrl;
        }

        public void setNotifyUrl(String notifyUrl) {
            this.notifyUrl = notifyUrl;
        }
    }
}
