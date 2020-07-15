package lsy.project.socket.api.cache;

import java.util.*;

import com.lsy.base.date.DateHelper;
import com.lsy.base.string.StringHelper;
import com.lsy.base.utils.ConvertHelper;
import com.lsy.redis.client.JedisClient;
import lsy.project.socket.api.contants.CollectContants;
import lsy.project.socket.api.dto.ImeiChannelDto;
import lsy.project.socket.api.dto.UpgradeDto;
import lsy.project.socket.api.entity.Protocol;
import lsy.project.socket.api.entity.ProtocolAnalysis;
import lsy.project.socket.api.entity.ProtocolType;
import lsy.project.socket.api.enums.BooleanCharEnum;
import lsy.project.socket.api.util.VendingMachineUtil;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;


/**
 * 数据采集，缓存服务类
 *
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
@Service
public class CollectRedisCacheService {

    /**
     * 在缓存中获取协议类型数据
     *
     * @param
     * @return
     * @since 2017/11/11  16:47
     */
    public static ProtocolType getProtocolTypesFromCache(String startTag, int port) {
        StringBuilder redisKey = new StringBuilder();
        redisKey.append(CollectContants.PROTOCOL_TYPE_REDIS_PREFIX)
                .append(startTag)
                .append("_").append(port);
        JedisClient client = JedisClient.getJedisClient();
        return (ProtocolType) ConvertHelper
                .mapStringToObject(client.hget(redisKey.toString()), ProtocolType.class);
    }

    /**
     * 在缓存中获取协议信息
     *
     * @param
     * @return
     * @since 2017/11/11  16:48
     */
    public static Map<String, Object> getProtocolsFromCache(String startTag, int port, String mid) {

        StringBuilder redisKey = new StringBuilder();
        redisKey.append(CollectContants.PROTOCOL_REDIS_PREFIX)
                .append(startTag)
                .append("_")
                .append(port)
                .append(".")
                .append(mid);
        JedisClient client = JedisClient.getJedisClient();
        Map<String, String> redisMap = client.hget(redisKey.toString());
        if (redisMap != null && redisMap.size() > 0) {
            Map<String, Object> returnMap = new HashMap<>(2);
            String analysisJson = redisMap.get(CollectContants.PROTOCOL_ANALYSIS_FIELD_NAME);
            //组装协议
            redisMap.remove(CollectContants.PROTOCOL_ANALYSIS_FIELD_NAME);
            Protocol protocol = (Protocol) ConvertHelper.mapStringToObject(redisMap, Protocol.class);
            //组装协议解析列表
            List<ProtocolAnalysis> protocolAnalyses = JSON.parseArray(analysisJson, ProtocolAnalysis.class);
            returnMap.put("protocol", protocol);
            returnMap.put("protocolAnalyses", protocolAnalyses);
            return returnMap;
        } else {
            return null;
        }

    }

    /**
     * 协议是存在
     *
     * @param
     * @return
     * @since 2017/11/11  17:32
     */
    public static boolean midExists(String startTag, int port, String mid) {
        JedisClient client = JedisClient.getJedisClient();
        StringBuilder redisKey = new StringBuilder();
        redisKey.append(CollectContants.PROTOCOL_REDIS_PREFIX)
                .append(startTag)
                .append("_")
                .append(port)
                .append(".")
                .append(mid);
        return client.exists(redisKey.toString());
    }

    /**
     * 获取协议的回写类
     *
     * @param
     * @return
     * @since 2017/11/14  22:04
     */
    public static String getProtocolCallBackClazz(String startTag, int port, String mid) {
        JedisClient client = JedisClient.getJedisClient();
        StringBuilder redisKey = new StringBuilder();
        redisKey.append(CollectContants.PROTOCOL_REDIS_PREFIX)
                .append(startTag)
                .append("_")
                .append(port)
                .append(".")
                .append(mid);
        return client.hget(redisKey.toString(), CollectContants.PROTOCOL_CALL_BACK_CLAZZ_FIELD_NAME);
    }

    /**
     * 获取是否转发
     *
     * @param
     * @return
     * @since 2017/11/14  22:04
     */
    public static String getProtocolIsForward(String startTag, int port, String mid) {
        JedisClient client = JedisClient.getJedisClient();
        StringBuilder redisKey = new StringBuilder();
        redisKey.append(CollectContants.PROTOCOL_REDIS_PREFIX)
                .append(startTag)
                .append("_")
                .append(port)
                .append(".")
                .append(mid);
        return client.hget(redisKey.toString(), CollectContants.PROTOCOL_IS_FORWARD_NAME);
    }

    /**
     * 获取转发地址
     *
     * @param
     * @return
     * @since 2017/11/14  22:04
     */
    public static String getProtocolForwardUrl(String startTag, int port, String mid) {
        JedisClient client = JedisClient.getJedisClient();
        StringBuilder redisKey = new StringBuilder();
        redisKey.append(CollectContants.PROTOCOL_REDIS_PREFIX)
                .append(startTag)
                .append("_")
                .append(port)
                .append(".")
                .append(mid);
        return client.hget(redisKey.toString(), CollectContants.PROTOCOL_FORWARD_URL_NAME);
    }

    /**
     * 自检协议 0否 1心跳自检 2合法数据自检
     *
     * @param startTag
     * @param port     端口
     * @param mid
     * @return
     * @version: 2.0
     * @date: 2018年9月20日 下午5:53:50
     */
    public static int getProtocolSelfChecking(String startTag, int port, String mid) {
        JedisClient client = JedisClient.getJedisClient();
        StringBuilder redisKey = new StringBuilder();
        redisKey.append(CollectContants.PROTOCOL_REDIS_PREFIX)
                .append(startTag)
                .append("_")
                .append(port)
                .append(".")
                .append(mid);
        String value = client.hget(redisKey.toString(), CollectContants.PROTOCOL_SELF_CHECKING_PROTOCOL_NAME);
        if (null != value) {
            return Integer.parseInt(value);
        }
        return 0;
    }

    /**
     * 默认所有的imei都是通过的
     *
     * @param
     * @return
     * @since 2017/11/11  17:24
     */
    public static boolean checkImei(String imei) {
        return true;
    }

    /**
     * 获取设备缓存信息
     *
     * @param imei 设备号
     * @return
     * @version: 2.0
     * @date: 2018 2018年4月27日 下午4:54:27
     */
    public static Map<String, String> getImeiCache(String imei) {
        JedisClient client = JedisClient.getJedisClient();
        String rediskey = CollectContants.IMEI_CACHE + imei;
        return client.hget(rediskey);
    }

    /**
     * 获取设备点位上次时间间隔（目前卡片机登录业务有用到）
     *
     * @param imei 设备号
     * @return
     * @author: zl
     * @version: 2.0
     * @date: 2018 2018年4月27日 下午4:22:16
     */
    public static String getImeiReportTime(String imei) {
        JedisClient client = JedisClient.getJedisClient();
        String rediskey = CollectContants.IMEI_CACHE + imei;
        return client.hget(rediskey, "intervalTimer");
    }

    /**
     * 从缓存中获取卡片机 IMEI今明两天（保洁员 或 网格长）的排班信息
     *
     * @param imei  设备号
     * @param value
     * @return
     * @author: zl
     * @version: 2.0
     * @date: 2018 2018年4月8日 下午4:40:08
     */
    public static String getImeiAttScheduleData(String imei, String value) {

        JedisClient client = JedisClient.getJedisClient();
        String rediskey = CollectContants.IMEI_ATTENDANCE_SCHEDULE + imei;
        return client.hget(rediskey, value);
    }

    /**
     * 更新设备缓存指令信息
     *
     * @param imei
     * @param value
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018 2018年4月27日 下午9:33:03
     */
    public static void updateImeiCache(String imei, Map value) {

        JedisClient client = JedisClient.getJedisClient();
        String rediskey = CollectContants.IMEI_CACHE + imei;
        client.hset(rediskey, value);
    }

    /**
     * 获取设备IMEI 当前绑定的登录账号
     *
     * @param imei
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zl
     * @version: 2.0
     * @date: 2018 2018年5月17日 下午5:36:52
     */
    public static String getImei2Account(String imei) {

        JedisClient client = JedisClient.getJedisClient();
        String rediskey = String.format(CollectContants.IMEI2EMPLOYEE, imei);
        return client.hget(rediskey, "account");
    }

    /**
     * 获取设备执行获取升级任务缓存信息
     *
     * @param imei 设备号
     * @return
     * @version: 2.0
     * @date: 2018年6月25日 15:51:10
     */
    public static Map<String, String> getImeiTast(String imei) {
        JedisClient client = JedisClient.getJedisClient();
        String rediskey = CollectContants.IMEI_TASK_CACHE + imei;
        return client.hget(rediskey);
    }

    /**
     * 新增升级任务缓存信息
     *
     * @param imei
     * @param value
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018年6月27日 下午6:56:36
     */
    public static void addImeiTast(String imei, Map value) {
        JedisClient client = JedisClient.getJedisClient();
        String rediskey = CollectContants.IMEI_TASK_CACHE + imei;
        client.hset(rediskey, value);
    }

    public static void addServerPushUpgrade(UpgradeDto boxUpgradeDto) {

        if (null == boxUpgradeDto) {
            return;
        }
        JedisClient client = JedisClient.getJedisClient();
        String rediskey = CollectContants.IMEI_UPGRADE_CACHE + boxUpgradeDto.getImei();
        @SuppressWarnings("unchecked")
        Map<String, String> value = JSON.parseObject(JSON.toJSONString(boxUpgradeDto), Map.class);
        client.hset(rediskey, value);
    }

    public static UpgradeDto getUpgradeDto(String imei) {
        JedisClient client = JedisClient.getJedisClient();
        String rediskey = CollectContants.IMEI_UPGRADE_CACHE + imei;
        Map<String, String> value = client.hget(rediskey);
        if (value != null && !value.isEmpty()) {
            return JSON.parseObject(JSON.toJSONString(value), UpgradeDto.class);
        }
        return null;
    }

    /**
     * 升级包下发完成 删除升级缓存信息
     *
     * @param imei
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018 2018年7月13日 上午9:26:22
     */
    public static void delUpgradeDto(String imei) {
        JedisClient client = JedisClient.getJedisClient();
        String rediskey = CollectContants.IMEI_UPGRADE_CACHE + imei;
        client.hdelete(rediskey);
    }

    /**
     * 合法的IMEI设备 客户端添加到缓存 并设置一周的过期时间
     *
     * @param imeiChannelDto
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018年7月12日 上午9:38:34
     */
    public static void setImeiChannel(ImeiChannelDto imeiChannelDto) {

        if (null == imeiChannelDto) {
            return;
        }
        JedisClient client = JedisClient.getJedisClient();
        String rediskey = CollectContants.IMEI_CHANNEL_CACHE + imeiChannelDto.getImei();
        @SuppressWarnings("unchecked")
        Map<String, String> value = JSON.parseObject(JSON.toJSONString(imeiChannelDto), Map.class);
        client.hset(rediskey, value);
        client.expire(rediskey, 7 * 60 * 60 * 24);//设置一天后过期
    }

    /**
     * 获取IMEI 通道缓存记录
     *
     * @param imei
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zl
     * @version: 2.0
     * @date: 2018年9月20日 下午10:21:09
     */
    public static ImeiChannelDto getImeiChannel(String imei) {
        JedisClient client = JedisClient.getJedisClient();
        String rediskey = CollectContants.IMEI_CHANNEL_CACHE + imei;
        Map<String, String> value = client.hget(rediskey);
        if (value != null && !value.isEmpty()) {
            return JSON.parseObject(JSON.toJSONString(value), ImeiChannelDto.class);
        }
        return null;
    }

    /**
     * 删除IMEI对应的客户端信息
     *
     * @param imei
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zl
     * @version: 2.0
     * @date: 2018年7月12日 上午9:39:26
     */
    public static void delImeiChannel(String imei) {
        JedisClient client = JedisClient.getJedisClient();
        String rediskey = CollectContants.IMEI_CHANNEL_CACHE + imei;
        client.hdelete(rediskey);
    }

    /**
     * 记录IMEI设备下线时间
     *
     * @param imei
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zl
     * @version: 2.0
     * @date: 2018年9月20日 下午11:12:55
     */
    public static void setImeiOfflineTime(String imei) {
        ImeiChannelDto imeiChannelDto = CollectRedisCacheService.getImeiChannel(imei);
        if (null != imeiChannelDto) {
            imeiChannelDto.setLastOfflineTime(DateHelper.formatDate(new Date(), DateHelper.PATTERN_TIME));
            imeiChannelDto.setIsOnline(BooleanCharEnum.FALSE.getValue());
            setImeiChannel(imeiChannelDto);
        }
    }

    public static String getUniqueMid(String mid, String imei) {

        JedisClient client = JedisClient.getJedisClient();
        if ("0000000000".equalsIgnoreCase(mid)) {
            String imeikey = String.format(CollectContants.IMEI_VENDINGMACHINE_CACHE, imei);
            if (client.exists(imeikey)) {
                Map<String, String> receive = client.hget(imeikey);
                mid = receive.get("Mid");
            } else {
                mid = VendingMachineUtil.getRandomNumr(10);
                client.set(String.format(CollectContants.IMEI_VENDINGMACHINE_MID_CACHE, mid), imei);
            }
        } else {
            String rediskey = String.format(CollectContants.IMEI_VENDINGMACHINE_MID_CACHE, mid);
            if (!client.exists(rediskey)) {
                client.set(rediskey, imei);
            }
        }
        return mid;
    }

    public static String getVmcImeiByMid(String mid) {

        JedisClient client = JedisClient.getJedisClient();
        String rediskey = String.format(CollectContants.IMEI_VENDINGMACHINE_MID_CACHE, mid);
        return client.getString(rediskey);
    }

    public static String getPrivateKey(String imei) {
        JedisClient client = JedisClient.getJedisClient();
        String imeikey = String.format(CollectContants.IMEI_VENDINGMACHINE_CACHE, imei);
        String priateKey = null;
        if (client.exists(imeikey)) {
            priateKey = client.hget(imeikey).get("PriKey");
        }
        return priateKey;
    }

    public static String getVMCMidByImei(String imei) {
        JedisClient client = JedisClient.getJedisClient();
        String imeikey = String.format(CollectContants.IMEI_VENDINGMACHINE_CACHE, imei);
        String mid = "";
        if (client.exists(imeikey)) {
            mid = client.hget(imeikey).get("Mid");
        }
        return mid;
    }

    public static void addVendingMachineImei(String imei, String pubKey, Map<String, String> send) {
        JedisClient client = JedisClient.getJedisClient();
        String rediskey = String.format(CollectContants.IMEI_VENDINGMACHINE_CACHE, imei);
        if (!client.exists(rediskey)) {
            send.put("PubKey", pubKey);
            send.put("date", DateHelper.formatTime(new Date()));
            client.hset(rediskey, send);

            String midRediskey = String.format(CollectContants.IMEI_VENDINGMACHINE_MID_CACHE, send.get("Mid"));
            if (!client.exists(midRediskey)) {
                client.set(midRediskey, imei);
            }
        }
    }

    public static void addVendingMachineWay(String imei, Map<String, String> send) {
        JedisClient client = JedisClient.getJedisClient();
        String rediskey = String.format(CollectContants.IMEI_VENDINGMACHINE_WAY_CACHE, imei);
        send.put("date", DateHelper.formatTime(new Date()));
        client.hset(rediskey, send);
    }

    public static Map<String, String> getVendingMachineWay(String imei) {
        JedisClient client = JedisClient.getJedisClient();
        String imeiWaykey = String.format(CollectContants.IMEI_VENDINGMACHINE_WAY_CACHE, imei);
        return client.hget(imeiWaykey);
    }

    public static void addVendingMachineOrder(String imei, Map<String, String> send) {
        JedisClient client = JedisClient.getJedisClient();

        String timeSp = DateHelper.formatDate(new Date(Long.parseLong(send.get("TimeSp")) * 1000L), "yyyyMMddHHmmss");
        String rediskey = String.format(CollectContants.IMEI_VENDINGMACHINE_ORDER_CACHE, imei, timeSp + "-" + send.get("OrderNo"));
        send.put("date", DateHelper.formatTime(new Date()));
        client.hset(rediskey, send);
        client.expire(rediskey, 60 * 60 * 24 * 7);//缓存一周
    }

    public static void editVendingMachineOrder(String cmd, String imei, Map<String, String> send) {
        JedisClient client = JedisClient.getJedisClient();
        Map<String, String> orderMap = getVendingMachineOrder(imei, send.get("OrderNo"), send.get("TimeSp"));
        String timeSp = DateHelper.formatDate(new Date(Long.parseLong(orderMap.get("TimeSp")) * 1000L), "yyyyMMddHHmmss");
        String rediskey = String.format(CollectContants.IMEI_VENDINGMACHINE_ORDER_CACHE, imei, timeSp + "-" + orderMap.get("OrderNo"));
        if ("3000".equalsIgnoreCase(cmd)) {
            orderMap.put(cmd, DateHelper.formatTime(new Date()));
        } else if ("4000".equalsIgnoreCase(cmd)) {
            send.put("date", DateHelper.formatTime(new Date()));
            orderMap.put(cmd, JSON.toJSONString(send));
        }
        client.hset(rediskey, orderMap);
    }

    public static Map<String, String> getVendingMachineOrder(String imei, String orderNo, String timeSp) {
        JedisClient client = JedisClient.getJedisClient();
        String imeiOrderkey = String.format(CollectContants.IMEI_VENDINGMACHINE_ORDER_CACHE, imei, "*-" + orderNo);
        if (StringHelper.isBlank(orderNo)) {
            timeSp = DateHelper.formatDate(new Date(Long.parseLong(timeSp) * 1000L), "yyyyMMddHHmmss");
            imeiOrderkey = String.format(CollectContants.IMEI_VENDINGMACHINE_ORDER_CACHE, imei, timeSp + "-*");
        }
        //业务场景对应的设备,订单号唯一
        Set set = client.getKeys(imeiOrderkey);
        Iterator it = set.iterator();
        while (it.hasNext()) {
            imeiOrderkey = it.next().toString();
        }
        return client.hget(imeiOrderkey);
    }

    public static void addVendingMachineHeart(String cmd, String imei, Map<String, String> rMap) {
        JedisClient client = JedisClient.getJedisClient();
        String rediskey = String.format(CollectContants.IMEI_VENDINGMACHINE_HEART_CACHE, imei, cmd);
        rMap.put("date", DateHelper.formatTime(new Date()));
        client.hset(rediskey, rMap);
    }

    public static Map<String, String> getVendingMachineHeart(String cmd, String imei) {
        JedisClient client = JedisClient.getJedisClient();
        String rediskey = String.format(CollectContants.IMEI_VENDINGMACHINE_HEART_CACHE, imei, cmd);
        return client.hget(rediskey);
    }

    public static void main(String[] args) {

        String mid = "6672703653";
        String imei = "863412041793525";
        String pubKey = "E00AE57FEC3030EC2DF8E949553F51EB468BADCF6A7FDC1369AA8BF8A3799CF9";

//        Map<String,String> map = new HashMap<>();
//        map.put("Mid",mid);
//        map.put("TimeSp","1514843250");
//        map.put("PriKey","3190793460609028920187056569106750376365810919571915974380272814");
//        addVendingMachineImei(imei,pubKey,map);

        String rediskey = String.format(CollectContants.IMEI_VENDINGMACHINE_MID_CACHE, mid);
        JedisClient client = JedisClient.getJedisClient();
        if (!client.exists(rediskey)) {
            client.set(rediskey, imei);
        }
    }
}
