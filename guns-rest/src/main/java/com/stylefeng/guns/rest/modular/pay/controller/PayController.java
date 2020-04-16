package com.stylefeng.guns.rest.modular.pay.controller;

import com.alibaba.fastjson.JSON;
import com.stylefeng.guns.common.exception.ServiceException;
import com.stylefeng.guns.core.message.MessageConstant;
import com.stylefeng.guns.modular.orderMGR.service.IOrderService;
import com.stylefeng.guns.modular.payMGR.service.IPayService;
import com.stylefeng.guns.modular.payMGR.transfer.UnionNotifier;
import com.stylefeng.guns.modular.payMGR.transfer.WeixinNotifier;
import com.stylefeng.guns.modular.system.model.Order;
import com.stylefeng.guns.modular.system.model.OrderStateEnum;
import com.stylefeng.guns.modular.system.model.PayStateEnum;
import com.stylefeng.guns.rest.core.ApiController;
import com.stylefeng.guns.rest.core.Responser;
import com.stylefeng.guns.rest.modular.order.responser.PayOrderResponser;
import com.stylefeng.guns.rest.modular.pay.requester.PayOrderRequester;
import com.stylefeng.guns.rest.modular.pay.responser.RandomResponser;
import com.stylefeng.guns.rest.modular.pay.responser.SignResponser;
import com.stylefeng.guns.util.MD5Util;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by 罗华.
 */
@RestController
@RequestMapping("/pay")
@Api(tags = "支付接口")
public class PayController extends ApiController {
    private static final Logger log = LoggerFactory.getLogger(PayController.class);

    /**
     * 返回成功xml
     */
    private String resSuccessXml = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";

    /**
     * 返回失败xml
     */
    private String resFailXml = "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[报文处理失败]]></return_msg></xml>";

    @Autowired
    private IPayService payService;
    @Autowired
    private IOrderService orderService;

    @Value("${application.pay.weixin.app-secret:''}")
    private String weixinSecret;

    @Value("${application.pay.union.app-secret:''}")
    private String unionSecret;

    @ApiOperation(value = "支付下单", httpMethod = "POST", response = PayOrderResponser.class)
    @RequestMapping(value = "/preorder", method = RequestMethod.POST)
    public Responser unionOrder(
            @Valid
            @RequestBody
            @ApiParam(required = true, value = "支付下单请求信息")
            PayOrderRequester requester
    ) {
        Order order = orderService.get(requester.getOrder());
        if (null == order)
            throw new ServiceException(MessageConstant.MessageCode.SYS_SUBJECT_NOT_FOUND, new String[]{"订单"});

        if ( OrderStateEnum.Valid.code != order.getStatus())
            throw new ServiceException(MessageConstant.MessageCode.SYS_TEMPLATE_MESSAGE, new String[]{"订单已失效， 请重新下单"});

        if (PayStateEnum.Paying.code == order.getPayStatus() )
            throw new ServiceException(MessageConstant.MessageCode.PAY_ORDER_ONAIR, new String[0]);

        if ( PayStateEnum.PayOk.code == order.getPayStatus() )
            throw new ServiceException(MessageConstant.MessageCode.PAY_ORDER_FINISH, new String[0]);

        if ( PayStateEnum.Expire.code == order.getPayStatus() )
            throw new ServiceException(MessageConstant.MessageCode.PAY_ORDER_EXPIRED, new String[0]);

        if (null != requester.getChannel())
            order.setPayMethod(requester.getChannel());

        String paySequence = payService.createPayOrder(order);
        order.setOutSequence(paySequence);
        orderService.updateById(order);

        return PayOrderResponser.me(paySequence);
    }

    @ApiOperation(value = "微信支付签名", httpMethod = "POST", response = SignResponser.class)
    @RequestMapping(value = "/weixin/sign", method = RequestMethod.POST)
    public Responser weixinSign(@RequestBody Map<String, Object> requestParams) {
        String signCode = sign(weixinSecret, requestParams);
        return SignResponser.me("MD5", signCode);
    }


