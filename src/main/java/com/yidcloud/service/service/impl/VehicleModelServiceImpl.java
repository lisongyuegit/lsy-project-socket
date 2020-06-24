package com.yidcloud.service.service.impl;


import com.lsy.mybatisplus.mapper.EntityWrapper;
import com.lsy.mybatisplus.plugins.Page;
import com.lsy.mybatisplus.service.impl.ServiceImpl;
import com.yidcloud.api.entity.VehicleModel;
import com.yidcloud.api.service.IVehicleModelService;
import com.yidcloud.service.mapper.VehicleModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author chenyanfa@edenep.net
 * @version 2.0
 * @description: 车辆型号表 服务实现类
 * @copyright: Copyright (c) 2018
 * @company: 易登科技
 * @since 2018/01/19 11:49:06
 */
@Service
public class VehicleModelServiceImpl extends ServiceImpl<VehicleModelMapper, VehicleModel> implements IVehicleModelService {

    @Autowired
    public VehicleModelMapper vehicleModelMapper;

    @Override
    public Page<VehicleModel> queryList(VehicleModel vehicleModel, Map<String, String> params, int current, int pageSize) {
        EntityWrapper<VehicleModel> entityWrapper = new EntityWrapper<VehicleModel>();
        entityWrapper.setEntity(vehicleModel);
        return this.selectPage(new Page<VehicleModel>(current, pageSize), entityWrapper);
    }

    @Override
    public Page<VehicleModel> queryVehicleModelInfoList(VehicleModel vehicleModel, Map<String, String> params, int current, int pageSize) {
        Page<VehicleModel> page = new Page<>(current, pageSize);
        List<VehicleModel> vehicleModelList = vehicleModelMapper.queryVehicleModelInfoList(page, vehicleModel);
        page.setRecords(vehicleModelList);
        return page;
    }
}
