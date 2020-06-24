package com.yidcloud.web.callback;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yidcloud.web.convert.DefaultProtocolConvert;
import com.yidcloud.web.model.CallbackMessage;
import com.yidcloud.web.model.ReceiveMessage;

/**
 * 
 * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
 * @copyright: Copyright (c) 2018
 * @company: 易登科技
 * @author: zhouliang@edenep.net
 * @version: 2.0
 * @date: 2018年12月26日 下午4:31:51
 */
public class YdBoxTimeCallBack extends AbstractCallback {

    private static Logger logger = LoggerFactory.getLogger(YdBoxTimeCallBack.class);

    @Override
    CallbackMessage createCallBackMsg(ReceiveMessage msg) throws Exception {
        //判断是否需要回写
        CallbackMessage callbackMessage = new CallbackMessage();
        
        String replyHeadTag = null;//心跳回复协议头
        String replyEndTag = null;//心跳回复协议尾
        byte[] imeiByte = null;
        byte[] cmdByte = new byte[1];
        if("FE".equalsIgnoreCase(msg.getHeadTag())) {//新智能盒子
            replyHeadTag = "FD";
            replyEndTag = "DF";
            cmdByte[0] = 0x02;
            imeiByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.stringToASCII(msg.getImei(), msg.getImei().length(), ""));
        }else if("FC".equalsIgnoreCase(msg.getHeadTag())) {//智能垃圾桶
            replyHeadTag = "FC";
            replyEndTag = "CF";
            cmdByte[0] = 0x02;
            imeiByte = DefaultProtocolConvert.hexstring2bytes(msg.getImei());
        }else if("FB".equalsIgnoreCase(msg.getHeadTag())) {//智能公厕
            replyHeadTag = "FB";
            replyEndTag = "BF";
            cmdByte[0] = 0x07;
            imeiByte = DefaultProtocolConvert.hexstring2bytes(msg.getImei());
        };
        
        DateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        String dateStr = sdf.format(new Date());
        StringBuilder sdateTime = new StringBuilder();
        StringBuilder stime = new StringBuilder();
        for(int i=0;i<dateStr.length();i++) {
            stime.append(dateStr.charAt(i));
            if((i+1)%2==0) {
                String a = DefaultProtocolConvert.int2hexstring(Integer.valueOf(stime.toString()));
                sdateTime.append(a.length()<2?String.format("0%s", a):a);
                stime.setLength(0);
            }
        }
        
        byte[] sdateTimeByte = DefaultProtocolConvert.hexstring2bytes(sdateTime.toString());
        //5=帧头+帧长+指令码+校验+帧尾
        byte[] protocolLength_byte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(imeiByte.length+5+sdateTimeByte.length),2));//数据长度

        byte[] totalDataByte = DefaultProtocolConvert.add(DefaultProtocolConvert.add(DefaultProtocolConvert
                        .add(protocolLength_byte, cmdByte), imeiByte),sdateTimeByte);//从帧长到数据内容
        
        //协议校验码
        byte[] protocolVerify_byte = new byte[] {(byte) DefaultProtocolConvert.byteSum(totalDataByte)};
        
        //协议头 取消息第一个字节
        callbackMessage.setHeaderMsg(DefaultProtocolConvert.hexstring2bytes(replyHeadTag));
        //协议尾 为校验码
        callbackMessage.setFooterMsg(DefaultProtocolConvert.hexstring2bytes(replyEndTag));
        
        callbackMessage.setBodyMsg(DefaultProtocolConvert.add(totalDataByte,protocolVerify_byte));
        
        logger.info("["+msg.getImei()+"]终端校时="+DefaultProtocolConvert.bytes2hexstring(callbackMessage.getHeaderMsg())+
                DefaultProtocolConvert.bytes2hexstring(callbackMessage.getBodyMsg())+
                DefaultProtocolConvert.bytes2hexstring(callbackMessage.getFooterMsg())+
                " byte="+callbackMessage.toString());
        
        return callbackMessage;
    }
    
}
