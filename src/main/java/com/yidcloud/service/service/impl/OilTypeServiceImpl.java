package com.yidcloud.service.service.impl;

import java.util.Map;

import com.lsy.mybatisplus.mapper.EntityWrapper;
import com.lsy.mybatisplus.plugins.Page;

import com.lsy.mybatisplus.service.impl.ServiceImpl;
import com.yidcloud.api.entity.OilType;
import com.yidcloud.api.service.IOilTypeService;
import com.yidcloud.service.mapper.OilTypeMapper;
import org.springframework.stereotype.Service;


/**
 * 油箱类型 服务实现类
 * @description:  油箱类型 服务实现类 (用一句话描述该类做什么)
 * @copyright: Copyright (c) 2018
 * @company: 易登科技
 * @author huhongyu@edenep.net
 * @version 2.0
 * @since 2018/06/06 20:06:31
 */
@Service
public class OilTypeServiceImpl extends ServiceImpl<OilTypeMapper, OilType> implements IOilTypeService {
	
	@Override
	public Page<OilType> queryList(OilType oilType, Map<String,String> params, int current, int pageSize){
		EntityWrapper<OilType> entityWrapper = new EntityWrapper<OilType>();
		entityWrapper.like("oil_name",oilType.getOilName());
		return this.selectPage(new Page<>(current, pageSize), entityWrapper);
	}

	@Override
	public OilType insertOne(OilType oilType) {
		this.insert(oilType);
		return oilType;
	}
}
