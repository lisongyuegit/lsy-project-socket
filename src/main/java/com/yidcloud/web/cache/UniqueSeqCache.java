package com.yidcloud.web.cache;

import com.lsy.base.date.DateHelper;
import com.lsy.redis.client.JedisClient;
import com.yidcloud.api.contants.CollectContants;

import java.util.Date;



/**
 * 唯一序列 缓存信息
 * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
 * @copyright: Copyright (c) 2018
 * @company: 易登科技
 * @author: zhouliang@edenep.net
 * @version: 2.0
 * @date: 2018年9月28日 上午11:35:40
 */
public class UniqueSeqCache {
    
    /**
     * 设置单个seqKey 配置信息到缓存
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年9月26日 下午3:46:38
     * @param seqKey
     * @return
     */
    public static boolean set(String imei,String seq,Object value) {
        
        String date = DateHelper.formatDate(new Date());
        String seqKey = String.format(CollectContants.TRASH_UPLOAD_SEQ,imei+seq+date);
        JedisClient client = JedisClient.getJedisClient();
        boolean isTrue = client.oHset(seqKey, value);
        //当天有效
        client.expire(seqKey, 86400);
        return isTrue;
    }
    
    /**
     * 判断是否存在
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年9月28日 上午11:41:52
     * @param seqKey
     * @return
     */
    public static boolean isExists(String imei,String seq) {
        JedisClient client = JedisClient.getJedisClient();
        String date = DateHelper.formatDate(new Date());
        String seqKey = String.format(CollectContants.TRASH_UPLOAD_SEQ,imei+seq+date);
        return client.exists(seqKey);
    }
    
}
