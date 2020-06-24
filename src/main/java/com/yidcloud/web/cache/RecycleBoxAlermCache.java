package com.yidcloud.web.cache;

import com.lsy.redis.client.JedisClient;
import com.yidcloud.api.contants.CollectContants;

import java.util.Map;



/**
 * 回收箱报警 缓存信息
 * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
 * @copyright: Copyright (c) 2018
 * @company: 易登科技
 * @author: zhouliang@edenep.net
 * @version: 2.0
 * @date: 2018年9月27日 上午13:35:40
 */
public class RecycleBoxAlermCache {
    
    /**
     * 设置单个imeiKey 配置信息到缓存
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年9月26日 下午3:46:38
     * @param imei
     * @return
     */
    public static boolean set(String imei,Map<String,String> value) {
        JedisClient client = JedisClient.getJedisClient();
        String imeiKey = String.format(CollectContants.TRASH_ALERM,imei);
        boolean isTrue = client.oHset(imeiKey, value);
        //90秒有效期
        client.expire(imeiKey, 90);
        return isTrue;
    }
    
    /**
     * 判断是否存在
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年9月28日 上午11:41:52
     * @param imeiKey
     * @return
     */
    public static boolean isExists(String imei) {
        JedisClient client = JedisClient.getJedisClient();
        String imeiKey = String.format(CollectContants.TRASH_ALERM,imei);
        return client.exists(imeiKey);
    }
    
}
