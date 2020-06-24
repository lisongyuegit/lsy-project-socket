package com.yidcloud.web.callback;

import com.yidcloud.web.convert.DefaultProtocolConvert;
import com.yidcloud.web.model.CallbackMessage;
import com.yidcloud.web.model.ReceiveMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 博实结鉴权协议回写
 * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
 * @copyright: Copyright (c) 2018
 * @company: 易登科技
 * @author: zhouliang@edenep.net
 * @version: 2.0
 * @date: 2019年7月11日14:43:54
 */
public class BSJAuthCallBack extends AbstractCallback {

    private static Logger logger = LoggerFactory.getLogger(BSJAuthCallBack.class);

    private static final long TIMEOUT = 10;//盒子升级包 回复超时时间
    @Override
    CallbackMessage createCallBackMsg(ReceiveMessage msg) throws Exception {
        //判断是否需要回写
        CallbackMessage callbackMessage = new CallbackMessage();
        
        String replyHeadTag = msg.getHeadTag();//心跳回复协议头
        String replyEndTag = msg.getHeadTag();//心跳回复协议尾

        byte[] body = DefaultProtocolConvert.hexstring2bytes(msg.getMsgByte());

        byte[] outbytes = DefaultProtocolConvert.subBytes(body, 1, 12);
        outbytes[0] = DefaultProtocolConvert.hexstring2bytes("80")[0];
        outbytes[1] = DefaultProtocolConvert.hexstring2bytes("01")[0];
        outbytes[2] = DefaultProtocolConvert.hexstring2bytes("00")[0];
        outbytes[3] = DefaultProtocolConvert.hexstring2bytes("05")[0];

        byte[] replayNumber = DefaultProtocolConvert.subBytes(outbytes, 10, 2);
        byte[] replayId = DefaultProtocolConvert.hexstring2bytes("0102");
        byte[] replayState = DefaultProtocolConvert.hexstring2bytes("00");
        //5=帧头+帧长+指令码+校验+帧尾
        byte[] totalDataByte = DefaultProtocolConvert.add(DefaultProtocolConvert.add(DefaultProtocolConvert
                        .add(outbytes, replayNumber), replayId), replayState);//从帧长到数据内容
        //协议校验码
        int xor = DefaultProtocolConvert.xor(totalDataByte);


        System.out.println(DefaultProtocolConvert.int2hexstring(xor));
        byte[] xorByte =  DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.int2hexstring(xor,2));
        //从消息ID到校验码;
        byte[] msgBody = DefaultProtocolConvert.add(totalDataByte, xorByte);
        //协议头 取消息第一个字节
        callbackMessage.setHeaderMsg(DefaultProtocolConvert.hexstring2bytes(replyHeadTag));
        //协议尾 为校验码
        callbackMessage.setFooterMsg(DefaultProtocolConvert.hexstring2bytes(replyEndTag));
        
        callbackMessage.setBodyMsg(msgBody);
        
        logger.info("["+msg.getImei()+"]鉴权回写="+DefaultProtocolConvert.bytes2hexstring(callbackMessage.getHeaderMsg())+
                DefaultProtocolConvert.bytes2hexstring(callbackMessage.getBodyMsg())+
                DefaultProtocolConvert.bytes2hexstring(callbackMessage.getFooterMsg())+
                " byte="+callbackMessage.toString());
        
        return callbackMessage;
    }

    public static void main(String[] args) {

        byte [] head = DefaultProtocolConvert.hexstring2bytes("7e");
        System.out.println(head[0]);

        System.out.println(DefaultProtocolConvert.hexstring2bytes("FD")[0]);
        byte a [] = new byte[] {-3};
        System.out.println(DefaultProtocolConvert.bytes2hexstring(a));

        String as = "000d" ;
        byte[] body = DefaultProtocolConvert.hexstring2bytes(as);
        byte[] outbytes = DefaultProtocolConvert.subBytes(body, 0, 1);
        byte[] lengthByte = DefaultProtocolConvert.byte2bitArray(body);
        for(int i = 0;i<lengthByte.length;i++){
            System.out.print(lengthByte[i]);
        }

        System.out.println();

        byte aaaa [] = new byte[2];
        aaaa[0] = DefaultProtocolConvert.bit2byte("00000000");
        aaaa[1] = DefaultProtocolConvert.bit2byte("00101101");
        String xxt2 = DefaultProtocolConvert.bytes2hexstring(aaaa);
        System.out.println(xxt2);

        byte[] authcode = DefaultProtocolConvert.hexstring2bytes("01020304050607080910");
        System.out.println(authcode.length);

        System.out.println(DefaultProtocolConvert.int2hexstring(7));

        String s = DefaultProtocolConvert.int2hexstring(57,2);
        System.out.println(s);
    }
}
