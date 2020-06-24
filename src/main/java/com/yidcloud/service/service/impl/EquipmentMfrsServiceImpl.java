package com.yidcloud.service.service.impl;

import java.util.List;
import java.util.Map;

import com.lsy.base.string.StringHelper;
import com.lsy.mybatisplus.mapper.EntityWrapper;
import com.lsy.mybatisplus.plugins.Page;
import com.lsy.mybatisplus.service.impl.ServiceImpl;
import com.yidcloud.api.entity.EquipmentMfrs;
import com.yidcloud.api.service.IEquipmentMfrsService;

import com.yidcloud.service.mapper.EquipmentMfrsMapper;


/**
 * @author 胡洪瑜 huhongyu@edenep.net
 * @version 2.0
 * @description: 设备厂商 服务实现类 (用一句话描述该类做什么)
 * @copyright: Copyright (c) 2017
 * @company: 易登科技
 * @since 2017/11/28 15:50:13
 */
public class EquipmentMfrsServiceImpl extends ServiceImpl<EquipmentMfrsMapper, EquipmentMfrs> implements
        IEquipmentMfrsService {
    @Override
    public Page<EquipmentMfrs> queryList(EquipmentMfrs equipmentMfrs, Map<String, String> params, int current, int pageSize) {
        EntityWrapper<EquipmentMfrs> entityWrapper = new EntityWrapper<EquipmentMfrs>();
        entityWrapper.setEntity(equipmentMfrs);
        if (equipmentMfrs != null) {
            if (StringHelper.isNotBlank(params.get("startCreateDate")) && StringHelper.isNotBlank(params.get("endCreateDate"))) {
                entityWrapper.between("create_date", params.get("startCreateDate"), params.get("endCreateDate"));
            } else if (StringHelper.isNotBlank(params.get("startCreateDate"))) {
                entityWrapper.ge("create_date", params.get("startCreateDate"));
            } else if (StringHelper.isNotBlank(params.get("endCreateDate"))) {
                entityWrapper.le("create_date", params.get("endCreateDate"));
            }
            if (StringHelper.isNotBlank(params.get("startUpdateDate")) && StringHelper.isNotBlank(params.get("endUpdateDate"))) {
                entityWrapper.between("update_date", params.get("startUpdateDate"), params.get("endUpdateDate"));
            } else if (StringHelper.isNotBlank(params.get("startUpdateDate"))) {
                entityWrapper.ge("update_date", params.get("startUpdateDate"));
            } else if (StringHelper.isNotBlank(params.get("endUpdateDate"))) {
                entityWrapper.le("update_date", params.get("endUpdateDate"));
            }
        }
        return this.selectPage(new Page<EquipmentMfrs>(current, pageSize), entityWrapper);
    }

    /**
     * 根据mfrsName搜索设备厂商（返回前10条）
     *
     * @param mfrsName
     * @return
     */
    @Override
    public List<EquipmentMfrs> searchEquipmentMfrs(String mfrsName) {
        EntityWrapper<EquipmentMfrs> entityWrapper = new EntityWrapper<EquipmentMfrs>();
        entityWrapper.like("mfrs_name", mfrsName);
        Page<EquipmentMfrs> page = this.selectPage(new Page<EquipmentMfrs>(1, 10), entityWrapper);
        return page.getRecords();
    }
}
