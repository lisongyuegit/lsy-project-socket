package com.yidcloud.web.callback;

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
 * @date: 2018年5月17日 下午4:08:07
 */
public class GbLoginCallBack extends AbstractCallback {

    private static Logger logger = LoggerFactory.getLogger(GbLoginCallBack.class);

    @Override
    CallbackMessage createCallBackMsg(ReceiveMessage msg) throws Exception {
        //判断是否需要回写
        CallbackMessage callbackMessage = new CallbackMessage();
        
        byte[] body = DefaultProtocolConvert.hexstring2bytes(msg.getMsgByte());
        byte[] outbytes = DefaultProtocolConvert.subBytes(body, 0, 30);
        outbytes[3] = 1;
        //修改数据长度为固定6(终端数据发送的时间6字节)
        outbytes[22] = 0x00;
        outbytes[23] = 0x06;
        //重新计算校验码
        int xor = DefaultProtocolConvert.xor(DefaultProtocolConvert.subBytes(outbytes, 2, outbytes.length - 2));
        
        //协议头 取消息第一个字节
        callbackMessage.setHeaderMsg(DefaultProtocolConvert.subBytes(outbytes, 0, 1));
        //协议尾 为校验码
        callbackMessage.setFooterMsg(DefaultProtocolConvert.hexstring2bytes(xor+""));
        callbackMessage.setBodyMsg(DefaultProtocolConvert.subBytes(outbytes, 1, 29));
        
        logger.info("["+msg.getImei()+"]登录校验回写="+DefaultProtocolConvert.bytes2hexstring(callbackMessage.getHeaderMsg())+
                DefaultProtocolConvert.bytes2hexstring(callbackMessage.getBodyMsg())+
                DefaultProtocolConvert.bytes2hexstring(callbackMessage.getFooterMsg())+
                " byte="+callbackMessage.toString());
        return callbackMessage;
    }
}
