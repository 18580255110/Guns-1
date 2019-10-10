package com.stylefeng.guns.modular.payMGR;

import com.alibaba.fastjson.JSON;
import com.stylefeng.guns.modular.payMGR.sdk.AcpService;
import org.apache.http.client.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description //TODO
 * @Author 罗华
 * @Date 2019/3/24 14:21
 * @Version 1.0
 */
public class UnionPostRequest extends PostRequest{
    private static final Logger log = LoggerFactory.getLogger(UnionPostRequest.class);

    private Map<String, String> postData = new HashMap<String, String>();

    public UnionPostRequest(String seq) {
        super(seq);
    }

    @Override
    public void post(ResponseHandler<String> callback) {
        Map<String, String> rspData = AcpService.post(this.postData, this.url, "UTF-8");  //发送请求报文并接受同步应答（默认连接超时时间30秒，读取返回结果超时时间30秒）;这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过
        super.post(callback);
    }

    @Override
    public Map<String, String> post() {

        log.info("Union pay request ===> {}", JSON.toJSONString(this.postData));

        Map<String, String> rspData = AcpService.post(this.postData, this.url, "UTF-8");  //发送请求报文并接受同步应答（默认连接超时时间30秒，读取返回结果超时时间30秒）;这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过

        log.info("Union pay response ===> {}", JSON.toJSONString(rspData));

        Map<String, String> postResult = new HashMap<>();
        if(!rspData.isEmpty()){
            if(AcpService.validate(rspData, "UTF-8")){
                log.info("验证签名成功");
                String respCode = rspData.get("respCode") ;
                if(("00").equals(respCode)){
                    //成功,获取tn号
                    postResult.put("code", "SUCCESS");
                    postResult.put("result", rspData.get("tn"));
                }else{
                    //其他应答码为失败请排查原因或做失败处理
                    postResult.put("code", "FAILED");
                    postResult.put("result", rspData.get("respMsg"));
                }
            }else{
                log.error("验证签名失败");
                postResult.put("code", "FAILED");
                postResult.put("result", "收到无效的平台响应");
            }
        }else{

            //未返回正确的http状态
            log.error("未获取到返回报文或返回http状态码非200");
            postResult.put("code", "FAILED");
            postResult.put("result", "未获取到平台响应");
        }
        return postResult;
    }

    public void pushPostData(Map<String, String> signedPostData) {
        this.postData.putAll(signedPostData);
    }
}
