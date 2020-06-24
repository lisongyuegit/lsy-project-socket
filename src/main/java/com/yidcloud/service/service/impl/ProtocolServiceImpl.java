package com.yidcloud.service.service.impl;

import com.alibaba.fastjson.JSON;

import com.lsy.base.exception.BaseException;
import com.lsy.base.string.StringHelper;
import com.lsy.base.utils.ConvertHelper;
import com.lsy.mybatisplus.mapper.EntityWrapper;
import com.lsy.mybatisplus.plugins.Page;
import com.lsy.mybatisplus.service.impl.ServiceImpl;
import com.lsy.redis.client.JedisClient;
import com.yidcloud.api.contants.CollectContants;
import com.yidcloud.api.entity.Protocol;
import com.yidcloud.api.entity.ProtocolAnalysis;
import com.yidcloud.api.service.IProtocolAnalysisService;
import com.yidcloud.api.service.IProtocolService;
import com.yidcloud.service.mapper.ProtocolMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;


/**
 * @description:  协议表（指令表，相当于一个一个的请求接口信息） 服务实现类 (用一句话描述该类做什么)
 * @copyright: Copyright (c) 2017
 * @company: 易登科技
 * @author 胡洪瑜
 * @version 2.0
 * @since 2017/11/08 17:09:17
 */
public class ProtocolServiceImpl extends ServiceImpl<ProtocolMapper, Protocol> implements IProtocolService {
    static Logger logger = LoggerFactory.getLogger(ProtocolServiceImpl.class);

    @Autowired
    public ProtocolMapper protocolMapper;

    @Autowired
    private IProtocolAnalysisService protocolAnalysisService;
    @Override
    public Page<Protocol> queryList(Protocol protocol, Map<String,String> params, int current, int pageSize){
        EntityWrapper<Protocol> entityWrapper = new EntityWrapper<Protocol>();
        entityWrapper.setEntity(protocol);
        if (protocol != null) {
            if (StringHelper.isNotBlank(params.get("startCreateDate")) && StringHelper.isNotBlank(params.get("endCreateDate"))) {
                entityWrapper.between("create_date", params.get("startCreateDate"), params.get("endCreateDate"));
            }else if(StringHelper.isNotBlank(params.get("startCreateDate"))) {
                entityWrapper.ge("create_date", params.get("startCreateDate"));
            }else if(StringHelper.isNotBlank(params.get("endCreateDate"))) {
                entityWrapper.le("create_date", params.get("endCreateDate"));
            }
            if (StringHelper.isNotBlank(params.get("startUpdateDate")) && StringHelper.isNotBlank(params.get("endUpdateDate"))) {
                entityWrapper.between("update_date", params.get("startUpdateDate"), params.get("endUpdateDate"));
            }else if(StringHelper.isNotBlank(params.get("startUpdateDate"))) {
                entityWrapper.ge("update_date", params.get("startUpdateDate"));
            }else if(StringHelper.isNotBlank(params.get("endUpdateDate"))) {
                entityWrapper.le("update_date", params.get("endUpdateDate"));
            }
        }
        return this.selectPage(new Page<Protocol>(current, pageSize), entityWrapper);
    }

    @Override
    public void synProtocolToCache() {
        List<Map> list = protocolMapper.selectAllProtocol(CollectContants.PROTOCOL_ACTIVE_STATUS_01);
        synProtocolToCache(list);
        logger.info("同步所有协议信息完成,条数为："+list.size());
    }

    @Override
    public void synProtocolToCache(String[] protocolIds) {
        if (protocolIds!=null && protocolIds.length>0){
            Map paramMap = new HashMap(2);
            paramMap.put("activeStatus",CollectContants.PROTOCOL_ACTIVE_STATUS_01);
            paramMap.put("ids",protocolIds);
            List<Map> list = protocolMapper.selectProtocols(paramMap);
            synProtocolToCache(list);
            logger.info("同步指定协议信息完成,条数为："+list.size());
        }
    }

    @Override
    public List<Map> selectProNameIdDic() throws BaseException {
        return protocolMapper.selectProNameIdDic();
    }

    /**
     * 同步协议信息到缓存中去
     * @param list
     * @since  2017/11/8  21:15
     */
    private void synProtocolToCache( List<Map> list){
        if(list!=null&&list.size()>0){
            StringBuilder redisKey= new StringBuilder();
            Map<String,String> convMap = null;
            for (Map map:list) {
                convMap = ConvertHelper.mapObjectToMapString(map);
                //找到每一个协议对应对应的解析方法，
                int protocolId = Integer.parseInt(convMap.get("id"));
                EntityWrapper<ProtocolAnalysis> wrapper = new EntityWrapper<>();
                ProtocolAnalysis protocolAnalysis = new ProtocolAnalysis();
                protocolAnalysis.setProtocolId(protocolId);
                wrapper.setEntity(protocolAnalysis);
                List<ProtocolAnalysis> protocolAnalyses = protocolAnalysisService.selectList(wrapper);
                String analysis = JSON.toJSONString(protocolAnalyses);
                convMap.put(CollectContants.PROTOCOL_ANALYSIS_FIELD_NAME,analysis);
                //组装key
                redisKey.append(CollectContants.PROTOCOL_REDIS_PREFIX)
                        .append(convMap.get("startTag"))
                        .append("_")
                        .append(convMap.get("port"))
                        .append(".")
                        .append(convMap.get("mid"));
                //把数据插入到redis中
                JedisClient client = JedisClient.getJedisClient();
                client.hset(redisKey.toString(),convMap);
                //清空redisKey
                redisKey.setLength(0);
            }
        }

    }

}
