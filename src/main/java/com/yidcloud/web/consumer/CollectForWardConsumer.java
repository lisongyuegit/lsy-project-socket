package com.yidcloud.web.consumer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.lsy.base.result.ResultVo;
import com.lsy.base.string.StringHelper;
import com.lsy.base.utils.UUID;
import com.lsy.rabbitmq.client.MqClientManager;
import com.lsy.rabbitmq.client.consumer.AbstractMqConsumer;
import com.lsy.redis.client.JedisClient;
import com.yidcloud.api.contants.CollectContants;
import com.yidcloud.api.enums.BooleanCharEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.yidcloud.web.cache.RecycleBoxAlermCache;
import com.yidcloud.web.cache.UniqueSeqCache;
import com.yidcloud.web.convert.DefaultProtocolConvert;
import com.yidcloud.web.util.HttpClientUtil;

import okhttp3.FormBody;
import okhttp3.MediaType;

/**
 * 数据转发消费者
 * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
 * @copyright: Copyright (c) 2018
 * @company: 易登科技
 * @author: zhouliang@edenep.net
 * @version: 2.0
 * @date: 2018年9月28日 下午9:48:28
 */
public class CollectForWardConsumer extends AbstractMqConsumer {
    
    JedisClient client = JedisClient.getJedisClient();

    private static Logger logger = LoggerFactory.getLogger(CollectForWardConsumer.class);

    public static final MediaType CONTENT_TYPE = MediaType.parse("application/json; charset=utf-8");

    private static final String FORWARDURL = "forwardUrl";

    private static final String QCODE = "qcode";


    private static final String HEIGHT = "height";

    private static final String WEIGHT = "weight";
    
    private static final String SEQ = "seq";

    @Override
    public ResultVo invoke(Map<String, String> param) throws Exception {
        String forwardUrl = param.get(FORWARDURL);
        logger.info("开始处理转发数据:"+param.get("sourceHexstring"));
        if (StringHelper.isNotBlank(forwardUrl) && !"null".equals(forwardUrl)) {
            String cases = param.get(CollectContants.HEADTAG) + param.get(CollectContants.MID);
            switch (cases) {
                //垃圾投放信息上传
                case "fc42":
                    uploadRubbishInfo(param);
                    break;
                //用户二维码验证
                case "fc44":
                    verifyQcode(param);
                    break;
                //垃圾箱位置信息上传
                case "fc03":
                    uploadLocation(param);
                    break;
                //垃圾箱报警信息上传
                case "fc49":
                    uploadAlerm(param);
                    break;
                //向服务器请求机器ID和密钥
                case "2323231000":
                    getVmData(param);
                    break;
                //向服务器推送货道状态 -- 暂时不要  售货机上报的货道状态不影响出货，故暂不做控制
                /*case "2323235000":
                    pushVmSlot(param);
                    break;*/
                default:
                    break;
            }
        }
        return null;
    }

    private void pushVmSlot(Map<String, String> param) {

        //组装 POST 参数数据
        Map<String,String> requestParam = new HashMap<>();
        requestParam.put("vmImei", param.get(CollectContants.IMEI));
        requestParam.put("aisleStatusStr", param.get("slotInfo"));
        String forwardUrl = param.get(FORWARDURL);
        if(forwardUrl.contains("http")) {
            //数据转发到第三方平台 若推送失败-最多推送三次，每次间隔5s
            ResultVo result = sendHttpRequest(HttpType.POST, forwardUrl, requestParam);
        }
    }

    private void getVmData(Map<String, String> param) {

    }

    public enum HttpType
    {
        POST,  //post 请求
        GET; //get 请求
        @Override
        public String toString() {
            return this==HttpType.POST?"post":"get";
        }
    }

    /**
     * 模拟发送Http请求数据
     * 若推送失败-最多推送三次，每次间隔5*i s ,其中i为变量 最小=0 最大=2
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年9月28日 下午12:02:50
     * @param httpType 请求方式
     * @param forwardUrl 推送地址
     * @param requestParam 请求数据
     * @return
     */
    private ResultVo sendHttpRequest(HttpType httpType, String forwardUrl,
            Map<String,String> requestParam) {
        
        ResultVo result = new ResultVo();
        
        FormBody.Builder formBody = new FormBody.Builder();
        Set<Map.Entry<String, String>> entry = requestParam.entrySet();
        for (Map.Entry<String, String> stringEntry : entry) {
            formBody.add(stringEntry.getKey(), stringEntry.getValue());
        }
        int maxCount = 3;
        for (int i = 0; i < maxCount; i++) {
            
            logger.info(String.format("设备ID: %s,推送地址:%s,Http %s[%s]send : %s", requestParam.get(CollectContants.IMEI), forwardUrl, httpType.toString(), i+1, JSON.toJSONString(requestParam)));
            if (httpType==HttpType.POST) {
                result = HttpClientUtil.httpPost(forwardUrl, formBody.build(), null, CONTENT_TYPE);
            } else if (httpType==HttpType.GET) {
                result = HttpClientUtil.httpGet(forwardUrl, "");
            }
            
            logger.info(String.format("设备ID: %s,推送地址:%s,Http %s[%s]receive : %s", requestParam.get(CollectContants.IMEI), forwardUrl, httpType.toString(), i+1, JSON.toJSONString(result)));
            if(result.getError_no() != 0) {
                try {
                    Thread.sleep(5000*(i+1));
                } catch (InterruptedException e) {
                    logger.error("线程中断异常：" + e);
                }
                continue;
            }
            break;
        }

        return result;
    }
    
