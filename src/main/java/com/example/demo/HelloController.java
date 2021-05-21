package com.example.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;

@RestController
public class HelloController {
    public static final String AM_APPID = "wx03a9dda77728f573";//appid
    public static final String AM_MCH_ID = "1503161281";//微信支付商户号
    public static final String AM_MCH_KEY = "c562e09e57b099f00e576304c071a7cd";//商户支付key
    public static final String AM_Secret = "c562e09e57b099f00e576304c071a7cd";//AM_Secret 微信公众号处可以看到

    @Autowired
    OrderMapper orderMapper;

    @RequestMapping("/")
    public ModelAndView hello(){
        return new ModelAndView("index");
    }

    @RequestMapping("/wechat/pay")
    public ReturnData pay(@RequestParam Map<String, Object> params, HttpServletRequest request) throws Exception {
        try {
            //获取code
            String code = params.get("code").toString();//获取code
            //自动生成orderId
            String orderId = "WeChat" + UUID.randomUUID().toString().replace("-","").substring(0, 26);

            Map params1 = new HashMap();
            params1.put("secret", AM_Secret);
            params1.put("appid", AM_APPID);
            params1.put("grant_type", "authorization_code");
            params1.put("code", code);

            //  通过get请求向微信发送请求，获取用户的openId
            String result = HttpGetUtil.httpRequestToString("https://api.weixin.qq.com/sns/oauth2/access_token", params1);
            com.alibaba.fastjson.JSONObject jsonObject = (com.alibaba.fastjson.JSONObject) JSON.parse(result);

            //  获得openid
            String openid = jsonObject.get("openid").toString();

            BigDecimal byOid = orderMapper.selectSumsByOid(124L);
            int price = byOid.multiply(new BigDecimal(100)).intValue();
            WeChatConfig config = new WeChatConfig(AM_APPID, AM_MCH_ID, AM_MCH_KEY, "");

            Map<String, String> data = new HashMap();
            data.put("body", "订单号："+orderId);//支付时候显示的名称
            data.put("out_trade_no", orderId);//数据库内的订单号
            data.put("device_info", "WEB");
            data.put("fee_type", "CNY");//货币种类

            String total_fee = Integer.toString(price);//金额

            data.put("total_fee", total_fee);//单位为分  只能为整数
            data.put("spbill_create_ip", request.getRemoteHost());
            data.put("trade_type", "JSAPI");
            data.put("openid", openid);//openid

            Order status = orderMapper.selectStatusById(124L);
            if((status.getIsBackpay()==null || status.getIsBackpay()==0) && status.getPayStatus()==0 && status.getOrderStatus()==1) {
                data.put("notify_url", "https://cs.fzsir.com/test/wechat/payBack");//支付完成后回调地址 接口
            } else {
                System.out.println("订单状态错误");
                ReturnData returnData = ReturnData.ok();
                returnData.put("code", 200);
                returnData.put("msg", "订单失效，不可支付");
                returnData.put("type", true);
                System.out.println(returnData.get("msg"));
                return returnData;
            }


            WXPay wxpay = new WXPay(config);
            Map<String, String> resp = wxpay.unifiedOrder(data);
            System.out.println("resp================>"+ resp.get("result_code") + "+++" +resp.get("return_msg"));
            if ("SUCCESS".equals(resp.get("result_code")) && "OK".equals(resp.get("return_msg"))) {
                String timestamp = System.currentTimeMillis() / 1000L + "";
                SortedMap<String, String> finalpackage = new TreeMap<String, String>();
                String packages = "prepay_id=" + resp.get("prepay_id");
                finalpackage.put("appId", resp.get("appid"));
                finalpackage.put("timeStamp", timestamp);
                finalpackage.put("nonceStr", resp.get("nonce_str"));
                finalpackage.put("package", packages);
                finalpackage.put("signType", "MD5");
                String signature = WXPayUtil.generateSignature(finalpackage, config.getKey());
                finalpackage.put("paySign", signature);

                ReturnData returnData = ReturnData.ok();
                returnData.put("code", 200);
                returnData.put("data", finalpackage);
                System.out.println(returnData.get("data"));
                return returnData;

            } else {
                System.out.println("进入失败");
                ReturnData returnData = ReturnData.ok();
                returnData.put("code", 200);
                returnData.put("msg", "支付失败");
                returnData.put("type", true);
                System.out.println(returnData.get("msg"));
                return returnData;
            }
        } catch (Exception e) {
            System.out.println("进入异常");
            e.printStackTrace();
            ReturnData returnData = ReturnData.error();
            returnData.put("type", false);
            returnData.put("msg", "服务器错误，请稍后重试");
            returnData.put("errorCode", "000");
            return returnData;
        }
    }


