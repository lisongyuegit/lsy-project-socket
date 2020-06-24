package com.yidcloud.service.service.impl;


import com.lsy.base.exception.BaseException;
import com.lsy.base.string.StringHelper;
import com.lsy.mybatisplus.mapper.EntityWrapper;
import com.lsy.mybatisplus.plugins.Page;
import com.lsy.mybatisplus.service.impl.ServiceImpl;
import com.lsy.redis.client.JedisClient;
import com.yidcloud.api.contants.CollectContants;
import com.yidcloud.api.entity.ProtocolType;
import com.yidcloud.api.service.IProtocolTypeService;
import com.yidcloud.service.mapper.ProtocolTypeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @description:  协议类型表 服务实现类 (用一句话描述该类做什么)
 * @copyright: Copyright (c) 2017
 * @company: 易登科技
 * @author 胡洪瑜
 * @version 2.0
 * @since 2017/11/08 17:09:17
 */
public class ProtocolTypeServiceImpl extends ServiceImpl<ProtocolTypeMapper, ProtocolType> implements IProtocolTypeService {
    static Logger logger = LoggerFactory.getLogger(ProtocolTypeServiceImpl.class);

    @Autowired
    public ProtocolTypeMapper typeMapper;
    @Override
    public Page<ProtocolType> queryList(ProtocolType protocolType, Map<String,String> params, int current, int pageSize){
        EntityWrapper<ProtocolType> entityWrapper = new EntityWrapper<ProtocolType>();
        entityWrapper.setEntity(protocolType);
        if (protocolType != null) {
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
        return this.selectPage(new Page<ProtocolType>(current, pageSize), entityWrapper);
    }

    @Override
    public void synProtocolTypeToCache() {
        //查询所有的协议类型
        List<ProtocolType> protocolTypes = selectList(null);
        synProtocolTypeToCache(protocolTypes);
        logger.info("同步所有协议类型信息完成,条数为："+protocolTypes.size());
    }

    @Override
    public void synProtocolTypeToCache(String[] protocolTypeIds) {
        if(protocolTypeIds!=null && protocolTypeIds.length>0){
            List<ProtocolType>  protocolTypes = selectBatchIds(Arrays.asList(protocolTypeIds));
            synProtocolTypeToCache(protocolTypes);
            logger.info("同步指定的协议类型信息完成,条数为："+protocolTypes.size());
        }
    }
    /**
     * 同步协议内容到缓存中去
     * @param protocolTypes
     * @since  2017/11/8  20:21
     */
    private void synProtocolTypeToCache(List<ProtocolType> protocolTypes){
        if(protocolTypes!=null&&protocolTypes.size()>0){
            StringBuilder redisKey= new StringBuilder();
            for (ProtocolType pro:protocolTypes) {
                //组装redis的key
                redisKey.append(CollectContants.PROTOCOL_TYPE_REDIS_PREFIX)
                        .append(pro.getStartTag())
                        .append("_").append(pro.getPort());
                //把数据存到redis中
                JedisClient client = JedisClient.getJedisClient();
                client.oHset(redisKey.toString(),pro);
                //清空redisKey
                redisKey.setLength(0);
            }
        }
    }
    @Override
    public List<Map> queryNameIdDic()throws BaseException
    {
        return typeMapper.selectTypeNameIdDic();
    }

}
