package com.yidcloud.service.service.impl;


import com.lsy.base.string.StringHelper;
import com.lsy.mybatisplus.mapper.EntityWrapper;
import com.lsy.mybatisplus.plugins.Page;
import com.lsy.mybatisplus.service.impl.ServiceImpl;
import com.yidcloud.api.entity.EquipmentGroup;
import com.yidcloud.api.service.IEquipmentGroupService;
import com.yidcloud.service.mapper.EquipmentGroupMapper;

import java.util.Map;

/**
 * @description:  设备组 服务实现类 (用一句话描述该类做什么)
 * @copyright: Copyright (c) 2017
 * @company: 易登科技
 * @author 胡洪瑜
 * @version 2.0
 * @since 2017/11/08 17:09:17
 */
public class EquipmentGroupServiceImpl extends ServiceImpl<EquipmentGroupMapper, EquipmentGroup> implements IEquipmentGroupService {
    @Override
    public Page<EquipmentGroup> queryList(EquipmentGroup equipmentGroup, Map<String,String> params, int current, int pageSize){
        EntityWrapper<EquipmentGroup> entityWrapper = new EntityWrapper<EquipmentGroup>();
        entityWrapper.setEntity(equipmentGroup);
        if (equipmentGroup != null) {
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
        return this.selectPage(new Page<EquipmentGroup>(current, pageSize), entityWrapper);
    }
}
