package com.yidcloud.api.service;



import com.lsy.mybatisplus.plugins.Page;
import com.lsy.mybatisplus.service.IService;
import com.yidcloud.api.entity.EquipmentModel;

import java.util.List;
import java.util.Map;

/**
 * @author 胡洪瑜 huhongyu@edenep.net
 * @version 2.0
 * @description: 设备型号，比如华为p7，iphone X 服务类 (用一句话描述该类做什么)
 * @copyright: Copyright (c) 2017
 * @company: 易登科技
 * @since 2017/11/28 11:47:59
 */
public interface IEquipmentModelService extends IService<EquipmentModel> {

    Page<EquipmentModel> queryList(EquipmentModel equipmentModel, Map<String, String> params, int current, int pageSize);

    /**
     * 查询设备型号列表
     * @param equipmentModel
     * @param params
     * @param current
     * @param pageSize
     * @return
     */
    Page<EquipmentModel> querEquipmentModelList(EquipmentModel equipmentModel, Map<String, String> params, int current, int pageSize);

    /**
     * 根据设备厂商，设备型号名称搜索设备型号（返回前10条）
     *
     * @param producerId
     * @param modelName
     * @return
     */
    List<EquipmentModel> searchEquipmentModels(Integer producerId, String modelName);
    /**
     * 新增设备型号，返回id
     * @param equipmentModel
     * @return
     */
    Integer insertReturnId(EquipmentModel equipmentModel);
}
