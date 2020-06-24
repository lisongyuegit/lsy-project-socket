package com.yidcloud.web.netty;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lsy.base.date.DateHelper;
import com.lsy.base.result.ResultVo;
import com.lsy.base.string.StringHelper;
import com.lsy.base.utils.ConvertHelper;
import com.lsy.rabbitmq.client.MqClientManager;
import com.yidcloud.api.contants.CollectContants;
import com.yidcloud.api.dto.FaxReportDto;
import com.yidcloud.api.entity.ProtocolType;
import com.yidcloud.api.enums.BooleanCharEnum;
import com.yidcloud.web.cache.CollectRedisCacheService;
import com.yidcloud.web.cache.RecycleBoxAlermCache;
import com.yidcloud.web.convert.DefaultProtocolConvert;
import com.yidcloud.web.model.ReceiveMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Http消息处理类
 *
 * @copyright: Copyright (c) 2018
 * @company: 易登科技
 * @author: zhouliang@edenep.net
 * @version: 2.0
 * @date: 2018年8月16日 11:36:10
 */
public class NettyHttpServerHandler extends SimpleChannelInboundHandler<Object> {

    static Logger logger = LoggerFactory.getLogger(NettyHttpServerHandler.class);

    private int port;

    private boolean isPost;//是否Post请求

    private String reqCmd;//请求命定

    private byte[] sbody = new byte[0];

