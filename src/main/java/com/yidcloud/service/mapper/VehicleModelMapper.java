package com.yidcloud.service.mapper;


import com.lsy.mybatisplus.mapper.BaseMapper;
import com.lsy.mybatisplus.plugins.Page;
import com.yidcloud.api.entity.VehicleModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description:  车辆型号表 mapper接口
 * @copyright: Copyright (c) 2018
 * @company: 易登科技
 * @author chenyanfa@edenep.net
 * @version 2.0
 * @since 2018/01/19 11:49:06
 */
public interface VehicleModelMapper extends BaseMapper<VehicleModel> {
    List<VehicleModel> queryVehicleModelInfoList(Page page, @Param("vehicleModel") VehicleModel vehicleModel);
}
