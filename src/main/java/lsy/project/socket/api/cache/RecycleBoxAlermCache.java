package lsy.project.socket.api.cache;

import java.util.Map;


import com.lsy.redis.client.JedisClient;
import lsy.project.socket.api.contants.CollectContants;


/**
 * 回收箱报警 缓存信息
 *
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public class RecycleBoxAlermCache {

    /**
     * 设置单个imeiKey 配置信息到缓存
     *
     * @param imei
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年9月26日 下午3:46:38
     */
    public static boolean set(String imei, Map<String, String> value) {
        JedisClient client = JedisClient.getJedisClient();
        String imeiKey = String.format(CollectContants.TRASH_ALERM, imei);
        boolean isTrue = client.oHset(imeiKey, value);
        //90秒有效期
        client.expire(imeiKey, 90);
        return isTrue;
    }

    /**
     * 判断是否存在
     *
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年9月28日 上午11:41:52
     */
    public static boolean isExists(String imei) {
        JedisClient client = JedisClient.getJedisClient();
        String imeiKey = String.format(CollectContants.TRASH_ALERM, imei);
        return client.exists(imeiKey);
    }

}
