package com.yidcloud.service.service.impl;



import com.lsy.base.string.StringHelper;
import com.lsy.mybatisplus.mapper.EntityWrapper;
import com.lsy.mybatisplus.plugins.Page;
import com.lsy.mybatisplus.service.impl.ServiceImpl;
import com.yidcloud.api.entity.ProtocolAnalysis;
import com.yidcloud.api.service.IProtocolAnalysisService;
import com.yidcloud.service.mapper.ProtocolAnalysisMapper;

import java.util.Map;

/**
 * @description:  协议解码表(每一个字段的解析规则) 服务实现类 (用一句话描述该类做什么)
 * @copyright: Copyright (c) 2017
 * @company: 易登科技
 * @author 胡洪瑜
 * @version 2.0
 * @since 2017/11/08 17:09:17
 */
public class ProtocolAnalysisServiceImpl extends ServiceImpl<ProtocolAnalysisMapper, ProtocolAnalysis> implements IProtocolAnalysisService {
    @Override
    public Page<ProtocolAnalysis> queryList(ProtocolAnalysis protocolAnalysis, Map<String,String> params, int current, int pageSize) {
        EntityWrapper<ProtocolAnalysis> entityWrapper = new EntityWrapper<ProtocolAnalysis>();
        entityWrapper.setEntity(protocolAnalysis);
        if (protocolAnalysis != null) {
            if (StringHelper.isNotBlank(params.get("startCreateDate")) && StringHelper
                    .isNotBlank(params.get("endCreateDate"))) {
                entityWrapper.between("create_date", params.get("startCreateDate"),
                        params.get("endCreateDate"));
            } else if (StringHelper.isNotBlank(params.get("startCreateDate"))) {
                entityWrapper.ge("create_date", params.get("startCreateDate"));
            } else if (StringHelper.isNotBlank(params.get("endCreateDate"))) {
                entityWrapper.le("create_date", params.get("endCreateDate"));
            }
            if (StringHelper.isNotBlank(params.get("startUpdateDate")) && StringHelper
                    .isNotBlank(params.get("endUpdateDate"))) {
                entityWrapper.between("update_date", params.get("startUpdateDate"),
                        params.get("endUpdateDate"));
            } else if (StringHelper.isNotBlank(params.get("startUpdateDate"))) {
                entityWrapper.ge("update_date", params.get("startUpdateDate"));
            } else if (StringHelper.isNotBlank(params.get("endUpdateDate"))) {
                entityWrapper.le("update_date", params.get("endUpdateDate"));
            }
        }
        return this.selectPage(new Page<ProtocolAnalysis>(current, pageSize), entityWrapper);
    }
}
