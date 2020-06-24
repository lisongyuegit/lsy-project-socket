package com.yidcloud.web.callback;

import com.lsy.base.utils.PropertiesHelper;
import com.yidcloud.web.convert.DefaultProtocolConvert;
import com.yidcloud.web.model.CallbackMessage;
import com.yidcloud.web.model.ReceiveMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 易登盒子心跳协议回写
 * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
 * @copyright: Copyright (c) 2018
 * @company: 易登科技
 * @author: zhouliang@edenep.net
 * @version: 2.0
 * @date: 2019年7月9日18:45:35
 */
public class HeartCallBack2 extends AbstractCallback {

    private static Logger logger = LoggerFactory.getLogger(HeartCallBack2.class);

    private static final long TIMEOUT = 10;//盒子升级包 回复超时时间
    @Override
    CallbackMessage createCallBackMsg(ReceiveMessage msg) throws Exception {
        //判断是否需要回写
        CallbackMessage callbackMessage = new CallbackMessage();
        
        String replyHeadTag = null;//心跳回复协议头
        String replyEndTag = null;//心跳回复协议尾
        byte[] imeiByte = null;
        byte[] cmdByte = new byte[1];
        if("FE".equalsIgnoreCase(msg.getHeadTag()) || msg.getImei().length()==17) {//新智能盒子
            replyHeadTag = "FD";
            replyEndTag = "DF";
            cmdByte[0] = 0x02;
            imeiByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.stringToASCII(msg.getImei(), msg.getImei().length(), ""));
        }else if("FC".equalsIgnoreCase(msg.getHeadTag()) //智能垃圾桶
                || "FB".equalsIgnoreCase(msg.getHeadTag()) //智能公厕
                || "FA".equalsIgnoreCase(msg.getHeadTag())
                || msg.getImei().length()<17) {//GPS定位
            replyEndTag = new StringBuffer(replyHeadTag).reverse().toString();
            replyHeadTag = msg.getHeadTag().toUpperCase();
            cmdByte[0] = 0x02;
            imeiByte = DefaultProtocolConvert.hexstring2bytes(msg.getImei());
        }

        PropertiesHelper helper = new PropertiesHelper("system.properties");
        String error_imei = helper.getStringProperty("special_callback_imei")==null?"10000000000000033,":helper.getStringProperty("special_callback_imei")+",";
        if(!error_imei.contains(msg.getImei()+",")){
            return null;
        }
        //5=帧头+帧长+指令码+校验+帧尾
        byte[] protocolLength_byte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(imeiByte.length+5),2));//数据长度

        byte[] totalDataByte = DefaultProtocolConvert.add(DefaultProtocolConvert
                        .add(protocolLength_byte, cmdByte), imeiByte);//从帧长到数据内容
        
        //协议校验码
        byte[] protocolVerify_byte = new byte[] {(byte) DefaultProtocolConvert.byteSum(totalDataByte)};
        
        //协议头 取消息第一个字节
        callbackMessage.setHeaderMsg(DefaultProtocolConvert.hexstring2bytes(replyHeadTag));
        //协议尾 为校验码
        callbackMessage.setFooterMsg(DefaultProtocolConvert.hexstring2bytes(replyEndTag));
        
        callbackMessage.setBodyMsg(DefaultProtocolConvert.add(totalDataByte,protocolVerify_byte));
        
        logger.info("["+msg.getImei()+"]心跳回写="+DefaultProtocolConvert.bytes2hexstring(callbackMessage.getHeaderMsg())+
                DefaultProtocolConvert.bytes2hexstring(callbackMessage.getBodyMsg())+
                DefaultProtocolConvert.bytes2hexstring(callbackMessage.getFooterMsg())+
                " byte="+callbackMessage.toString());
        
        return callbackMessage;
    }
}