    /**
     * 回调
     */
    @RequestMapping("/wechat/payBack")
    public String payBack(HttpServletRequest request) {
        System.out.println("进入payBack");
        //System.out.println("微信支付成功,微信发送的callback信息,请注意修改订单信息");
        try {
            StringBuilder notifyData = new StringBuilder(); // 支付结果通知的xml格式数据
            String inputLine;
            while ((inputLine = request.getReader().readLine()) != null) {
                notifyData.append(inputLine);
            }
            request.getReader().close();

            Map<String, String> notifyMap = WXPayUtil.xmlToMap(notifyData.toString());  // 转换成map
            final String out_trade_no = notifyMap.get("out_trade_no");
            final String transaction_id = notifyMap.get("transaction_id");

            System.out.println("out_trade_no====================>"+out_trade_no);
            System.out.println("transaction_id===========================>"+transaction_id);


            WeChatConfig config = new WeChatConfig(AM_APPID, AM_MCH_ID, AM_MCH_KEY, "");
            WXPay wxpay = new WXPay(config);
            String resXml;
            if (wxpay.isPayResultNotifySignatureValid(notifyMap)) {
                Order order = new Order();
                order.setId(124L);
                order.setOrderId(out_trade_no);
                order.setPayStatus(1);
                order.setTradeStatus(2);
                order.setPayWay(1);
                order.setCreateTime(new Date());
                orderMapper.updateById(order);
                // 注意特殊情况：订单已经退款，但收到了支付结果成功的通知，不应把商户侧订单状态从退款改成支付成功
                resXml = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml> ";

            } else {
                // 签名错误，如果数据里没有sign字段，也认为是签名错误
                resXml = "<xml>return_code><![CDATA[FAIL]]></return_code>return_msg><![CDATA[报文为空]]></return_msg></xml>";
            }
            return resXml;

        } catch (Exception e) {
            e.printStackTrace();
            return "<xml>return_code><![CDATA[FAIL]]></return_code>return_msg><![CDATA[服务器错误]]></return_msg></xml>";
        }

    }


    @RequestMapping("/wechat/refund")
    public ReturnData refund(@RequestParam Map<String, Object> params) throws Exception {

        try {

            ReturnData r = ReturnData.ok();

            String out_trade_no = params.get("orderNo").toString();//商户订单号
            String total_fee = "1";//金额

            Map<String, String> data = new HashMap();

            data.put("appid", AM_APPID);
            data.put("mch_id", AM_MCH_ID);
            data.put("out_trade_no", out_trade_no); //out_trade_no   商户订单号
//          data.put("transaction_id", transaction_id);//out_refund_no 微信订单号    和商户订单号二选一即可

            data.put("out_refund_no", out_trade_no);//out_refund_no 商户退款单号
            data.put("total_fee", total_fee);       //total_fee 订单金额
            data.put("refund_fee", total_fee);      //refund_fee 退款金额
            data.put("refund_fee_type", "CNY");     //refund_fee_type 退款货币种类 需与支付一致

            WeChatConfig config = new WeChatConfig(AM_APPID, AM_MCH_ID, AM_MCH_KEY, "D://Users//lwj//Desktop//cert//apiclient_cert.p12");

            WXPay wxpay = new WXPay(config);
            Map<String, String> resp = wxpay.refund(data);


            try {
                if ("SUCCESS".equals(resp.get("result_code")) && "OK".equals(resp.get("return_msg"))) {
                    /*
                     *
                     *   退款成功后 写的代码地方
                     *
                     * */
                    r.put("type", true);
                    r.put("msg", "退款成功");
                    System.out.println("订单号：" + out_trade_no + "退款成功");
                    return r;
                } else {
                    r.put("type", true);
                    r.put("msg", "退款失败");
                    return r;
                }

            } catch (Exception e) {
                r.put("type", true);
                r.put("msg", "退款失败");
                return r;
            }
        } catch (Exception e) {
            ReturnData r = ReturnData.error();
            r.put("type", false);
            r.put("msg", "啊哦~服务器出错了");
            r.put("errorCode", "000");
            return r;
        }


    }

    /*
     * 二维码支付
     * */
    @RequestMapping("/wxapi/qrcode")
    public ReturnData qrcode(@RequestParam Map<String, Object> params, HttpServletRequest request, HttpServletResponse response) throws Exception {

        try {

            ReturnData r = ReturnData.ok();

            String out_trade_no = params.get("orderNo").toString();//商户订单号
            String total_fee = "1";//金额

            Map<String, String> data = new HashMap();

            data.put("appid", AM_APPID);
            data.put("mch_id", AM_MCH_ID);
            data.put("body", "二维码支付");
            data.put("out_trade_no", out_trade_no); //out_trade_no   商户订单号
            data.put("total_fee", total_fee);       //total_fee 订单金额
            data.put("spbill_create_ip", request.getRemoteHost());
            data.put("notify_url", "http://qijimianliu.iok.la/wxapi/payBack");//这个回调需要微信支付商户平台设置 不然是不会回调（应该吧）
            data.put("trade_type", "NATIVE");


            WeChatConfig config = new WeChatConfig(AM_APPID, AM_MCH_ID, AM_MCH_KEY, "");

            WXPay wxpay = new WXPay(config);
            Map<String, String> resp = wxpay.unifiedOrder(data);

            try {
                if ("SUCCESS".equals(resp.get("result_code")) && "OK".equals(resp.get("return_msg"))) {

                    SortedMap<String, String> finalpackage = new TreeMap<String, String>();
                    finalpackage.put("code_url", resp.get("code_url"));


                    r.put("type", true);
                    r.put("data", finalpackage);
                } else {
                    r.put("type", false);
                }
                return r;
            } catch (Exception e) {
                r.put("type", false);
                r.put("msg", "退款失败");
                return r;
            }


        } catch (Exception e) {
            ReturnData r = ReturnData.error();
            r.put("type", false);
            r.put("msg", "啊哦~服务器出错了");
            r.put("errorCode", "000");
            return r;
        }
    }
}
