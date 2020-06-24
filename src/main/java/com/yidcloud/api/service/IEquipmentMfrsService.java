package com.yidcloud.api.service;

import com.lsy.mybatisplus.plugins.Page;
import com.lsy.mybatisplus.service.IService;
import com.yidcloud.api.entity.EquipmentMfrs;

import java.util.List;
import java.util.Map;



/**
 * @author 胡洪瑜 huhongyu@edenep.net
 * @version 2.0
 * @description: 设备厂商 服务类 (用一句话描述该类做什么)
 * @copyright: Copyright (c) 2017
 * @company: 易登科技
 * @since 2017/11/28 15:50:13
 */
public interface IEquipmentMfrsService extends IService<EquipmentMfrs> {

    public Page<EquipmentMfrs> queryList(EquipmentMfrs equipmentMfrs, Map<String, String> params,
                                         int current, int pageSize);

    /**
     * 根据mfrsName搜索设备厂商（返回前10条）
     *
     * @param mfrsName
     * @return
     */
    List<EquipmentMfrs> searchEquipmentMfrs(String mfrsName);

}
