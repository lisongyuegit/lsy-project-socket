package com.yidcloud.api.service;



import com.lsy.mybatisplus.plugins.Page;
import com.lsy.mybatisplus.service.IService;
import com.yidcloud.api.entity.ProtocolAnalysis;

import java.util.Map;

/**
 * @description:  协议解码表(每一个字段的解析规则) 服务类 (用一句话描述该类做什么)
 * @copyright: Copyright (c) 2017
 * @company: 易登科技
 * @author 胡洪瑜
 * @version 2.0
 * @since 2017/11/08 17:09:17
 */
public interface IProtocolAnalysisService extends IService<ProtocolAnalysis> {
    Page<ProtocolAnalysis> queryList(ProtocolAnalysis protocolAnalysis, Map<String,String> params, int current, int pageSize);

}
