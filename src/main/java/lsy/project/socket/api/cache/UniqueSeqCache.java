package lsy.project.socket.api.cache;

import java.util.Date;


import com.lsy.base.date.DateHelper;
import com.lsy.redis.client.JedisClient;
import lsy.project.socket.api.contants.CollectContants;


/**
 * 唯一序列 缓存信息
 *
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public class UniqueSeqCache {

    /**
     * 设置单个seqKey 配置信息到缓存
     *
     * @param seqKey
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年9月26日 下午3:46:38
     */
    public static boolean set(String imei, String seq, Object value) {

        String date = DateHelper.formatDate(new Date());
        String seqKey = String.format(CollectContants.TRASH_UPLOAD_SEQ, imei + seq + date);
        JedisClient client = JedisClient.getJedisClient();
        boolean isTrue = client.oHset(seqKey, value);
        //当天有效
        client.expire(seqKey, 86400);
        return isTrue;
    }

    /**
     * 判断是否存在
     *
     * @param seqKey
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年9月28日 上午11:41:52
     */
    public static boolean isExists(String imei, String seq) {
        JedisClient client = JedisClient.getJedisClient();
        String date = DateHelper.formatDate(new Date());
        String seqKey = String.format(CollectContants.TRASH_UPLOAD_SEQ, imei + seq + date);
        return client.exists(seqKey);
    }

}
