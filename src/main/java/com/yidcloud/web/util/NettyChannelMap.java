package com.yidcloud.web.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yidcloud.web.cache.CollectRedisCacheService;

import io.netty.channel.Channel;

/**
 * 用于保存TCP客户端通道
 * @copyright: Copyright (c) 2018
 * @company: 易登科技
 * @author: zhouliang@edenep.net
 * @version: 2.0
 * @date: 2018年7月2日 下午4:40:02
 */
public class NettyChannelMap {

    static Logger logger = LoggerFactory.getLogger(NettyChannelMap.class);
    
    /**
     * 存储所有的客户端连接
     */
    private static Map<String,Channel> map=new ConcurrentHashMap<String, Channel>();
    
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
    
    public static void put(String clientId,Channel Channel){
        logger.info("put new clientId:"+clientId);
        map.put(clientId,Channel);
    }
    
    public static Map<String,Channel> getAll(){
        return map;
     }
    
    public static Channel get(String clientId){
       return map.get(clientId);
    }
    
    /**
     * 移除指定通道-以及IMEI数据
     * @author: 
     * @version: 2.0
     * @date: 2018年7月2日 下午8:42:56
     * @param Channel
     */
    public static void remove(String clientId){
        
        //移除指定IP通道
        map.remove(clientId);
        if(imeiMap.isEmpty() || imeiMap.size()==0) {
            return;
        }
        
        String imei = null;
        for (Map.Entry entry:imeiMap.entrySet()){
            imei = (String) entry.getKey();
            if (entry.getValue().equals(clientId)){
                imeiMap.remove(imei);
                //缓存中设置设备下线
                CollectRedisCacheService.setImeiOfflineTime(imei);
            }
        }
    }
    
    /**
     * 判断指令IMEI通道是否已存在
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: 
     * @version: 2.0
     * @date: 2018年7月2日 下午5:31:10
     * @param imeiKey
     * @return
     */
    public static boolean judgeExist(String imeiKey) {
        if(imeiMap.containsKey(imeiKey)) {
            return true;
        }
        return false;
    }
    
}
