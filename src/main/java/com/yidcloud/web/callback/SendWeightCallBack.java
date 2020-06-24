package com.yidcloud.web.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yidcloud.web.convert.DefaultProtocolConvert;
import com.yidcloud.web.model.CallbackMessage;
import com.yidcloud.web.model.ReceiveMessage;

/**
 * 易登盒子心跳协议回写
 * 
 * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
 * @copyright: Copyright (c) 2018
 * @company: 易登科技
 * @author: zhouliang@edenep.net
 * @version: 2.0
 * @date: 2018年7月11日 下午9:18:27
 */
public class SendWeightCallBack extends AbstractCallback {

    private static Logger logger = LoggerFactory.getLogger(SendWeightCallBack.class);

    @Override
    CallbackMessage createCallBackMsg(ReceiveMessage msg) throws Exception {
        //判断是否需要回写
        CallbackMessage callbackMessage = new CallbackMessage();
        String replyHeadTag = msg.getHeadTag();//心跳回复协议头
        String replyEndTag = msg.getEndTag();//心跳回复协议尾
        byte[] imeiByte = DefaultProtocolConvert.hexstring2bytes(msg.getImei());;
        byte[] cmdByte = new byte[1];
        cmdByte[0] = 0x43;
        byte[] status = new byte[1];
        status[0] = 0x01;
        //5=帧头+帧长+指令码+校验+帧尾
        byte[] protocolLength_byte = DefaultProtocolConvert
                .hexstring2bytes(DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(imeiByte.length + 5 + status.length), 2));//数据长度

        byte[] totalDataByte = DefaultProtocolConvert
                .add(DefaultProtocolConvert.add(DefaultProtocolConvert.add(protocolLength_byte, cmdByte), imeiByte), status);//从帧长到数据内容

        //协议校验码
        byte[] protocolVerify_byte = new byte[] { (byte) DefaultProtocolConvert.byteSum(totalDataByte) };

        //协议头 取消息第一个字节
        callbackMessage.setHeaderMsg(DefaultProtocolConvert.hexstring2bytes(replyHeadTag));
        //协议尾 为校验码
        callbackMessage.setFooterMsg(DefaultProtocolConvert.hexstring2bytes(replyEndTag));

        callbackMessage.setBodyMsg(DefaultProtocolConvert.add(totalDataByte, protocolVerify_byte));

        logger.info("[" + msg.getImei() + "]上传重量回写=" + DefaultProtocolConvert.bytes2hexstring(callbackMessage.getHeaderMsg())
                + DefaultProtocolConvert.bytes2hexstring(callbackMessage.getBodyMsg())
                + DefaultProtocolConvert.bytes2hexstring(callbackMessage.getFooterMsg()) + " byte=" + callbackMessage.toString());
        return callbackMessage;
    }
}
