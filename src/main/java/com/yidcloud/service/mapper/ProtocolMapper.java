package com.yidcloud.service.mapper;



import com.lsy.mybatisplus.mapper.BaseMapper;
import com.yidcloud.api.entity.Protocol;

import java.util.List;
import java.util.Map;

/**
 * @description:  协议表（指令表，相当于一个一个的请求接口信息） mapper接口 (用一句话描述该类做什么)
 * @copyright: Copyright (c) 2017
 * @company: 易登科技
 * @author 胡洪瑜
 * @version 2.0
 * @since 2017/11/08 15:55:49
 */
public interface ProtocolMapper extends BaseMapper<Protocol> {
    /**
     * 查询所有某一个状态下的协议
     * @param activeStatus 协议激活状态
     * @return List<Map,Object>
     * @since  2017/11/8  20:44
     */
    List<Map> selectAllProtocol(String activeStatus);

    /**
     * 查询指定参数的协议
     * @return List<Map,Object>
     * @param map
     * @since  2017/11/8  21:03
     */
    List<Map> selectProtocols(Map map);

    List<Map>selectProNameIdDic();
}
