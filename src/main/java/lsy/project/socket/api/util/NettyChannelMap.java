package lsy.project.socket.api.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lsy.project.socket.api.cache.CollectRedisCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;


/**
 * 用于保存TCP客户端通道
 *
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public class NettyChannelMap {

    static Logger logger = LoggerFactory.getLogger(NettyChannelMap.class);

    /**
     * 存储所有的客户端连接
     */
    private static Map<String, Channel> map = new ConcurrentHashMap<String, Channel>();

    /**
     * 存储所有的客户端连接 与 IMEI的关系
     */
    private static Map<String, String> imeiMap = new ConcurrentHashMap<>();

    public static Map<String, String> getImeiMap() {
        return imeiMap;
    }

    public static void setImeiMap(Map<String, String> imeiMap) {
        NettyChannelMap.imeiMap = imeiMap;
    }

    public static void put(String clientId, Channel Channel) {
        logger.info("put new clientId:" + clientId);
        map.put(clientId, Channel);
    }

    public static Map<String, Channel> getAll() {
        return map;
    }

    public static Channel get(String clientId) {
        return map.get(clientId);
    }

    /**
     * 移除指定通道-以及IMEI数据
     *
     * @author:
     * @version: 2.0
     * @date: 2018年7月2日 下午8:42:56
     */
    public static void remove(String clientId) {

        //移除指定IP通道
        map.remove(clientId);
        if (imeiMap.isEmpty() || imeiMap.size() == 0) {
            return;
        }

        String imei = null;
        for (Map.Entry entry : imeiMap.entrySet()) {
            imei = (String) entry.getKey();
            if (entry.getValue().equals(clientId)) {
                imeiMap.remove(imei);
                //缓存中设置设备下线
                CollectRedisCacheService.setImeiOfflineTime(imei);
            }
        }
    }

    /**
     * 判断指令IMEI通道是否已存在
     *
     * @param imeiKey
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018年7月2日 下午5:31:10
     */
    public static boolean judgeExist(String imeiKey) {
        if (imeiMap.containsKey(imeiKey)) {
            return true;
        }
        return false;
    }

}
