package com.yidcloud.api.service;



import com.lsy.base.exception.BaseException;
import com.lsy.mybatisplus.plugins.Page;
import com.lsy.mybatisplus.service.IService;
import com.yidcloud.api.entity.Protocol;

import java.util.List;
import java.util.Map;

/**
 * @description:  协议表（指令表，相当于一个一个的请求接口信息） 服务类 (用一句话描述该类做什么)
 * @copyright: Copyright (c) 2017
 * @company: 易登科技
 * @author 胡洪瑜
 * @version 2.0
 * @since 2017/11/08 17:09:17
 */
public interface IProtocolService extends IService<Protocol> {
    Page<Protocol> queryList(Protocol protocol, Map<String,String> params, int current, int pageSize);
    /**
     * 把所有的协议信息同步到缓存中
     * @since  2017/11/8  17:16
     */
    void synProtocolToCache();
    /**
     * 同步指定的协议信息到缓存
     * @return
     * @param protocolIds
     * @since  2017/11/8  17:16
     */
    void synProtocolToCache(String [] protocolIds);

    /**
     * 查询名字和id的字典
     * @return
     * @throws BaseException
     */
    public List<Map> selectProNameIdDic()throws BaseException;
}
