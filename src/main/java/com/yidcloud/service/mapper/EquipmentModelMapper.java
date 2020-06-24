package com.yidcloud.service.mapper;


import com.lsy.mybatisplus.mapper.BaseMapper;
import com.lsy.mybatisplus.plugins.pagination.Pagination;
import com.yidcloud.api.entity.EquipmentModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description:  设备型号 mapper接口 (用一句话描述该类做什么)
 * @copyright: Copyright (c) 2017
 * @company: 易登科技
 * @author 胡洪瑜
 * @version 2.0
 * @since 2017/11/08 15:55:49
 */
public interface EquipmentModelMapper extends BaseMapper<EquipmentModel> {
    List<EquipmentModel> queryEquipmentModelInfoList(Pagination pagination, @Param("equipmentModel") EquipmentModel equipmentModel);
}
