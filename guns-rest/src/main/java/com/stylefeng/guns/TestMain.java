package com.stylefeng.guns;

import com.alibaba.fastjson.JSON;
import com.stylefeng.guns.modular.payMGR.sdk.AcpService;
import com.stylefeng.guns.modular.payMGR.sdk.SDKConfig;
import com.stylefeng.guns.util.DateUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TestMain{

    public static void main(String[] args){
//        String result = new Sha256Hash("abcd1234").toHex().toUpperCase();
//        System.out.println(result);

//        int price = 20;
//        int schedule = 2;
//        int remain = 1;
//        BigDecimal perPrice = new BigDecimal(String.valueOf(price)).divide(new BigDecimal(schedule), 10, RoundingMode.HALF_UP);
//        BigDecimal remainPrice = new BigDecimal(remain).multiply(perPrice);
//        BigDecimal signPrice = remainPrice.setScale(0, RoundingMode.HALF_UP);
//
//        System.out.println(signPrice);

        SDKConfig config = SDKConfig.getConfig();
        config.loadPropertiesFromPath(null);

        String merId = "802500082990505";
//        String merId = "777290058165896";
        String txnAmt = "1";
        String orderId = "0eMZ1910101645174512";
        String txnTime = DateUtil.format(new Date(), "yyyyMMddHHmmss");

        Map<String, String> contentData = new HashMap<String, String>();

        /***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
        contentData.put("version", "5.1.0");            //版本号 全渠道默认值
        contentData.put("encoding", "UTF-8");     //字符集编码 可以使用UTF-8,GBK两种方式
        contentData.put("signMethod", "01"); //签名方法
        contentData.put("txnType", "01");              		 	//交易类型 01:消费
        contentData.put("txnSubType", "01");           		 	//交易子类 01：消费
        contentData.put("bizType", "000201");          		 	//填写000201
        contentData.put("channelType", "08");          		 	//渠道类型 08手机

        /***商户接入参数***/
        contentData.put("merId", merId);   		 				//商户号码，请改成自己申请的商户号或者open上注册得来的777商户号测试
        contentData.put("accessType", "0");            		 	//接入类型，商户接入填0 ，不需修改（0：直连商户， 1： 收单机构 2：平台商户）
        contentData.put("orderId", orderId);        	 	    //商户订单号，8-40位数字字母，不能含“-”或“_”，可以自行定制规则
        contentData.put("txnTime", txnTime);		 		    //订单发送时间，取系统时间，格式为yyyyMMddHHmmss，必须取当前时间，否则会报txnTime无效
        contentData.put("accType", "01");					 	//账号类型 01：银行卡02：存折03：IC卡帐号类型(卡介质)
        contentData.put("txnAmt", txnAmt);						//交易金额 单位为分，不能带小数点
        contentData.put("currencyCode", "156");                 //境内商户固定 156 人民币

        contentData.put("backUrl", "http://app.kecui.com.cn/pay/union/notify");

        /**对请求参数进行签名并发送http post请求，接收同步应答报文**/
        Map<String, String> reqData = AcpService.sign(contentData, "UTF-8");			 //报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。
//        String requestAppUrl = "https://gateway.test.95516.com/gateway/api/appTransReq.do";							 //交易请求url从配置文件读取对应属性文件acp_sdk.properties中的 acpsdk.backTransUrl
        String requestAppUrl = "https://gateway.95516.com/gateway/api/appTransReq.do";							 //交易请求url从配置文件读取对应属性文件acp_sdk.properties中的 acpsdk.backTransUrl
        Map<String, String> rspData = AcpService.post(reqData,requestAppUrl,"UTF-8");  //发送请求报文并接受同步应答（默认连接超时时间30秒，读取返回结果超时时间30秒）;这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过

        /**对应答码的处理，请根据您的业务逻辑来编写程序,以下应答码处理逻辑仅供参考------------->**/
        //应答码规范参考open.unionpay.com帮助中心 下载  产品接口规范  《平台接入接口规范-第5部分-附录》

        System.out.println("Response ==> " + JSON.toJSONString(rspData));
    }
}