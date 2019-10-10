package com.stylefeng.guns.modular.payMGR;

import com.alibaba.fastjson.JSON;
import com.stylefeng.guns.modular.payMGR.service.impl.PayServiceImpl;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
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
import java.util.HashMap;
import java.util.Map;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/1/8 01:26
 * @Version 1.0
 */
public class WxPostRequest extends PostRequest {
    private static final Logger log = LoggerFactory.getLogger(WxPostRequest.class);

    public WxPostRequest(String seq) {
        super(seq);
    }

    @Override
    public Map<String, String> post() {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(this.url);
        StringEntity postData = new StringEntity(this.datagram, "UTF-8");
        log.debug("Send data ==> {}", this.datagram );
        post.setEntity(postData);

        Map<String, String> postResult = new HashMap<>();
        try {
            httpclient.execute(post, new ResponseHandler<String>() {

                @Override
                public String handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                    XStream xStream = new XStream(new StaxDriver());
                    xStream.alias("xml", Map.class);
                    xStream.registerConverter(new MapEntryConvert());
                    Map<String, String> response = (Map<String, String>) xStream.fromXML(httpResponse.getEntity().getContent());

                    log.debug("Response ===>  {}" , JSON.toJSONString(response));

                    if ("SUCCESS".equals(response.get("return_code"))){
                        if ("SUCCESS".equals(response.get("result_code"))){
                            postResult.put("code", "SUCCESS");
                            postResult.put("result", response.get("prepay_id"));
                        }else{
                            postResult.put("code", "FAILED");
                            postResult.put("result", postResult.get("err_code_des"));
                        }
                    }else{
                        postResult.put("code", "FAILED");
                        postResult.put("result", postResult.get("return_msg"));
                    }
                    return null;
                }
            });
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            postResult.put("code", "FAILED");
            postResult.put("result", "支付失败");
        }

        return postResult;
    }
}
