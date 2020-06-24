package com.yidcloud.api.service;



import com.lsy.mybatisplus.plugins.Page;
import com.lsy.mybatisplus.service.IService;
import com.yidcloud.api.entity.VehicleModel;

import java.util.Map;

/**
 * @author chenyanfa@edenep.net
 * @version 2.0
 * @description: 车辆型号表 服务类 (用一句话描述该类做什么)
 * @copyright: Copyright (c) 2018
 * @company: 易登科技
 * @since 2018/01/19 11:49:06
 */
public interface IVehicleModelService extends IService<VehicleModel> {

    Page<VehicleModel> queryList(VehicleModel vehicleModel, Map<String, String> params, int current, int pageSize);

    Page<VehicleModel> queryVehicleModelInfoList(VehicleModel vehicleModel, Map<String, String> params, int current, int pageSize);

}