    @ApiOperation(value = "银联支付签名", httpMethod = "POST", response = SignResponser.class)
    @RequestMapping(value = "/union/sign", method = RequestMethod.POST)
    public Responser unionSign(@RequestBody Map<String, Object> requestParams) {
        String signCode = sign(unionSecret, requestParams);
        return SignResponser.me("MD5", signCode);
    }

    @ApiOperation(value = "微信支付随机数", httpMethod = "POST", response = RandomResponser.class)
    @RequestMapping(value = "/weixin/random", method = RequestMethod.POST)
    public Responser weixinRandom() {
        String uuid = UUID.randomUUID().toString();
        String random = MD5Util.encrypt(uuid).toUpperCase();
        return RandomResponser.me(random);
    }

    @RequestMapping(value = "/weixin/notify", method = RequestMethod.POST)
    public void weixinPayNotifyHandler(HttpServletRequest request, HttpServletResponse response) {

        String resXml = resFailXml;
        try {
            WeixinNotifier notidier = WeixinNotifier.parse(request.getInputStream());

            payService.notify(notidier);

            log.info("Success parse notify xml ===> {}", JSON.toJSONString(notidier));

            resXml = resSuccessXml;
        } catch (Exception e) {
            log.warn("支付通知 处理失败： {}", e.getMessage(), e);
        } finally {
            try {
                // 处理业务完毕
                BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
                out.write(resXml.getBytes());
                out.flush();
                out.close();
            } catch (IOException e) {
            }
        }
        return;
    }

    @RequestMapping(value = "/union/notify", method = RequestMethod.POST)
    public void unionPayNotifyHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("Union pay notify handle finish!");
        String encoding = request.getParameter("encoding");
        log.info("message encoding = {}", encoding);
        // 获取银联通知服务器发送的后台通知参数
        Map<String, String> reqParam = getAllRequestParam(request);

        try {
            UnionNotifier unionNotifier = UnionNotifier.parse(reqParam, encoding);
            payService.notify(unionNotifier);
        }catch(Exception e){
            log.warn("支付通知 处理失败： {}", e.getMessage(), e);
        }finally {
            log.info("Union pay notify handle finish!");
            try {
                // 处理业务完毕
                BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
                out.write("ok".getBytes());
                out.flush();
                out.close();
            } catch (IOException e) {
            }
        }


    }


    private String sign(String signKey, Map<String, Object> postData) {
        Set<String> postKeySet = new TreeSet<String>();
        Iterator<String> postKeyIter = postData.keySet().iterator();
        while (postKeyIter.hasNext()) {
            postKeySet.add(postKeyIter.next());
        }

        Iterator<String> sortedKeyIter = postKeySet.iterator();
        StringBuilder stringSignBuilder = new StringBuilder();
        while (sortedKeyIter.hasNext()) {
            String key = sortedKeyIter.next();
            Object value = postData.get(key);
            if (null == value)
                continue;

            if (stringSignBuilder.length() > 0)
                stringSignBuilder.append("&");

            stringSignBuilder.append(key + "=" + value);
        }

        stringSignBuilder.append("&key=").append(signKey);
        String sign = MD5Util.encrypt(stringSignBuilder.toString()).toUpperCase();
        log.debug("sign ===> {}", sign);
        return sign;
    }

    /**
     * 获取请求参数中所有的信息
     *
     * @param request
     * @return
     */
    public static Map<String, String> getAllRequestParam(final HttpServletRequest request) {
        Map<String, String> res = new HashMap<String, String>();
        Enumeration<?> temp = request.getParameterNames();
        if (null != temp) {
            while (temp.hasMoreElements()) {
                String en = (String) temp.nextElement();
                String value = request.getParameter(en);
                res.put(en, value);
                //在报文上送时，如果字段的值为空，则不上送<下面的处理为在获取所有参数数据时，判断若值为空，则删除这个字段>
                //System.out.println("ServletUtil类247行  temp数据的键=="+en+"     值==="+value);
                if (null == res.get(en) || "".equals(res.get(en))) {
                    res.remove(en);
                }
            }
        }
        return res;
    }
}
