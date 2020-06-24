package com.yidcloud.api.service;

import com.lsy.mybatisplus.plugins.Page;
import com.lsy.mybatisplus.service.IService;
import com.yidcloud.api.entity.OilTypeStandard;

import java.util.List;
import java.util.Map;



/**
 * 油箱类型标的 服务类
 * @description:  油箱类型标的 服务类 (用一句话描述该类做什么)
 * @copyright: Copyright (c) 2018
 * @company: 易登科技
 * @author huhongyu@edenep.net
 * @version 2.0
 * @since 2018/06/06 20:06:31
 */
public interface IOilTypeStandardService extends IService<OilTypeStandard> {
	
	
	/**
     * queryList
     *      
     * @description:  (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: huhongyu@edenep.net
     * @version: 2.0
     * @date: 2018/06/06 20:06:31
     * @param oilTypeStandard
     * @param params
     * @param current
     * @param pageSize
     * @return Page<OilTypeStandard>
     */
	Page<OilTypeStandard> queryList(OilTypeStandard oilTypeStandard, Map<String, String> params, int current, int pageSize);
	
}
