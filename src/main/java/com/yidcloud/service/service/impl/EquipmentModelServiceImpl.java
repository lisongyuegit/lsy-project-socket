package com.yidcloud.service.service.impl;

import java.util.List;
import java.util.Map;

import com.lsy.base.string.StringHelper;
import com.lsy.mybatisplus.mapper.EntityWrapper;
import com.lsy.mybatisplus.plugins.Page;
import com.lsy.mybatisplus.service.impl.ServiceImpl;
import com.yidcloud.api.entity.EquipmentModel;
import com.yidcloud.api.service.IEquipmentModelService;
import com.yidcloud.service.mapper.EquipmentModelMapper;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author 胡洪瑜
 * @version 2.0
 * @description: 设备型号 服务实现类 (用一句话描述该类做什么)
 * @copyright: Copyright (c) 2017
 * @company: 易登科技
 * @since 2017/11/08 17:09:17
 */
public class EquipmentModelServiceImpl extends ServiceImpl<EquipmentModelMapper, EquipmentModel> implements IEquipmentModelService {
    @Autowired
    public EquipmentModelMapper equipmentModelMapper;

    @Override
    public Page<EquipmentModel> queryList(EquipmentModel equipmentModel, Map<String, String> params, int current, int pageSize) {
        EntityWrapper<EquipmentModel> entityWrapper = new EntityWrapper<EquipmentModel>();
        entityWrapper.setEntity(equipmentModel);
        if (equipmentModel != null) {
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
        return this.selectPage(new Page<EquipmentModel>(current, pageSize), entityWrapper);
    }

    /**
     * 查询设备型号列表
     *
     * @param equipmentModel
     * @param params
     * @param current
     * @param pageSize
     * @return
     */
    @Override
    public Page<EquipmentModel> querEquipmentModelList(EquipmentModel equipmentModel, Map<String, String> params, int current, int pageSize) {
        Page<EquipmentModel> page = new Page<EquipmentModel>(current, pageSize);
        page.setRecords(equipmentModelMapper.queryEquipmentModelInfoList(page,equipmentModel));
        return page;
    }

    /**
     * 根据设备厂商，设备型号名称搜索设备型号（返回前10条）
     *
     * @param producerId
     * @param modelName
     * @return
     */
    @Override
    public List<EquipmentModel> searchEquipmentModels(Integer producerId, String modelName) {
        EntityWrapper<EquipmentModel> entityWrapper = new EntityWrapper<EquipmentModel>();
        if (producerId != null && producerId.intValue() > 0) {
            entityWrapper.eq("producer_id", producerId);
        }
        entityWrapper.like("model_name", modelName);
        Page<EquipmentModel> page = this.selectPage(new Page<EquipmentModel>(1, 10), entityWrapper);
        return page.getRecords();
    }

    /**
     * 新增设备型号，返回id
     *
     * @param equipmentModel
     * @return
     */
    @Override
    public Integer insertReturnId(EquipmentModel equipmentModel) {
        int row = equipmentModelMapper.insert(equipmentModel);
        if (row > 0) {
            return equipmentModel.getId();
        }

        return null;
    }
}
