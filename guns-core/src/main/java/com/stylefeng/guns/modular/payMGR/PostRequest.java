package com.stylefeng.guns.modular.payMGR;

import com.stylefeng.guns.modular.adjustMGR.service.impl.AdjustStudentServiceImpl;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/1/8 01:25
 * @Version 1.0
 */
public abstract class PostRequest {

    private static final Logger log = LoggerFactory.getLogger(PostRequest.class);

    protected String url;
    /**
     * 随机字符串
     */
    private String nonceStr;
    /**
     * 请求报文
     */
    protected String datagram;
    /**
     * 当前业务
     */
    private String service;

    public PostRequest(String seq){
        this.nonceStr = seq;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDatagram(String datagram) {
        this.datagram = datagram;
    }

    public void post(ResponseHandler<String> callback){
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(this.url);
        StringEntity postData = new StringEntity(this.datagram, "UTF-8");
        log.debug("Send data ==> {}", this.datagram );
        post.setEntity(postData);
        try {
            httpclient.execute(post, callback);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String toString() {
        return this.nonceStr;
    }

    @Override
    public boolean equals(Object obj) {
        return this.nonceStr.equals(obj.toString());
    }

    public abstract Map<String, String> post();
}