    /**
     * 上传报警信息
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年9月28日 下午12:01:25
     * @param param
     */
    private void uploadAlerm(Map<String, String> param) {
        
        //组装 POST 参数数据
        Map<String,String> requestParam = new HashMap<>();
        requestParam.put("imei", param.get(CollectContants.IMEI));
        requestParam.put("alermType", param.get("alermType"));
        
        //将报警信息写入缓存
        RecycleBoxAlermCache.set(param.get(CollectContants.IMEI), param);
        
        if(param.get(CollectContants.IMEI).equals("02020202")) {
        	param.put(FORWARDURL, "http://192.168.0.204:8017/efen/merchant/event/merchantDeviceEvent/add");
        }
        String forwardUrl = param.get(FORWARDURL);
        if(forwardUrl.contains("http")) {
            //数据转发到第三方平台 若推送失败-最多推送三次，每次间隔5s
            ResultVo result = sendHttpRequest(HttpType.POST, forwardUrl, requestParam);
        }
    }
    
    /**
     * 上报位置信息
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年9月28日 下午12:01:25
     * @param param
     */
    private void uploadLocation(Map<String, String> param) {
        
        //组装 POST 参数数据
        Map<String,String> requestParam = new HashMap<>();
        requestParam.put(CollectContants.IMEI, param.get(CollectContants.IMEI));
        requestParam.put("lat", param.get("lat"));
        requestParam.put("lng", param.get("lng"));
        String forwardUrl = param.get(FORWARDURL);
        
        logger.info(String.format("设备ID:%s,上报位置状态:%s,经度:%s,纬度:%s", param.get(CollectContants.IMEI),"00".equals(param.get("locationStatus"))?"有效":"无效", param.get("lng"),param.get("lat")));

        //数据转发到第三方平台 若推送失败-最多推送三次，每次间隔5s
        if("00".equals(param.get("locationStatus"))) {//定位状态 00 有效 01无效
            sendHttpRequest(HttpType.POST, forwardUrl, requestParam);
        }
    }

    /**
     * 在第三方平台验证二维码信息
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年9月28日 下午12:01:36
     * @param param
     * @throws Exception 
     */
    private void verifyQcode(Map<String, String> param) throws Exception {
        
        logger.info(param.get(CollectContants.IMEI) + "验证用户,二维码：" + param.get(QCODE)+"原始数据："+param.get("sourceHexstring"));
        
        //存在报警信息 直接回复错误，禁止开门
        if(RecycleBoxAlermCache.isExists(param.get(CollectContants.IMEI))) {
            param.put(CollectContants.CMD, "FC44");
            param.put(CollectContants.ISSUCCRSS, BooleanCharEnum.FALSE.getValue());
            param.put("errcode", "2");
            MqClientManager.publish(CollectContants.COLLECT_SEND_TERMINAL_MSG_QUEUE_COMMAND, param, null);
            return;
        }
        
        Map<String,String> requestParam = new HashMap<>();
        requestParam.put(CollectContants.IMEI, param.get(CollectContants.IMEI));
        requestParam.put("openid", param.get(QCODE));
        if(param.get(CollectContants.IMEI).equals("02020202")) {
        	param.put(FORWARDURL, "http://192.168.0.204:8087/efen/gateway/userExists");
        }
        String forwardUrl = param.get(FORWARDURL) + "?openid=" + param.get(QCODE);
        ResultVo result = sendHttpRequest(HttpType.GET, forwardUrl, requestParam);
        //数据转发到第三方平台成功 0 表示成功
        if (result.getError_no()==0) {
            param.put(CollectContants.ISSUCCRSS, BooleanCharEnum.TRUE.getValue());
        }else {
            param.put(CollectContants.ISSUCCRSS, BooleanCharEnum.FALSE.getValue());
            param.put("errcode", "1");
        }
        
        /**
         * 合法消息入智能垃圾桶的队列  广播消息
         */
        param.put(CollectContants.CMD, "FC44");
        MqClientManager.publish(CollectContants.COLLECT_SEND_TERMINAL_MSG_QUEUE_COMMAND, param, null);
    }

    /**
     * 上传垃圾信息
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年9月28日 下午12:02:25
     * @param param
     */
    private void uploadRubbishInfo(Map<String, String> param) {
        
        //判断缓存中  是否已上传过改序列值的记录
        if(UniqueSeqCache.isExists(param.get(CollectContants.IMEI),param.get(SEQ))) {
            String printlog = param.get(CollectContants.IMEI) + "上传垃圾重量,二维码：" + param.get(QCODE)+"原始数据："+param.get("sourceHexstring") + " %s";
            logger.info(String.format(printlog,",["+param.get(SEQ)+ "重复上传]"));
            return;
        }
        
        //组装 POST 参数数据
        Map<String,String> requestParam = new HashMap<>();
        requestParam.put(CollectContants.IMEI, param.get(CollectContants.IMEI));
        requestParam.put("openid", param.get(QCODE));
        requestParam.put("height", String.valueOf(DefaultProtocolConvert.hexstring2int(param.get(HEIGHT))));
        requestParam.put("weight", String.valueOf((double)DefaultProtocolConvert.hexstring2int(param.get(WEIGHT))/100));
        requestParam.put("requestId", UUID.getUUIDString());
        if(param.get(CollectContants.IMEI).equals("02020202")) {
        	param.put(FORWARDURL, "http://192.168.0.204:8087/efen/gateway/trashcan/trashcanDataUpload");
        }
        String forwardUrl = param.get(FORWARDURL);
        ResultVo result = sendHttpRequest(HttpType.POST, forwardUrl, requestParam);
        
        //数据转发到第三方平台成功, 保存唯一序列值到缓存
        if (result.getError_no()==0) {
            UniqueSeqCache.set(param.get(CollectContants.IMEI),param.get(SEQ), param);
        }

    }
}
