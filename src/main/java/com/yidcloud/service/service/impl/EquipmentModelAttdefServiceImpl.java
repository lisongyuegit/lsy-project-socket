package com.yidcloud.service.service.impl;



import com.lsy.mybatisplus.service.impl.ServiceImpl;
import com.yidcloud.api.entity.EquipmentModelAttdef;
import com.yidcloud.api.service.IEquipmentModelAttdefService;
import com.yidcloud.service.mapper.EquipmentModeAttdeflMapper;

import java.util.List;
import java.util.Map;

/**
 * @description:  设备型号属性 服务实现类 (用一句话描述该类做什么)
 * @copyright: Copyright (c) 2017
 * @company: 易登科技
 * @author 陈炎发 chenyanfa@edenep.net
 * @version 2.0
 * @since 2018/1/13 17:12
 */
public class EquipmentModelAttdefServiceImpl extends ServiceImpl<EquipmentModeAttdeflMapper, EquipmentModelAttdef> implements IEquipmentModelAttdefService {

    @Override
    public List<EquipmentModelAttdef> queryList(Map<String, Object> params) {
        return this.selectByMap(params);
    }
}
