package lsy.project.socket.api.consumer;

import java.util.HashMap;
import java.util.Map;

import com.lsy.base.result.ResultVo;
import com.lsy.base.string.StringHelper;
import com.lsy.rabbitmq.client.consumer.AbstractMqConsumer;
import lsy.project.socket.api.cache.CollectRedisCacheService;
import lsy.project.socket.api.contants.CollectContants;
import lsy.project.socket.api.convert.DefaultProtocolConvert;
import lsy.project.socket.api.enums.BooleanCharEnum;
import lsy.project.socket.api.model.CallbackMessage;
import lsy.project.socket.api.util.NettyChannelMap;
import lsy.project.socket.api.util.VendingMachineUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import io.netty.channel.Channel;


/**
 * 服务器下发指令给硬件 消费类
 *
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public class ServerSendmsgToClientConsumer extends AbstractMqConsumer {

    static Logger logger = LoggerFactory.getLogger(ServerSendmsgToClientConsumer.class);

    @Override
    public ResultVo invoke(Map<String, String> param) {

        ResultVo vo = new ResultVo();
        String imei = param.get(CollectContants.IMEI);
        if (null == imei) {
            return vo;
        }

        Map<String, String> imeimap = NettyChannelMap.getImeiMap();
        if (null == imeimap || imeimap.isEmpty() || !imeimap.containsKey(imei)) {
            return vo;
        }

        //设备号对应的客户端通道ID
        String clientId = imeimap.get(imei);
        //客户端tcp通道
        Channel channel = NettyChannelMap.get(clientId);
        String cmd = param.get(CollectContants.CMD);
        switch (cmd) {
            case "FC44":
                callBackFc44(param, channel);
                break;
            case "2002":
                callBackVMC2002(param, channel);
                break;
            case "3000":
                callBackVMC3000(param, channel);
                break;
            case "6000":
            case "6001":
                callBackVMC6001(param, channel);
                break;
            case "6002":
                callBackVMC6002(param, channel);
                break;
            default:
                break;
        }
        return vo;
    }

    /**
     * 垃圾箱扫码与被扫 开门 数据回写给硬件
     *
     * @param param
     * @param channel
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年9月28日 下午4:55:36
     */
    private void callBackFc44(Map<String, String> param, Channel channel) {

        boolean success = param.get(CollectContants.ISSUCCRSS).equalsIgnoreCase(BooleanCharEnum.TRUE.getValue()) ? true : false;
        byte[] cmdByte = new byte[1];
        byte[] contantType = null;
        if (success) {
            cmdByte[0] = 0x45;
            contantType = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.string2hexstring(param.get("qcode")));
        } else {
            cmdByte[0] = 0x53;
            byte[] status = new byte[1];
            status[0] = Byte.parseByte(param.get("errcode"));//错误码
            contantType = status;
        }
        byte[] imeiByte = DefaultProtocolConvert.hexstring2bytes(param.get(CollectContants.IMEI));

        CallbackMessage callbackMessage = new CallbackMessage();
        callbackMessage.setHeaderMsg(DefaultProtocolConvert.hexstring2bytes(param.get(CollectContants.HEADTAG)));
        //协议尾 为校验码
        callbackMessage.setFooterMsg(DefaultProtocolConvert.hexstring2bytes(param.get(CollectContants.ENDTAG)));
        //5=帧头+帧长+指令码+校验+帧尾
        byte[] protocolLength_byte = DefaultProtocolConvert
                .hexstring2bytes(DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(imeiByte.length + 5 + contantType.length), 2));//数据长度

        byte[] totalDataByte = DefaultProtocolConvert
                .add(DefaultProtocolConvert.add(DefaultProtocolConvert.add(protocolLength_byte, cmdByte), imeiByte), contantType);//从帧长到数据内容
        //协议校验码
        byte[] protocolVerify_byte = new byte[]{(byte) DefaultProtocolConvert.byteSum(totalDataByte)};

        callbackMessage.setBodyMsg(DefaultProtocolConvert.add(totalDataByte, protocolVerify_byte));
        logger.info(String.format("设备ID：%s,协议ID:%s,回写hex:%s,回写二进制:%s", param.get(CollectContants.IMEI), param.get(CollectContants.MID),
                DefaultProtocolConvert.bytes2hexstring(callbackMessage.getHeaderMsg()) +
                        DefaultProtocolConvert.bytes2hexstring(callbackMessage.getBodyMsg()) +
                        DefaultProtocolConvert.bytes2hexstring(callbackMessage.getFooterMsg()),
                callbackMessage.toString()));
        channel.writeAndFlush(callbackMessage);
    }


    private void callBackVMC2002(Map<String, String> param, Channel channel) {

        CallbackMessage callbackMessage = new CallbackMessage();
        callbackMessage.setHeaderMsg(DefaultProtocolConvert.hexstring2bytes(param.get(CollectContants.HEADTAG)));
        callbackMessage.setFooterMsg(DefaultProtocolConvert.hexstring2bytes(param.get(CollectContants.ENDTAG)));

        String imei = param.get("imei");
        String mid = param.get("mid");
        Map<String, String> sendMap = new HashMap<>();
        sendMap.put("TimeSp", System.currentTimeMillis() / 1000 + "");
        sendMap.put("Mid", StringHelper.isBlank(mid) ? CollectRedisCacheService.getVMCMidByImei(imei) : mid);
        String result = VendingMachineUtil.getVcmSendResult(param.get("cmd"), sendMap);
        logger.info("服务器主动检测心跳===============" + result);
        if (StringHelper.isBlank(result)) {
            return;
        }
        callbackMessage.setBodyMsg(DefaultProtocolConvert.hexstring2bytes(
                DefaultProtocolConvert.stringToASCII(result, result.length(), "")));
        channel.writeAndFlush(callbackMessage);
    }

    private void callBackVMC3000(Map<String, String> param, Channel channel) {

        CallbackMessage callbackMessage = new CallbackMessage();
        callbackMessage.setHeaderMsg(DefaultProtocolConvert.hexstring2bytes(param.get(CollectContants.HEADTAG)));
        callbackMessage.setFooterMsg(DefaultProtocolConvert.hexstring2bytes(param.get(CollectContants.ENDTAG)));

        String imei = param.get("imei");
        String mid = param.get("mid");
        Map<String, String> sendMap = new HashMap<>();
        sendMap.put("OrderNo", param.get("OrderNo"));
        sendMap.put("Slot", param.get("Slot"));
        sendMap.put("SKU", param.get("SKU"));
        sendMap.put("Price", param.get("Price"));
        sendMap.put("PayType", param.get("PayType"));
        sendMap.put("TimeSp", StringHelper.isBlank(param.get("TimeSp")) ? System.currentTimeMillis() / 1000 + "" : param.get("TimeSp"));
        sendMap.put("Mid", StringHelper.isBlank(mid) ? CollectRedisCacheService.getVMCMidByImei(imei) : mid);
        String result = VendingMachineUtil.getVcmSendResult(param.get("cmd"), sendMap);
        logger.info("服务器主动下发远程出货命定===============" + result);
        if (StringHelper.isBlank(result)) {
            return;
        }
        callbackMessage.setBodyMsg(DefaultProtocolConvert.hexstring2bytes(
                DefaultProtocolConvert.stringToASCII(result, result.length(), "")));
        channel.writeAndFlush(callbackMessage);
    }

    private void callBackVMC6001(Map<String, String> param, Channel channel) {

        CallbackMessage callbackMessage = new CallbackMessage();
        callbackMessage.setHeaderMsg(DefaultProtocolConvert.hexstring2bytes(param.get(CollectContants.HEADTAG)));
        callbackMessage.setFooterMsg(DefaultProtocolConvert.hexstring2bytes(param.get(CollectContants.ENDTAG)));

        String imei = param.get("imei");
        String mid = param.get("mid");
        Map<String, String> sendMap = new HashMap<>();
        sendMap.put("DropSensor", param.get("DropSensor") == null ? "1" : param.get("DropSensor"));
        if (StringHelper.isNotBlank(param.get("Temp"))) {
            sendMap.put("Temp", param.get("Temp"));
        }
        if (StringHelper.isNotBlank(param.get("TempMode"))) {
            sendMap.put("TempMode", param.get("TempMode"));
        }
        if (StringHelper.isNotBlank(param.get("LedLight"))) {
            sendMap.put("LedLight", param.get("LedLight"));
        }
        if (StringHelper.isNotBlank(param.get("DoorHot"))) {
            sendMap.put("DoorHot", param.get("DoorHot"));
        }
        sendMap.put("TimeSp", param.get("TimeSp"));
        sendMap.put("Mid", StringHelper.isBlank(mid) ? CollectRedisCacheService.getVMCMidByImei(imei) : mid);
        String result = VendingMachineUtil.getVcmSendResult(param.get("cmd"), sendMap);
        logger.info("服务器主动下发参数设置===============" + result);
        if (StringHelper.isBlank(result)) {
            return;
        }
        callbackMessage.setBodyMsg(DefaultProtocolConvert.hexstring2bytes(
                DefaultProtocolConvert.stringToASCII(result, result.length(), "")));
        channel.writeAndFlush(callbackMessage);
        logger.info("售货机下发：" + DefaultProtocolConvert.bytes2hexstring(callbackMessage.getHeaderMsg()) +
                DefaultProtocolConvert.bytes2hexstring(callbackMessage.getBodyMsg()) +
                DefaultProtocolConvert.bytes2hexstring(callbackMessage.getFooterMsg()));
    }

    private void callBackVMC6002(Map<String, String> param, Channel channel) {
        CallbackMessage callbackMessage = new CallbackMessage();
        callbackMessage.setHeaderMsg(DefaultProtocolConvert.hexstring2bytes(param.get(CollectContants.HEADTAG)));
        callbackMessage.setFooterMsg(DefaultProtocolConvert.hexstring2bytes(param.get(CollectContants.ENDTAG)));

        String imei = param.get("imei");
        String mid = param.get("mid");
        Map<String, String> sendMap = new HashMap<>();
        sendMap.put("TimeSp", param.get("TimeSp"));
        sendMap.put("Mid", StringHelper.isBlank(mid) ? CollectRedisCacheService.getVMCMidByImei(imei) : mid);
        String result = VendingMachineUtil.getVcmSendResult(param.get("cmd"), sendMap);
        logger.info("远程清除货道故障并测试货道===============" + result);
        if (StringHelper.isBlank(result)) {
            return;
        }
        callbackMessage.setBodyMsg(DefaultProtocolConvert.hexstring2bytes(
                DefaultProtocolConvert.stringToASCII(result, result.length(), "")));
        channel.writeAndFlush(callbackMessage);
    }
}
