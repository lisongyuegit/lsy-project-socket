package com.yidcloud.service.service.impl;

import java.util.Map;

import com.lsy.base.string.StringHelper;

import com.lsy.mybatisplus.mapper.EntityWrapper;
import com.lsy.mybatisplus.plugins.Page;
import com.lsy.mybatisplus.service.impl.ServiceImpl;
import com.yidcloud.api.entity.OilTypeStandard;
import com.yidcloud.api.service.IOilTypeStandardService;
import com.yidcloud.service.mapper.OilTypeStandardMapper;
import org.springframework.stereotype.Service;


/**
 * 油箱类型标的 服务实现类
 * @description:  油箱类型标的 服务实现类 (用一句话描述该类做什么)
 * @copyright: Copyright (c) 2018
 * @company: 易登科技
 * @author huhongyu@edenep.net
 * @version 2.0
 * @since 2018/06/06 20:06:31
 */
@Service
public class OilTypeStandardServiceImpl extends ServiceImpl<OilTypeStandardMapper, OilTypeStandard> implements IOilTypeStandardService {
	
	@Override
	public Page<OilTypeStandard> queryList(OilTypeStandard oilTypeStandard, Map<String,String> params, int current, int pageSize){
		EntityWrapper<OilTypeStandard> entityWrapper = new EntityWrapper<OilTypeStandard>();
		entityWrapper.setEntity(oilTypeStandard);
		if (oilTypeStandard != null) {
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
		return this.selectPage(new Page<OilTypeStandard>(current, pageSize), entityWrapper);
	}
}
