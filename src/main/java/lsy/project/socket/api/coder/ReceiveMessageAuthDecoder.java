package lsy.project.socket.api.coder;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;
import com.lsy.base.date.DateHelper;
import com.lsy.base.string.StringHelper;
import com.lsy.rabbitmq.client.MqClientManager;
import lsy.project.socket.api.cache.CollectRedisCacheService;
import lsy.project.socket.api.contants.CollectContants;
import lsy.project.socket.api.convert.DefaultProtocolConvert;
import lsy.project.socket.api.entity.Protocol;
import lsy.project.socket.api.entity.ProtocolType;
import lsy.project.socket.api.model.ReceiveMessage;
import lsy.project.socket.api.util.CollectUtil;
import lsy.project.socket.api.util.StandardConstantResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;


/**
 * 接收的消息，合法性校验
 *
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public class ReceiveMessageAuthDecoder extends ByteToMessageDecoder {

    static Logger logger = LoggerFactory.getLogger(ReceiveMessageAuthDecoder.class);
    /**
     * 消息头字节长度
     */
    private static final int HEAD_LENGTH = 4;
    /**
     * 消息最大长度
     */
    private static final int MAX_BODY_LENGTH = 1024 * 10;

    private int port;

    public ReceiveMessageAuthDecoder(int port) {
        super();
        this.port = port;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        printReqInfo(byteBuf);
        //判断消息长度，如果小于4则返回
        if (byteBuf.readableBytes() < HEAD_LENGTH) {
            return;
        }

        //取前面四个字节  用于取协议类型
        byte[] startTagByte = new byte[HEAD_LENGTH];
        byteBuf.readBytes(startTagByte);
        byteBuf.resetReaderIndex();

        ProtocolType protocolType = getProtocolType(startTagByte);
        //判断协议类型是否存在
        if (protocolType == null) {
            printErrorBody(channelHandlerContext, byteBuf, startTagByte, StandardConstantResp.ERROR_PROTOCOLTYPE_NOTEXIST);
            return;
        }

        // 太大的数据，认为是不合理的   防止客户端传来的数据过大
        if (byteBuf.readableBytes() > MAX_BODY_LENGTH) {
            printErrorBody(channelHandlerContext, byteBuf, startTagByte, StandardConstantResp.ERROR_DATA_OVERLENGTH);
            return;
        }

        // 分包处理客户端发过来的信息
        byte[] body = unpacked(channelHandlerContext, byteBuf, protocolType.getStartTag());
        //判断消息长度 
        if (null == body) {
            return;
        }
        //对body做初步解析，并把body组装成ReceiveMessage
        ReceiveMessage receiveMessage = getAuthReceiveMessage(body, protocolType);
        if (receiveMessage == null) {
            return;
        } else {
            list.add(receiveMessage);
        }
    }

    /**
     * 拆包处理
     *
     * @param channelHandlerContext 客户端通道
     * @param byteBuf               缓存取字节流
     * @param startTag              协议头
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年7月19日 下午5:21:02
     */
    public byte[] unpacked(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, String startTag) {

        byte[] body = null;

        String remoteAddress = channelHandlerContext.channel().remoteAddress().toString();
        switch (startTag) {
            //国标协议做分包处理
            case "2323":
                body = gbUnPacked(byteBuf);
                break;
            case "232323":
                body = vendingMachineUnPacked(byteBuf);
                break;
            //易登智能盒子fe，垃圾桶fc,智能公厕,GPS定位 协议做分包处理
            case "fe":
            case "fc":
            case "fb":
            case "fa":
                body = edenepBoxUnpacked(byteBuf);
                break;
            //老智能盒子 协议做分包处理
            case "5b":
                body = smartBoxUnpacked(byteBuf);
                break;
//		case "7e":
//            body = szvehicleUnpacked(byteBuf);
//            break;
            case "5e":
                body = androidUnpacked(byteBuf);
                break;
            case "7e":
                body = bsjUnpacked(byteBuf);
                break;
            case "2a23":
                body = TTXUnpacked(byteBuf);
                break;
            case "4641":
                body = chuyuUnpacked(byteBuf);
                break;
            //默认不做分包处理
            default:
                body = others(byteBuf);
                break;
        }

        if (null == body) {
            byte[] errorBody = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(errorBody);
            byteBuf.resetReaderIndex();
            logger.error(String.format(StandardConstantResp.ERROR_INCOMPLETE_PACKAGE.getMessage(), remoteAddress, startTag, DefaultProtocolConvert.bytes2hexstring(errorBody)));
        } else {
            logger.info(String.format(StandardConstantResp.COLLECT_OK.getMessage(), remoteAddress, startTag, DefaultProtocolConvert.bytes2hexstring(body)));
        }
        return body;
    }

    private byte[] chuyuUnpacked(ByteBuf byteBuf) {
        byte[] tembody = new byte[MAX_BODY_LENGTH];
        while (true) {
            tembody[byteBuf.readerIndex()] = byteBuf.readByte();

            String srcText = DefaultProtocolConvert.bytes2hexstring(tembody);
            int headTag = srcText.indexOf("4641");
            int endTag = srcText.indexOf("4146");
            if (headTag >= 0 && endTag >= 0 && endTag > headTag) {
                break;
            }
            //当缓存区所有的字节读取完毕 则返回
            if (0 == byteBuf.readableBytes()) {
                return null;
            }
        }

        byte[] body = DefaultProtocolConvert.subBytes(tembody, 0, byteBuf.readerIndex());
        //标记已读位置（实际相当于已经从缓存区读取过的数据不再读取）
        byteBuf.markReaderIndex();

        String tempbodystr = DefaultProtocolConvert.bytes2hexstring(body);
        tempbodystr = tempbodystr.substring(tempbodystr.indexOf("4641"), tempbodystr.length());
        body = DefaultProtocolConvert.hexstring2bytes(tempbodystr);
        return body;
    }

    private byte[] vendingMachineUnPacked(ByteBuf byteBuf) {
        byte[] tembody = new byte[MAX_BODY_LENGTH];
        while (true) {
            tembody[byteBuf.readerIndex()] = byteBuf.readByte();
            //当检测到&&&结尾字符时,结束
            if (DefaultProtocolConvert.bytes2hexstring(tembody).contains("262626")) {
                break;
            }
            //当缓存区所有的字节读取完毕 则返回
            if (0 == byteBuf.readableBytes()) {
                return null;
            }
        }

        byte[] body = DefaultProtocolConvert.subBytes(tembody, 0, byteBuf.readerIndex());
        //标记已读位置（实际相当于已经从缓存区读取过的数据不再读取）
        byteBuf.markReaderIndex();

        String tempbodystr = DefaultProtocolConvert.bytes2hexstring(body);
        tempbodystr = tempbodystr.substring(tempbodystr.indexOf("232323"), tempbodystr.length());
        body = DefaultProtocolConvert.hexstring2bytes(tempbodystr);

        logger.info("售货机body====" + DefaultProtocolConvert.bytes2string(body));
        return body;
    }

    /**
     * 获取指定字符串出现的次数
     *
     * @param srcText  源字符串
     * @param findText 要查找的字符串
     * @return
     * @author: zhouliang@edenep.net
     */
    public static int appearNumber(String srcText, String findText) {
        int count = 0;
        Pattern p = Pattern.compile(findText);
        Matcher m = p.matcher(srcText);
        while (m.find()) {
            count++;
        }
        return count;
    }

    /**
     * 博实结 协议分包处理
     *
     * @param byteBuf
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2019年7月15日16:16:04
     */
    private byte[] bsjUnpacked(ByteBuf byteBuf) {

        // TODO Auto-generated method stub
        byte[] tembody = new byte[MAX_BODY_LENGTH];
        while (true) {
            tembody[byteBuf.readerIndex()] = byteBuf.readByte();

            //当检测字符串中包含两个5e字符时,结束  之所以是两个因为头和尾都是5e
            String srcText = DefaultProtocolConvert.bytes2hexstring(tembody);
            if (appearNumber(srcText, "7e") == 2) {
                break;
            }
            //当缓存区所有的字节读取完毕 则返回
            if (0 == byteBuf.readableBytes()) {
                return null;
            }
        }

        byte[] body = DefaultProtocolConvert.subBytes(tembody, 0, byteBuf.readerIndex());
        //标记已读位置（实际相当于已经从缓存区读取过的数据不再读取）
        byteBuf.markReaderIndex();

        String tempbodystr = DefaultProtocolConvert.bytes2hexstring(body);
        tempbodystr = tempbodystr.substring(tempbodystr.indexOf("7e"), tempbodystr.length());

        tempbodystr = tempbodystr.substring(tempbodystr.indexOf("7e"), tempbodystr.lastIndexOf("7e") + "7e".length());
        tempbodystr = tempbodystr.replace("7d01", "7d");//转义
        tempbodystr = tempbodystr.replace("7d02", "7e");//转义
        body = DefaultProtocolConvert.hexstring2bytes(tempbodystr);
        return body;
    }

    /**
     * android 协议分包处理
     *
     * @param byteBuf
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年10月8日 下午2:57:29
     */
    private byte[] androidUnpacked(ByteBuf byteBuf) {

        // TODO Auto-generated method stub
        byte[] tembody = new byte[MAX_BODY_LENGTH];
        while (true) {
            tembody[byteBuf.readerIndex()] = byteBuf.readByte();

            //当检测字符串中包含两个5e字符时,结束  之所以是两个因为头和尾都是5e
            String srcText = DefaultProtocolConvert.bytes2hexstring(tembody);
            if (appearNumber(srcText, "5e") == 2) {
                break;
            }
            //当缓存区所有的字节读取完毕 则返回
            if (0 == byteBuf.readableBytes()) {
                return null;
            }
        }

        byte[] body = DefaultProtocolConvert.subBytes(tembody, 0, byteBuf.readerIndex());
        //标记已读位置（实际相当于已经从缓存区读取过的数据不再读取）
        byteBuf.markReaderIndex();

        String tempbodystr = DefaultProtocolConvert.bytes2hexstring(body);
        tempbodystr = tempbodystr.substring(tempbodystr.indexOf("5e"), tempbodystr.length());
        body = DefaultProtocolConvert.hexstring2bytes(tempbodystr);
        return body;
    }

    /**
     * 通天星 协议分包处理
     *
     * @param byteBuf
     * @return
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2019年8月5日10:56:07
     */
    private byte[] TTXUnpacked(ByteBuf byteBuf) {

        // TODO Auto-generated method stub
        byte[] tembody = new byte[MAX_BODY_LENGTH];
        while (true) {
            tembody[byteBuf.readerIndex()] = byteBuf.readByte();

            String srcText = DefaultProtocolConvert.bytes2hexstring(tembody);
            int headTag = srcText.indexOf("2a23");
            int endTag = srcText.indexOf("2a58");
            if (headTag >= 0 && endTag >= 0 && endTag > headTag) {
                break;
            }
            //当缓存区所有的字节读取完毕 则返回
            if (0 == byteBuf.readableBytes()) {
                return null;
            }
        }

        byte[] body = DefaultProtocolConvert.subBytes(tembody, 0, byteBuf.readerIndex());
        //标记已读位置（实际相当于已经从缓存区读取过的数据不再读取）
        byteBuf.markReaderIndex();

        String tempbodystr = DefaultProtocolConvert.bytes2hexstring(body);
        tempbodystr = tempbodystr.substring(tempbodystr.indexOf("2a23"), tempbodystr.length());
        body = DefaultProtocolConvert.hexstring2bytes(tempbodystr);
        return body;
    }

    /**
     * 其他默认协议 不做分包处理 简单粗暴取所有的数据
     *
     * @param byteBuf
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年7月19日 下午7:18:50
     */
    private byte[] others(ByteBuf byteBuf) {
        byte[] body = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(body);
        return body;
    }

    /**
     * 老智能盒子 处理粘包
     *
     * @param byteBuf
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年7月20日 下午5:11:13
     */
    private byte[] smartBoxUnpacked(ByteBuf byteBuf) {

        byte[] tembody = new byte[MAX_BODY_LENGTH];
        while (true) {
            tembody[byteBuf.readerIndex()] = byteBuf.readByte();
            //当检测到5d结尾字符时,结束
            if (DefaultProtocolConvert.bytes2hexstring(tembody).contains("5d")) {
                break;
            }
            //当缓存区所有的字节读取完毕 则返回
            if (0 == byteBuf.readableBytes()) {
                return null;
            }
        }

        byte[] body = DefaultProtocolConvert.subBytes(tembody, 0, byteBuf.readerIndex());
        //标记已读位置（实际相当于已经从缓存区读取过的数据不再读取）
        byteBuf.markReaderIndex();

        String tempbodystr = DefaultProtocolConvert.bytes2hexstring(body);
        tempbodystr = tempbodystr.substring(tempbodystr.indexOf("5b"), tempbodystr.length());
        body = DefaultProtocolConvert.hexstring2bytes(tempbodystr);
        return body;
    }

    /**
     * 易登盒子 分包处理
     *
     * @param byteBuf
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年7月19日 下午7:13:02
     */
    private byte[] edenepBoxUnpacked(ByteBuf byteBuf) {

        byte[] gbBaseByte = new byte[2];
        byteBuf.readBytes(gbBaseByte);
        byteBuf.resetReaderIndex();//重置
        int dataLength = DefaultProtocolConvert.hexstring2int(
                DefaultProtocolConvert.bytes2hexstring(DefaultProtocolConvert.subBytes(gbBaseByte, 1, 1)));
        byte[] body = new byte[dataLength];//数据长度
        if (byteBuf.readableBytes() < dataLength) {
            return null;
        }
        //从缓存区读取指定长度的字节到数组
        byteBuf.readBytes(body, 0, dataLength);

        //标记已读位置（实际相当于已经从缓存区读取过的数据不再读取）
        byteBuf.markReaderIndex();
        return body;
    }

    /**
     * 神州车载 7e 协议分包处理
     *
     * @param byteBuf
     * @return
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年8月11日 下午3:11:38
     */
    private byte[] szvehicleUnpacked(ByteBuf byteBuf) {

        byte[] gbBaseByte = new byte[5];
        byteBuf.readBytes(gbBaseByte);
        byteBuf.resetReaderIndex();//重置

        //取协议数据部分长度
        String attrs = DefaultProtocolConvert.byte2bitstring(gbBaseByte[3])
                + DefaultProtocolConvert.byte2bitstring(gbBaseByte[4]);
        String temp = DefaultProtocolConvert.fixHexStringOffset(attrs.substring(6, 16), 16);
        String temp1 = temp.substring(0, 8);
        String temp2 = temp.substring(8, 16);
        int dataLength = DefaultProtocolConvert.bytes2int(new byte[]{DefaultProtocolConvert.bit2byte(temp1),
                DefaultProtocolConvert.bit2byte(temp2)});

        byte[] body = new byte[dataLength + 15];//数据长度  + 15个基本长度
        if (byteBuf.readableBytes() < body.length) {
            return null;
        }
        //从缓存区读取指定长度的字节到数组
        byteBuf.readBytes(body, 0, body.length);

        //标记已读位置（实际相当于已经从缓存区读取过的数据不再读取）
        byteBuf.markReaderIndex();
        return body;
    }

    /**
     * 国标协议分包处理
     *
     * @param byteBuf
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年7月19日 下午7:13:22
     */
    private byte[] gbUnPacked(ByteBuf byteBuf) {

        //国标整个包长最小单位25; 基本长度24+数据单元0+最后一位校验码1
        if (byteBuf.readableBytes() < 25) {
            return null;
        }
        //取国标数据单位长度
        byte[] gbBaseByte = new byte[24];
        byteBuf.readBytes(gbBaseByte);
        byteBuf.resetReaderIndex();//重置
        int gbDataUnitLength = DefaultProtocolConvert.hexstring2int(
                DefaultProtocolConvert.bytes2hexstring(DefaultProtocolConvert.subBytes(gbBaseByte, 22, 2)));
        int gbDataLength = 24 + gbDataUnitLength + 1;

        byte[] body = new byte[gbDataLength];//24个基本长度+数据单元长度+1位校验码
        if (byteBuf.readableBytes() < gbDataLength) {
            return null;
        }
        byteBuf.readBytes(body, 0, gbDataLength);
        byteBuf.markReaderIndex();
        return body;
    }

    /**
     * 打印接收的错误数据
     *
     * @param byteBuf
     * @param startTagByte 接收到的协议前四字节
     * @param errorType    标准常量类型定义
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年7月19日 下午6:50:06
     */
    private void printErrorBody(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, byte[] startTagByte, StandardConstantResp errorType) {

        byte[] errorBody = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(errorBody);
        //清除缓冲区异常数据
        byteBuf.skipBytes(byteBuf.readableBytes());

        String remoteAddress = channelHandlerContext.channel().remoteAddress().toString();
        logger.error(String.format("客户端[%s],HeadTag Four Byte[%s],[%s],丢弃整条数据[%s]", remoteAddress, DefaultProtocolConvert.bytes2hexstring(startTagByte), errorType.getMessage(), DefaultProtocolConvert.bytes2hexstring(errorBody)));
    }

    /**
     * 获取合法的消息
     *
     * @param body
     * @return
     */
    private ReceiveMessage getAuthReceiveMessage(byte[] body, ProtocolType protocolType) throws Exception {

        ReceiveMessage message = new ReceiveMessage();

        String startTag = protocolType.getStartTag();
        //判断原始数据是否需要转义
        String escape = protocolType.getEscape();
        if (StringHelper.isNotBlank(escape) && !"null".equals(escape)) {
            String escapeData = DefaultProtocolConvert.bytes2hexstring(body);
            String escapeArr[] = escape.split(",");
            for (String str : escapeArr) {
                if (str.startsWith("{") && str.endsWith("}")) {
                    String oldStr = str.substring(1, str.indexOf(":"));
                    String newStr = str.substring(str.indexOf(":") + 1, str.length() - 1);
                    escapeData = escapeData.replace(oldStr, newStr);
                }
            }
            body = DefaultProtocolConvert.hexstring2bytes(escapeData);
        }
        message.setPort(port);
        message.setHeadTag(startTag);
        message.setEndTag(protocolType.getEndTag());

        message.setMsgByte(DefaultProtocolConvert.bytes2hexstring(body));

        String splitStr = protocolType.getSplitStr();
        message.setSplitStr(splitStr);
        String imei = null;
        String mid;
        String imeiClazzAndMethodPath = protocolType.getImeiJavaMethod();
        String midClazzAndMethodPath = protocolType.getMidJavaMethod();
        String imeiClazzPath = CollectUtil.getClazzPath(imeiClazzAndMethodPath);
        String imeiMethodPath = CollectUtil.getMethodPath(imeiClazzAndMethodPath);

        Object imeiO = Class.forName(imeiClazzPath).newInstance();
        Method imeiMethod = Class.forName(imeiClazzPath).getDeclaredMethod(imeiMethodPath, byte[].class);

        String midClazzPath = CollectUtil.getClazzPath(midClazzAndMethodPath);
        String midMethodPath = CollectUtil.getMethodPath(midClazzAndMethodPath);

        Object midO = Class.forName(midClazzPath).newInstance();
        Method midMethod = Class.forName(midClazzPath).getDeclaredMethod(midMethodPath, byte[].class);

        if (StringHelper.isBlank(splitStr) || "null".equals(splitStr)) {
            //不是通过分隔符的数据
            //判断协议是否存在
            byte[] midField = DefaultProtocolConvert.getChildArray(body, protocolType.getMidStartPosition(), protocolType.getMidSize());
            mid = String.valueOf(midMethod.invoke(midO, midField));
            if (!CollectRedisCacheService.midExists(startTag, port, mid)) {
                return null;
            }
            //特殊情况 处理
            if (protocolType.getImeiStartPosition().toString().equalsIgnoreCase("0") &&
                    protocolType.getImeiSize().toString().equalsIgnoreCase("0")) {
                String bodyStr = DefaultProtocolConvert.bytes2string(body);
                if ("232323".equalsIgnoreCase(protocolType.getStartTag())) {
                    bodyStr = bodyStr.substring(bodyStr.indexOf("{"), bodyStr.lastIndexOf("}") + 1);
                    Map<String, String> vmcMap = JSON.parseObject(bodyStr, Map.class);
                    if ("1000".equalsIgnoreCase(mid)) {
                        imei = JSON.parseObject(bodyStr, Map.class).get("IMEI").toString();
                    } else {
                        imei = CollectRedisCacheService.getVmcImeiByMid(vmcMap.get("Mid"));
                    }
                }
            } else {
                //判断是否授权
                byte[] imeiField = DefaultProtocolConvert.getChildArray(body, protocolType.getImeiStartPosition(), protocolType.getImeiSize());
                imei = String.valueOf(imeiMethod.invoke(imeiO, imeiField));
                if (!CollectRedisCacheService.checkImei(imei)) {
                    return null;
                }
            }
            message.setImei(imei);
            message.setMid(mid);
        } else {
            //通过分隔符处理的数据
            //获取转码之后的消息
            String bodyString = DefaultProtocolConvert.bytes2hexstring(body, 0, body.length);

            //获取转码之后的消息体
            String msgBody[] = new String[]{};
            try {
                msgBody = (bodyString.substring(bodyString.indexOf(startTag) + startTag.length(), bodyString.lastIndexOf(protocolType.getEndTag()))).split(protocolType.getSplitStr());
            } catch (Exception e) {
                logger.error("bodyString:" + bodyString);
                logger.error("", e);
            }
            imei = msgBody[protocolType.getImeiPosition()];
            byte[] imeiByte = DefaultProtocolConvert.hexstring2bytes(imei);
            imei = String.valueOf(imeiMethod.invoke(imeiO, imeiByte));
            if (!CollectRedisCacheService.checkImei(imei)) {
                return null;
            }
            mid = msgBody[protocolType.getMidPosition()];
            byte[] midByte = DefaultProtocolConvert.hexstring2bytes(mid);
            mid = String.valueOf(midMethod.invoke(midO, midByte));
            if (!CollectRedisCacheService.midExists(startTag, port, mid)) {
                return null;
            }

            message.setImei(imei);
            message.setMid(mid);
        }
        message.setIsForward(CollectRedisCacheService.getProtocolIsForward(startTag, port, mid));
        message.setForwardUrl(CollectRedisCacheService.getProtocolForwardUrl(startTag, port, mid));
        message.setCallBackClazz(CollectRedisCacheService.getProtocolCallBackClazz(startTag, port, mid));
        message.setSelfCheckingProtocol(CollectRedisCacheService.getProtocolSelfChecking(startTag, port, mid));
        //校验校验码
        if (!checkVerifyCode(body, message)) {
            return null;
        }
        //给所有的消息添加获取消息的时间
        message.setReceiveDateStr(DateHelper.formatTime(new Date()));
        return message;
    }

    /**
     * 校验校验码
     * 当协议类型中校验码位置为空时(后台未配置)则无需校验，此时返回true表示校验通过
     *
     * @param body
     * @return
     * @author: zl
     * @version: 2.0
     * @date: 2018年5月24日 下午6:15:42
     */
    private boolean checkVerifyCode(byte[] body, ReceiveMessage message) {

        //获取协议
        Map<String, Object> protocolMap = CollectRedisCacheService
                .getProtocolsFromCache(message.getHeadTag(), message.getPort(), message.getMid());

        if (protocolMap == null) {
            return false;
        }

        Protocol protocol = (Protocol) protocolMap.get("protocol");

        boolean verifySuccess = true;
        Integer verifycodePosition = protocol.getVerifycodePosition();//消息校验码位置
        if (null != verifycodePosition) {

            Integer verifyStartPosition = protocol.getVerifyStartPosition() == null ? 0 : protocol.getVerifyStartPosition();//消息 验证起始字节位置
            Integer verifyEndPosition = protocol.getVerifyEndPosition() == null ? 0 : protocol.getVerifyEndPosition();//验证字节总长度 ，从起始字节开始    负数代表到消息倒数第几位
            /**
             * 第一种情况verifyCount为正数即>0.则要验证的长度即为本身 verifyCount
             * 第二种情况verifyCount为负数，表示到总长度的倒数第几位。此时verifyCount=总长度-开始字节-倒数位数。
             **/
            Integer verifyCount = 0;
            if (verifyEndPosition.compareTo(0) < 0) {
                verifyCount = body.length - (verifyStartPosition + Math.abs(verifyEndPosition));//
            } else {
                verifyCount = verifyEndPosition - verifyStartPosition;
            }
            try {
                byte[] verifyBtye = DefaultProtocolConvert.subBytes(body, verifyStartPosition, verifyCount);
                String verifyClazzAndMethodPath = protocol.getVerifyJavaMethod();
                String verifyClazzPath = CollectUtil.getClazzPath(verifyClazzAndMethodPath);
                String verifyMethodPath = CollectUtil.getMethodPath(verifyClazzAndMethodPath);
                Object verifyClass = Class.forName(verifyClazzPath).newInstance();
                Method verifyMethod = Class.forName(verifyClazzPath).getDeclaredMethod(verifyMethodPath, byte[].class);
                int sumbyte = (int) verifyMethod.invoke(verifyClass, verifyBtye);

                int verifycode = 0;
                if (verifycodePosition.compareTo(0) < 0) {//若为负数，则表示从字节数组的末尾开始第N个字节，N为verifycodePosition的绝对值
                    verifycode = body[body.length + verifycodePosition];
                }
                if (verifycode != sumbyte) {
                    logger.error(String.format("IMEI[%s]校验码校验不通过,消息体中校验码：body[%s]=%s;程序计算校验码：%s,hex=%s。原始数据[%s]", message.getImei(), (body.length + verifycodePosition + 1), body[body.length + verifycodePosition], sumbyte,
                            Integer.toHexString(sumbyte),
                            DefaultProtocolConvert.bytes2hexstring(body)));
                    verifySuccess = false;
                }
            } catch (Exception e) {
                verifySuccess = false;
                logger.error(String.format("IMEI[%s]校验码校验异常,异常信息：%s", message.getImei(), e.getMessage()));
            }
        }
        return verifySuccess;
    }

    /**
     * 去命中所有的消息头
     *
     * @param
     * @return
     * @since 2017/11/13  20:51
     */
    private ProtocolType getProtocolType(byte[] body) {
        ProtocolType protocolType = null;
        int i = 4;
        while (protocolType == null && i > 0) {
            String startTag = DefaultProtocolConvert.bytes2hexstring(body, 0, i);
            protocolType = CollectRedisCacheService.getProtocolTypesFromCache(startTag, port);
            i--;
        }
        return protocolType;
    }

    public static void main(String[] args) {

        String srcText = "2a233030377c303235303138337c323031392d30382d35202030393a32353a33377c3130362e3336373730367c32372e3736393232307c307c307c302e30307c313038382e32377c337c3830353331383738377c307c307c307c302e307c302e307c302e307c302e307c31317c2a58";

        int headTag = srcText.indexOf("2a23");
        int endTag = srcText.indexOf("2a58");
        if (headTag >= 0 && endTag >= 0 && endTag > headTag) {
            System.out.println("协议正常");
        }
        System.out.println(headTag);
        System.out.println(endTag);

    }

    /**
     * 打印入参信息
     *
     * @param byteBuf
     */
    private void printReqInfo(ByteBuf byteBuf) {
        //取前面四个字节  用于取协议类型
        byte[] startTagByte = new byte[HEAD_LENGTH];
        byteBuf.readBytes(startTagByte);
        byteBuf.resetReaderIndex();
        byte[] errorBody = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(errorBody);
        logger.info("socket 接收到消息: " + DefaultProtocolConvert.bytes2hexstring(errorBody));
        Map<String, String> parmMap = new HashMap<>();
        pushMq(CollectContants.COLLECT_SEND_TERMINAL_MSG_QUEUE_COMMAND, parmMap, null);

    }

    private void pushMq(String bizType, Map<String, String> parmMap, String mqId) {
        try {
            MqClientManager.publish(bizType, parmMap, mqId);
        } catch (Exception e) {
            logger.error("消息入队列失败: " + e);
        }
    }
}