    public NettyHttpServerHandler(int port) {
        super();
        this.port = port;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        super.handlerAdded(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        //保存接口请求参数
        Map<String, String> parmMap = new HashMap<>();

        //处理http请求，并且通过获取uri保存请求路由
        if (msg instanceof HttpRequest) {
            DefaultHttpRequest request = (DefaultHttpRequest) msg;
            int sublen = request.uri().indexOf("?") == -1 ? request.uri().length() : request.uri().indexOf("?");
            reqCmd = request.uri().substring(1, sublen);
            if (HttpMethod.GET == request.method()) {
                getGetParameter(request.uri(), parmMap);
                doGet(parmMap, ctx);
            }
            isPost = request.method().equals(HttpMethod.POST);
        }

        //处理post接口请求
        if (isPost) {
            if(msg instanceof LastHttpContent){
                LastHttpContent content = (LastHttpContent) msg;
                ByteBuf byteBuf = content.content();
                byte[] body = new byte[byteBuf.readableBytes()];
                byteBuf.readBytes(body);
                sbody = DefaultProtocolConvert.add(sbody, body);
                getPostParameter(sbody, parmMap);
                doPost(parmMap, ctx);
            }else if (msg instanceof HttpContent) {
                HttpContent content = (HttpContent) msg;
                ByteBuf byteBuf = content.content();
                byte[] body = new byte[byteBuf.readableBytes()];
                byteBuf.readBytes(body);
                sbody = DefaultProtocolConvert.add(sbody, body);
            }
        }
    }

    /**
     * 处理post请求
     *
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018年8月16日 下午3:20:45
     * @param parmMap
     * @param ctx
     * @throws UnsupportedEncodingException
     */
    private void doPost(Map<String, String> parmMap, ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        logger.info("处理post请求" + reqCmd);
        switch (reqCmd) {
            //飞安信卡片机点位上传
            case "faxReport":
                faxReport(parmMap, ctx);
                break;
            //远程扫码垃圾箱开门
            case "openTrash":
                openTrash(parmMap, ctx);
                break;
            //售货机服务器主动检测心跳
            case "checkVcmHeart":
                checkVcmHeart(parmMap, ctx);
                break;
            //售货机服务器主动发起出货命定
            case "checkVCM3000":
                checkVCM3000(parmMap, ctx);
                break;
            //售货机参数设置
            case "checkVCM6000":
                //售货机参数设置
            case "checkVCM6001":
                checkVCM6001(parmMap, ctx);
                break;
            //远程清除货道故障并测试货道
            case "checkVCM6002":
                checkVCM6002(parmMap, ctx);
                break;
            default:
                break;
        }
    }

    /**
     * 飞安信卡片机点位上传-支持批量
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年9月28日 下午9:30:37
     * @param parmMap
     * @param ctx
     * @throws UnsupportedEncodingException
     */
    private void faxReport(Map<String, String> parmMap, ChannelHandlerContext ctx)
            throws UnsupportedEncodingException {
        String data = parmMap.get("data");
        if (null == data) {
            writeHttpResponse(false, ",data数据为空或者为null", ctx);
            return;
        }

        ProtocolType protocolType = CollectRedisCacheService.getProtocolTypesFromCache("2a", port);
        if (null == protocolType) {
            writeHttpResponse(false, ",找不到对应的协议类型", ctx);
            return;
        }
        List<ReceiveMessage> receiveMessages = new ArrayList<>();
        ReceiveMessage receiveMessage = null;

        List<FaxReportDto> list = JSONArray.parseArray(data, FaxReportDto.class);
        for (FaxReportDto faxReportDto : list) {

            String hexMsgByte = DefaultProtocolConvert.stringToASCII(faxReportDto.toString(), faxReportDto.toString().length(), null);
            String msgByte = String.format("%s%s%s", protocolType.getStartTag(), hexMsgByte, protocolType.getEndTag());
            receiveMessage = new ReceiveMessage();
            receiveMessage.setPort(port);
            receiveMessage.setHeadTag(protocolType.getStartTag());
            receiveMessage.setEndTag(protocolType.getEndTag());
            receiveMessage.setMsgByte(msgByte);
            receiveMessage.setSplitStr(protocolType.getSplitStr());
            receiveMessage.setMid(faxReportDto.getMid());
            receiveMessage.setImei(faxReportDto.getTnumber());
            receiveMessage
                    .setCallBackClazz(CollectRedisCacheService.getProtocolCallBackClazz(protocolType.getStartTag(), port, faxReportDto.getMid()));
            receiveMessage.setIsForward(CollectRedisCacheService.getProtocolIsForward(protocolType.getStartTag(), port, faxReportDto.getMid()));
            receiveMessage.setForwardUrl(CollectRedisCacheService.getProtocolForwardUrl(protocolType.getStartTag(), port, faxReportDto.getMid()));
            receiveMessage .setReceiveDateStr(DateHelper.formatTime(new Date()));
            receiveMessages.add(receiveMessage);
        }

        // 消息处理。。。。。
        for (int i = 0; i < receiveMessages.size(); i++) {
            logger.info(i + ":解析后消息内容=" + JSON.toJSONString(receiveMessages.get(i)));
            try {
                MqClientManager.publish(CollectContants.COLLECT_AUTH_MSG_QUEUE_COMMAND, ConvertHelper.objectToMapString(receiveMessages.get(i)),
                        null);
                writeHttpResponse(true, null, ctx);
            } catch (Exception e) {
                writeHttpResponse(false, "消息入队列失败", ctx);
                logger.error("消息入队列失败" + e);
            }
        }
    }

    /**
     * 扫码远程开垃圾箱门
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年9月28日 下午9:31:14
     * @param parmMap
     * @param ctx
     * @throws UnsupportedEncodingException
     * @throws Exception
     */
    private void openTrash(Map<String, String> parmMap, ChannelHandlerContext ctx)
            throws Exception {
        String imei = parmMap.get("imei");
        String qcode = parmMap.get("qcode");
        if (StringHelper.isBlank(imei)) {
            writeHttpResponse(false, ",imei为空", ctx);
            return;
        }
        if (StringHelper.isBlank(qcode)) {
            writeHttpResponse(false, ",qcode二维码为空", ctx);
            return;
        }

        //组装mq 广播消息参数  发布消息
        parmMap.put(CollectContants.HEADTAG, "fc");
        parmMap.put(CollectContants.ENDTAG, "cf");
        parmMap.put(CollectContants.MID, "44");
        parmMap.put(CollectContants.CMD, "FC44");
        //存在报警信息 直接回复错误，禁止开门
        if(RecycleBoxAlermCache.isExists(imei)) {
            parmMap.put(CollectContants.ISSUCCRSS, BooleanCharEnum.FALSE.getValue());
        }else {
            parmMap.put(CollectContants.ISSUCCRSS, BooleanCharEnum.TRUE.getValue());
        }
        MqClientManager.publish(CollectContants.COLLECT_SEND_TERMINAL_MSG_QUEUE_COMMAND, parmMap, null);

        //http 请求响应
        writeHttpResponse(true, null, ctx);
    }

    private void checkVcmHeart(Map<String, String> parmMap, ChannelHandlerContext ctx)
            throws Exception {
        String imei = parmMap.get("imei");
        String mid = parmMap.get("mid");
        if (StringHelper.isBlank(imei)) {
            writeHttpResponse(false, ",imei设备号为空", ctx);
            return;
        }
        //组装mq 广播消息参数  发布消息
        parmMap.put(CollectContants.HEADTAG, DefaultProtocolConvert.string2hexstring("###"));
        parmMap.put(CollectContants.ENDTAG, DefaultProtocolConvert.string2hexstring("&&&"));
        parmMap.put(CollectContants.CMD, "2002");
        parmMap.put(CollectContants.MID, mid);
        parmMap.put(CollectContants.IMEI, imei);
        MqClientManager.publish(CollectContants.COLLECT_SEND_TERMINAL_MSG_QUEUE_COMMAND, parmMap, null);

        //服务器发送 心跳（在线保持、握手）,检测机器是否在线
        Map<String,String> heartMap = CollectRedisCacheService.getVendingMachineHeart("2001",imei);
        if(null == heartMap || heartMap.isEmpty()){
            writeHttpResponse(false, "设备处于离线状态,无法操作", ctx);
            return;
        }

        Date requestTime = new Date();
        Date d1 = DateHelper.parseString(heartMap.get("date"));
        long time = DateHelper.getDateMiliDispersion(requestTime,d1);
        if(time/60000>3){
            writeHttpResponse(false, "设备处于离线状态"+heartMap.get("date")+",,time="+time, ctx);
            return;
        }
        //http 请求响应
        writeHttpResponse(true, null, ctx);
    }

    private void checkVCM3000(Map<String, String> parmMap, ChannelHandlerContext ctx)
            throws Exception {

        ResultVo resultVo = new ResultVo();
        String cmd = "3000";
        String imei = parmMap.get("imei");
        if (StringHelper.isBlank(imei)) {
            resultVo.setError_no(-1);
            resultVo.setError_info("imei设备号为空");
            writeHttpResponseVCM(resultVo, ctx);
            return;
        }

        String slot = parmMap.get("Slot");
        if (StringHelper.isBlank(slot)) {
            resultVo.setError_no(-1);
            resultVo.setError_info("货道号不能为空");
            writeHttpResponseVCM(resultVo, ctx);
            return;
        }

        //服务器发送 心跳（在线保持、握手）,检测机器是否在线
        Map<String,String> heartMap = CollectRedisCacheService.getVendingMachineHeart("2001",imei);
        if(null == heartMap || heartMap.isEmpty()){
            resultVo.setError_no(-1);
            resultVo.setError_info("设备处于离线状态,无法操作");
            writeHttpResponseVCM(resultVo, ctx);
            return;
        }

        Date requestTime = new Date();
        Date d1 = DateHelper.parseString(heartMap.get("date"));
        long time = DateHelper.getDateMiliDispersion(requestTime,d1);
        if(time/60000>3){
            resultVo.setError_no(-1);
            resultVo.setError_info("设备处于离线状态"+heartMap.get("date")+",,time="+time);
            writeHttpResponseVCM(resultVo, ctx);
            return;
        }
        //下发出货命定
        parmMap.put(CollectContants.HEADTAG, DefaultProtocolConvert.string2hexstring("###"));
        parmMap.put(CollectContants.ENDTAG, DefaultProtocolConvert.string2hexstring("&&&"));
        parmMap.put(CollectContants.CMD, cmd);
        parmMap.put("TimeSp", StringHelper.isBlank(parmMap.get("TimeSp"))?System.currentTimeMillis()/1000+"":parmMap.get("TimeSp"));
        MqClientManager.publish(CollectContants.COLLECT_SEND_TERMINAL_MSG_QUEUE_COMMAND, parmMap, null);

        //循环等待回复结果，等待时间30秒
        boolean replay = false;
        while (!replay){


            time = DateHelper.getDateMiliDispersion(new Date(),requestTime);
            if(time/1000>30){
                resultVo.setError_no(-1);
                resultVo.setError_info("售货机出货超时");
                writeHttpResponseVCM(resultVo, ctx);
                return;
            }

            Map<String,String> orderMap = CollectRedisCacheService.getVendingMachineOrder(imei,parmMap.get("OrderNo"),null);
            String cmd4000 = orderMap.get("4000");
            if(StringHelper.isBlank(cmd4000)){
                continue;
            }

            JSONObject jsonObject = JSON.parseObject(cmd4000,JSONObject.class);
            String result = jsonObject.getString("Resault");
            String error = jsonObject.getString("Error");
            String replay4000 = jsonObject.getString("date");
            Date date4000 = DateHelper.parseString(replay4000);
            long replay4000time  = DateHelper.getDateMiliDispersion(d1,date4000);
            if(replay4000time>0){
                continue;
            }
            replay = true;
            if("0".equalsIgnoreCase(result) && "0".equalsIgnoreCase(error)){
                writeHttpResponseVCM(resultVo, ctx);
                return;
            }
            resultVo.setError_no(1);
            resultVo.setError_info("售货机出货异常");
            resultVo.setResult("result",result);
            resultVo.setResult("error",error);
            logger.info("result=="+result+"===error=="+error+"--"+JSON.toJSONString(resultVo.getResult()));

            writeHttpResponseVCM(resultVo, ctx);
        }
    }

    private void checkVCM6001(Map<String, String> parmMap, ChannelHandlerContext ctx)
            throws Exception {
        String imei = parmMap.get("imei");
        if (StringHelper.isBlank(imei)) {
            writeHttpResponse(false, ",imei设备号为空", ctx);
            return;
        }
        //服务器发送 心跳（在线保持、握手）,检测机器是否在线
        Map<String,String> heartMap = CollectRedisCacheService.getVendingMachineHeart("2001",imei);
        if(null == heartMap || heartMap.isEmpty()){
            writeHttpResponse(false, "设备处于离线状态,设置失败", ctx);
            return;
        }

        Date requestTime = new Date();
        Date d1 = DateHelper.parseString(heartMap.get("date"));
        long time = DateHelper.getDateMiliDispersion(requestTime,d1);
        if(time/60000>3){
            writeHttpResponse(false, "设备处于离线状态,设置失败"+heartMap.get("date")+",,time="+time, ctx);
            return;
        }

        //组装mq 广播消息参数  发布消息
        parmMap.put(CollectContants.CMD, parmMap.get("cmd")==null?"6001":parmMap.get("cmd"));
        parmMap.put(CollectContants.HEADTAG, DefaultProtocolConvert.string2hexstring("###"));
        parmMap.put(CollectContants.ENDTAG, DefaultProtocolConvert.string2hexstring("&&&"));
        parmMap.put("TimeSp", StringHelper.isBlank(parmMap.get("TimeSp"))?System.currentTimeMillis()/1000+"":parmMap.get("TimeSp"));
        MqClientManager.publish(CollectContants.COLLECT_SEND_TERMINAL_MSG_QUEUE_COMMAND, parmMap, null);
        //http 请求响应
        writeHttpResponse(true, null, ctx);
    }

    private void checkVCM6002(Map<String, String> parmMap, ChannelHandlerContext ctx) throws Exception {
        String imei = parmMap.get("imei");
        if (StringHelper.isBlank(imei)) {
            writeHttpResponse(false, ",imei设备号为空", ctx);
            return;
        }
        //组装mq 广播消息参数  发布消息
        parmMap.put(CollectContants.CMD, "6002");
        parmMap.put(CollectContants.HEADTAG, DefaultProtocolConvert.string2hexstring("###"));
        parmMap.put(CollectContants.ENDTAG, DefaultProtocolConvert.string2hexstring("&&&"));
        parmMap.put("TimeSp", StringHelper.isBlank(parmMap.get("TimeSp"))?System.currentTimeMillis()/1000+"":parmMap.get("TimeSp"));
        MqClientManager.publish(CollectContants.COLLECT_SEND_TERMINAL_MSG_QUEUE_COMMAND, parmMap, null);
        //http 请求响应
        writeHttpResponse(true, null, ctx);
    }


    private void doGet(Map<String, String> parmMap, ChannelHandlerContext ctx) throws Exception {
        doPost(parmMap, ctx);
    }

    /**
     * 获取post请求参数,并且将参数添加到Map
     *
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年8月16日 下午3:34:52
     * @param body
     * @param paramMap
     */
    private void getPostParameter(byte[] body, Map<String, String> paramMap) {

        QueryStringDecoder decoderQuery = new QueryStringDecoder("some?" + DefaultProtocolConvert.bytes2string(body));
        decoderQuery.parameters().entrySet().forEach(entry -> {
            // entry.getValue()是一个List, 只取第一个元素
            paramMap.put(entry.getKey(), entry.getValue().get(0));
        });
    }

    /**
     * 获取get请求参数,并且将参数添加到Map
     *
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年8月16日 下午3:33:52
     * @param body
     * @param paramMap
     */
    private void getGetParameter(String uri, Map<String, String> paramMap) {
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        decoder.parameters().entrySet().forEach(entry -> {
            paramMap.put(entry.getKey(), entry.getValue().get(0));
        });
    }

    /**
     * 响应客户端
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年8月16日 下午3:36:46
     * @param ctx
     * @throws UnsupportedEncodingException
     */
    private void writeHttpResponse(boolean isOk, String errormsg, ChannelHandlerContext ctx) throws UnsupportedEncodingException {

        Map<String, String> respMap = new HashMap<>();
        if (isOk) {
            respMap.put("status", "0");
            respMap.put("msg", "操作成功");
        } else {
            respMap.put("status", "-1");
            respMap.put("msg", "操作失败" + errormsg);
        }
        ByteBuf bytebuf = Unpooled.copiedBuffer(JSON.toJSONString(respMap), CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/json; charset=UTF-8");
        response.content().writeBytes(bytebuf);
        bytebuf.release();
        ctx.writeAndFlush(response);
        ctx.close();
    }

    private void writeHttpResponseVCM(ResultVo resultVo, ChannelHandlerContext ctx) throws UnsupportedEncodingException {
        ByteBuf bytebuf = Unpooled.copiedBuffer(JSON.toJSONString(resultVo), CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/json; charset=UTF-8");
        response.content().writeBytes(bytebuf);
        bytebuf.release();
        ctx.writeAndFlush(response);
        ctx.close();
    }
}
