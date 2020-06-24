package com.yidcloud.web.netty;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.lsy.base.date.DateHelper;
import com.lsy.base.string.StringHelper;
import com.lsy.base.utils.ConvertHelper;
import com.lsy.rabbitmq.client.MqClientManager;
import com.yidcloud.api.contants.CollectContants;
import com.yidcloud.api.dto.ImeiChannelDto;
import com.yidcloud.api.enums.BooleanCharEnum;
import com.yidcloud.api.enums.SelfCheckingEnum;
import com.yidcloud.web.util.VendingMachineUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yidcloud.web.cache.CollectRedisCacheService;
import com.yidcloud.web.callback.AbstractCallback;
import com.yidcloud.web.callback.SendCommendCallBack;
import com.yidcloud.web.model.ReceiveMessage;
import com.yidcloud.web.util.NettyChannelMap;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 接受到的消息处理类
 * 
 * @copyright Copyright (c) 2017
 * @author 胡洪瑜 huhongyu@edenep.net
 * @version 2.0
 * @since
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<ReceiveMessage> {

    static Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    //线程安全心跳失败计数器
    private AtomicInteger unRecPingTimes = new AtomicInteger(0);

    public static String getIPString(ChannelHandlerContext ctx) {
        String socketString = ctx.channel().remoteAddress().toString();
        int colonAt = socketString.indexOf(":");
        String ipString = socketString.substring(1, colonAt);
        return ipString;
    }

    public static String getRemoteAddress(ChannelHandlerContext ctx) {
        return ctx.channel().remoteAddress().toString();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        //往channel map中添加channel信息
        String key = String.format("%s_%s", getIPString(ctx), ctx.channel().id().asLongText());
        NettyChannelMap.put(key, ctx.channel());
        logger.info(String.format("client%s 接入连接,当前连接数%s", getRemoteAddress(ctx), NettyChannelMap.getAll().size()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //删除Channel Map中的失效Client
        String key = String.format("%s_%s", getIPString(ctx), ctx.channel().id().asLongText());
        NettyChannelMap.remove(key);
        logger.info(String.format("client%s 断开连接,当前连接数%s", getRemoteAddress(ctx), NettyChannelMap.getAll().size()));
        //销毁连接
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ReceiveMessage msg) throws Exception {

        /**
         * One step: 合法正常的消息进行数据转发,执行条件 转发客户端连接程序已连上
         */
        if ("2323".equals(msg.getHeadTag()) && NettyClient.isForwardConnected) {
            NettyClient.dataForward(msg);
        }
        
//        PropertiesHelper helper = new PropertiesHelper("system.properties");
//        boolean isForwardGb = "True".equalsIgnoreCase(helper.getStringProperty("IsForward_gb")==null?"false":helper.getStringProperty("IsForward_gb"));
//        if ("2323".equals(msg.getHeadTag()) && NettyClient.isForwardConnected) {
//        	int port = helper.getIntegerProperty("Forward.server.port.gb");
//        	String serverAddress = helper.getStringProperty("Forward.server.address.gb");
//        	new NettyClient(port,serverAddress);
//            NettyClient.startGb();
//            NettyClient.dataForward(msg);
//        }
        
        /**
         * Two step: 添加通道信息到内存, 记录缓存信息
         */
        operImeiChannel(ctx, msg);

        /**
         * Three step: 合法消息入队列 ,将计数器重新置为零
         */
        try {
            unRecPingTimes.set(0);
            
            MqClientManager.publish(CollectContants.COLLECT_AUTH_MSG_QUEUE_COMMAND, ConvertHelper.objectToMapString(msg), null);
            logger.info(String.format("客户端IP[%s] 合法消息入队列，SERVER send msg:%s", getRemoteAddress(ctx), msg.toString()));
            
        } catch (Exception e) {
            logger.error("消息入队列失败" + e);
        }
        
        /**
         * Four step: 是否需要回写
         */
        String callBackClazz = msg.getCallBackClazz();
        if (!StringHelper.isBlank(callBackClazz)) {
            try {
                AbstractCallback callback = (AbstractCallback) ClassLoader.getSystemClassLoader().loadClass(callBackClazz).newInstance();
                callback.callBack(ctx, msg);
            } catch (Exception e) {
                logger.error("回写失败", e);
            }
        }

        /**
         * Five step: 检查平台是否有指令需要下发到终端
         */
        SendCommendCallBack scCallBack = new SendCommendCallBack();
        scCallBack.callBack(ctx, msg);

        /**
         * 售货机特殊处理
         */
        if(msg.getHeadTag().equalsIgnoreCase("232323")){
            handVendingMachine(msg);
        }
    }

    private void handVendingMachine(ReceiveMessage msg) {
        Map<String, String> receiveMap = VendingMachineUtil.getReceiveValueMap(msg);
        switch (msg.getMid()){
            case "3000":
                //订单出货：售货机收到命定回复回写到缓存
                CollectRedisCacheService.editVendingMachineOrder(msg.getMid(),msg.getImei(),receiveMap);
                break;
            default:
                break;
        }
    }

    /**
     * 添加通道信息到内存，2 记录缓存信息
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: 
     * @version: 2.0
     * @date: 2018年9月20日 下午11:00:48
     * @param ctx
     * @param msg
     */
    private void operImeiChannel(ChannelHandlerContext ctx, ReceiveMessage msg) {
        
        ImeiChannelDto imeiChannelDto = CollectRedisCacheService.getImeiChannel(msg.getImei());
        
        //通道客户端IP-内存key
        String unique_client = String.format("%s_%s", getIPString(ctx), ctx.channel().id().asLongText());
        
        //通道客户端IP 不存在 ，TODO 将客户端IP添加到内存中,将IMEI通道信息写入缓存
        if (!NettyChannelMap.judgeExist(unique_client)) {
            NettyChannelMap.getImeiMap().put(msg.getImei(), unique_client);
            if(null == imeiChannelDto) {
                imeiChannelDto = new ImeiChannelDto();
                imeiChannelDto.setImei(msg.getImei());
            }
            imeiChannelDto.setRemote(getRemoteAddress(ctx));//设置客户端IP
            imeiChannelDto.setIsOnline(BooleanCharEnum.TRUE.getValue());//设置为在线状态
            imeiChannelDto.setOnlineTime(DateHelper.formatDate(new Date(), DateHelper.PATTERN_TIME));//设置上线时间
            imeiChannelDto.setHeartTime(
                    msg.getSelfCheckingProtocol() == SelfCheckingEnum.HEART_SELFCHECKING.getValue()
                    ? DateHelper.formatDate(new Date(), DateHelper.PATTERN_TIME)
                            : imeiChannelDto.getHeartTime());//设置最近一次心跳时间
            imeiChannelDto.setReportTime(
                    msg.getSelfCheckingProtocol() == SelfCheckingEnum.REPORTDATA_SELFCHECKING.getValue()
                    ? DateHelper.formatDate(new Date(), DateHelper.PATTERN_TIME)
                            : imeiChannelDto.getReportTime());//设置最近一次上传数据时间
            CollectRedisCacheService.setImeiChannel(imeiChannelDto);
        }else {
            
            //通道客户端IP已存在 且协议需进行自检记录 ，TODO 将IMEI通道信息写入缓存
            if(msg.getSelfCheckingProtocol()>0 && null != imeiChannelDto) {
                imeiChannelDto.setHeartTime(
                        msg.getSelfCheckingProtocol() == SelfCheckingEnum.HEART_SELFCHECKING.getValue()
                        ? DateHelper.formatDate(new Date(), DateHelper.PATTERN_TIME)
                                : imeiChannelDto.getHeartTime());//设置最近一次心跳时间
                imeiChannelDto.setReportTime(
                        msg.getSelfCheckingProtocol() == SelfCheckingEnum.REPORTDATA_SELFCHECKING.getValue()
                        ? DateHelper.formatDate(new Date(), DateHelper.PATTERN_TIME)
                                : imeiChannelDto.getReportTime());//设置最近一次上传数据时间
                CollectRedisCacheService.setImeiChannel(imeiChannelDto);
            }
        }
        
    }

    /**
     * 事件触发器，该处用来处理客户端空闲超时,发送心跳维持连接。
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                /*读超时*/
                unRecPingTimes.getAndIncrement();
                logger.info(String.format("客户端[%s_%s]未发送数据,以致服务器端(READER_IDLE 读超时),当前超时次数%s", getIPString(ctx), ctx.channel().id().asLongText(),
                        unRecPingTimes.get()));
                //客户端未进行ping心跳发送的次数等于3,断开此连接
                if (unRecPingTimes.intValue() == 3) {
                    //                      ctx.disconnect();
                    ctx.close();
                    logger.info(String.format("客户端[%s_%s]空闲超时，服务器主动关闭此连接....", getIPString(ctx), ctx.channel().id().asLongText()));
                }
            } else if (event.state() == IdleState.WRITER_IDLE) {
                /*服务端写超时*/
                logger.info(String.format("客户端[%s_%s]未收到服务器发送数据,服务器端(WRITER_IDLE 写超时),当前超时次数", getIPString(ctx), ctx.channel().id().asLongText()));
            } else if (event.state() == IdleState.ALL_IDLE) {
                /*总超时*/
                logger.info("服务器端 读写都超时");
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("", cause);
        ctx.close();
    }
}
