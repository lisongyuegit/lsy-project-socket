package lsy.project.socket.api.netty;

import com.alibaba.fastjson.JSON;
import com.lsy.base.date.DateHelper;
import com.lsy.base.string.StringHelper;
import com.lsy.base.utils.ConvertHelper;
import com.lsy.rabbitmq.client.MqClientManager;
import lsy.project.socket.api.cache.CollectRedisCacheService;
import lsy.project.socket.api.contants.CollectContants;
import lsy.project.socket.api.convert.DefaultProtocolConvert;
import lsy.project.socket.api.entity.ProtocolType;
import lsy.project.socket.api.model.ReceiveMessage;
import lsy.project.socket.api.util.CollectUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * UDP消息处理类
 *
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public class NettyUDPServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    static Logger logger = LoggerFactory.getLogger(NettyUDPServerHandler.class);
    private int port;

    public NettyUDPServerHandler(int port) {
        super();
        this.port = port;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        super.handlerAdded(ctx);
        logger.info("UDP通道已经连接");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {

        // TODO Auto-generated method stub
        String req = packet.content().toString(CharsetUtil.UTF_8);
        ByteBuf byteBuf = packet.content();
        //判断消息长度，如果小于1则返回
        if (byteBuf.readableBytes() < 1) {
            return;
        }
        //读到的长度，满足要求，则把传送过来的数据取出来
        byte[] body = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(body);

        //对body做初步解析，并把body组装成ReceiveMessage
        List<ReceiveMessage> receiveMessages = getReceiveMessage(body);
        logger.info(String.format("IP[%s]端口[%s]原始消息内容[%s]", packet.sender().getHostString(), packet.sender().getPort(), req));

        // 消息处理。。。。。  
        for (int i = 0; i < receiveMessages.size(); i++) {
            logger.info(i + ":解析后消息内容=" + JSON.toJSONString(receiveMessages.get(i)));
            try {
                MqClientManager.publish(CollectContants.COLLECT_AUTH_MSG_QUEUE_COMMAND, ConvertHelper.objectToMapString(receiveMessages.get(i)), null);
            } catch (Exception e) {
                logger.error("消息入队列失败" + e);
            }
        }

        // 回复一条信息给客户端
        /*ctx.writeAndFlush(new DatagramPacket(
        Unpooled.copiedBuffer("Hello，我是Server，我的时间戳是"+System.currentTimeMillis()
                        , CharsetUtil.UTF_8)
                        , packet.sender())).sync();*/
    }

    /**
     * 将接受到数据转化为平台的消息格式
     *
     * @param body
     * @return
     * @throws Exception
     * @author:
     * @version: 2.0
     * @date: 2018 2018年5月3日 下午9:16:33
     */
    private List<ReceiveMessage> getReceiveMessage(byte[] body) throws Exception {
        ReceiveMessage message = new ReceiveMessage();
        ProtocolType protocolType = getProtocolType(body);
        //判断协议类型是否存在
        if (protocolType == null) {
            return null;
        }

        String startTag = protocolType.getStartTag();//协议头
        String endTag = protocolType.getEndTag();//协议尾
        String splitStr = protocolType.getSplitStr();//字段分隔符
        String splitStrBatch = protocolType.getSplitStrBatch();//批量上传分隔符

        logger.info("startTag:" + startTag);

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

        String hexbody = DefaultProtocolConvert.bytes2hexstring(body);
        hexbody = hexbody.replaceFirst(startTag, "").replace(endTag, "");

        List<ReceiveMessage> receiveMessages = new ArrayList<>();
        ReceiveMessage receiveMessage = null;

        //判断协议类型中批量分隔符是否为空，若为空 则赋值一个不可能的字符 作为分隔符
        if (StringHelper.isBlank(splitStrBatch) || "null".equals(splitStrBatch)) {
            splitStrBatch = "~^";
        }
        String[] batchBody = hexbody.split(splitStrBatch);
        for (String msgByte : batchBody) {
            msgByte = String.format("%s%s%s", startTag, msgByte, endTag);
            receiveMessage = new ReceiveMessage();
            receiveMessage.setPort(port);
            receiveMessage.setHeadTag(startTag);
            receiveMessage.setEndTag(endTag);
            receiveMessage.setMsgByte(msgByte);
            receiveMessage.setSplitStr(splitStr);
            //获取协议对应的IMEI（设备唯一码） 和 MID（协议名称）
            getImeiAndMid(DefaultProtocolConvert.hexstring2bytes(msgByte), receiveMessage, protocolType);
            if (null == receiveMessage.getMid() || null == receiveMessage.getImei()) return null;
            receiveMessage.setIsForward(CollectRedisCacheService.getProtocolIsForward(startTag, port, message.getMid()));
            receiveMessage.setForwardUrl(CollectRedisCacheService.getProtocolForwardUrl(startTag, port, message.getMid()));
            receiveMessage.setCallBackClazz(CollectRedisCacheService.getProtocolCallBackClazz(startTag, port, message.getMid()));
            receiveMessage.setReceiveDateStr(DateHelper.formatTime(new Date()));
            receiveMessages.add(receiveMessage);
        }

        return receiveMessages;
    }

    /**
     * 获取协议对应的IMEI（设备唯一码） 和 MID（协议名称）
     *
     * @param body         上传原始字节
     * @param message      待封装的消息体
     * @param protocolType 协议类型
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018年5月4日 上午10:48:34
     */
    private void getImeiAndMid(byte[] body, ReceiveMessage message, ProtocolType protocolType) throws InstantiationException, IllegalAccessException,
            ClassNotFoundException, NoSuchMethodException, InvocationTargetException {

        String splitStr = protocolType.getSplitStr();
        String startTag = protocolType.getStartTag();

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

        String imei;
        String mid;
        if (StringHelper.isBlank(splitStr) || "null".equals(splitStr)) {
            //不是通过分隔符的数据
            //判断协议是否存在
            byte[] midField = DefaultProtocolConvert.getChildArray(body, protocolType.getMidStartPosition(), protocolType.getMidSize());
            mid = String.valueOf(midMethod.invoke(midO, midField));
            if (!CollectRedisCacheService.midExists(startTag, port, mid)) {
                return;
            }
            //判断是否授权
            byte[] imeiField = DefaultProtocolConvert.getChildArray(body, protocolType.getImeiStartPosition(), protocolType.getImeiSize());
            imei = String.valueOf(imeiMethod.invoke(imeiO, imeiField));
            if (!CollectRedisCacheService.checkImei(imei)) {
                return;
            }
            message.setImei(imei);
            message.setMid(mid);

        } else {
            //通过分隔符处理的数据
            //获取转码之后的消息
            String bodyString = DefaultProtocolConvert.bytes2hexstring(body, 0, body.length);
            //获取转码之后的消息体
            String msgBody[] = (bodyString.substring(bodyString.indexOf(startTag) + startTag.length(), bodyString.lastIndexOf(protocolType.getEndTag()))).split(protocolType.getSplitStr());
            imei = msgBody[protocolType.getImeiPosition()];
            byte[] imeiByte = DefaultProtocolConvert.hexstring2bytes(imei);
            imei = String.valueOf(imeiMethod.invoke(imeiO, imeiByte));
            if (!CollectRedisCacheService.checkImei(imei)) {
                return;
            }
            mid = msgBody[protocolType.getMidPosition()];
            byte[] midByte = DefaultProtocolConvert.hexstring2bytes(mid);
            mid = String.valueOf(midMethod.invoke(midO, midByte));
            if (!CollectRedisCacheService.midExists(startTag, port, mid)) {
                return;
            }

            message.setImei(imei);
            message.setMid(mid);
        }
        return;
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

        String a = "aab";
        String d = null;
        String ab[] = a.split(d == null ? "~^" : d);

        System.out.println(ab.length);
    }
}
