package com.yidcloud.api.service;



import com.lsy.mybatisplus.service.IService;
import com.yidcloud.api.entity.EquipmentModelAttdef;

import java.util.List;
import java.util.Map;

/**
 * @description:  设备型号属性服务接口
 * @copyright: Copyright (c) 2017
 * @company: 易登科技
 * @author 陈炎发 chenyanfa@edenep.net
 * @version 2.0
 * @since 2018/1/13 17:12
 */
public interface IEquipmentModelAttdefService extends IService<EquipmentModelAttdef> {

	List<EquipmentModelAttdef> queryList(Map<String, Object> params);


}
