package com.yidcloud.service.mapper;



import com.lsy.mybatisplus.mapper.BaseMapper;
import com.yidcloud.api.entity.ProtocolType;

import java.util.List;
import java.util.Map;

/**
 * @description:  协议类型表 mapper接口 (用一句话描述该类做什么)
 * @copyright: Copyright (c) 2017
 * @company: 易登科技
 * @author 胡洪瑜
 * @version 2.0
 * @since 2017/11/08 15:55:49
 */
public interface ProtocolTypeMapper extends BaseMapper<ProtocolType> {
    List<Map> selectTypeNameIdDic();
}
