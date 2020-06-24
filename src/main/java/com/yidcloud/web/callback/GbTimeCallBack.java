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
 * 国标登录协议数据回写
 * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
 * @copyright: Copyright (c) 2018
 * @company: 易登科技
 * @author: zl
 * @version: 2.0
 * @date: 2018年6月12日 17:21:54
 */
public class GbTimeCallBack extends AbstractCallback {

    private static Logger logger = LoggerFactory.getLogger(GbTimeCallBack.class);

    @Override
    CallbackMessage createCallBackMsg(ReceiveMessage msg) throws Exception {
        //判断是否需要回写
        CallbackMessage callbackMessage = new CallbackMessage();
        
        String msgs = msg.getMsgByte();
        String headMsg = msgs.substring(0, 44);
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
        String bodyMsg = String.format("%s%s%s", headMsg,"0006",sdateTime);
        byte[] body = DefaultProtocolConvert.hexstring2bytes(bodyMsg);
        body[3] = 1;
        int xor = DefaultProtocolConvert.xor(DefaultProtocolConvert.subBytes(body, 2, body.length-2));
        
        //协议头 取消息第一个字节
        callbackMessage.setHeaderMsg(DefaultProtocolConvert.subBytes(body, 0, 1));
        //协议尾 为校验码
        callbackMessage.setFooterMsg(DefaultProtocolConvert.hexstring2bytes(xor+""));
        callbackMessage.setBodyMsg(DefaultProtocolConvert.subBytes(body, 1, body.length-1));
        
        logger.info("["+msg.getImei()+"]校时数据回写="+DefaultProtocolConvert.bytes2hexstring(callbackMessage.getHeaderMsg())+
                DefaultProtocolConvert.bytes2hexstring(callbackMessage.getBodyMsg())+
                DefaultProtocolConvert.bytes2hexstring(callbackMessage.getFooterMsg())+" byte="+callbackMessage.toString());
        return callbackMessage;
    }
    
}
