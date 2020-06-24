package com.yidcloud.api.entity;

import com.lsy.mybatisplus.annotations.TableField;
import com.lsy.mybatisplus.annotations.TableId;
import com.lsy.mybatisplus.annotations.TableName;
import com.lsy.mybatisplus.enums.IdType;

import java.io.Serializable;
import java.util.Arrays;



/**
 * @author 胡洪瑜 huhongyu@edenep.net
 * @version 2.0
 * @description: 设备型号，比如华为p7，iphone X (用一句话描述该类做什么)
 * @copyright: Copyright (c) 2017
 * @company: 易登科技
 * @since 2017/11/28 11:47:59
 */
@TableName("t_hwy_cot_equipment_model")
public class EquipmentModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 型号名称
     */
    @TableField("model_name")
    private String modelName;
    /**
     * 型号编号
     */
    @TableField("model_code")
    private String modelCode;
    /**
     * 协议类型ID
     */
    @TableField("protocol_type_id")
    private Integer protocolTypeId;
    /**
     * 设备厂商id
     */
    @TableField("producer_id")
    private Integer producerId;
    /**
     * 设备类型，取数据字段
     */
    @TableField("equipment_type")
    private String equipmentType;
    /**
     * 可用年限
     */
    @TableField("durable_years")
    private Integer durableYears;
    /**
     * 扩展字段json
     */
    @TableField("ext_json")
    private byte[] extJson;
    /**
     * 创建时间
     */
    @TableField("create_date")
    private String createDate;
    /**
     * 创建人
     */
    @TableField("create_by")
    private String createBy;
    /**
     * 修改时间
     */
    @TableField("update_date")
    private String updateDate;
    /**
     * 修改人
     */
    @TableField("update_by")
    private String updateBy;
    
    /**
     * 是否GPS设备
     */
    @TableField("is_gps")
    private String isGpsModel;


    /**
     * 协议类型名称
     */
    @TableField(exist = false)
    private String protocolTypeName;
    /**
     * 设备厂商名称
     */
    @TableField(exist = false)
    private String producerName;
    /**
     * /**
     * 设备类型名称
     */
    @TableField(exist = false)
    private String equipmentTypeName;
    /**
     * 创建人名称
     */
    @TableField(exist = false)
    private String createByName;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelCode() {
        return modelCode;
    }

    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }

    public Integer getProtocolTypeId() {
        return protocolTypeId;
    }

    public void setProtocolTypeId(Integer protocolTypeId) {
        this.protocolTypeId = protocolTypeId;
    }

    public Integer getProducerId() {
        return producerId;
    }

    public void setProducerId(Integer producerId) {
        this.producerId = producerId;
    }

    public String getEquipmentType() {
        return equipmentType;
    }

    public void setEquipmentType(String equipmentType) {
        this.equipmentType = equipmentType;
    }

    public Integer getDurableYears() {
        return durableYears;
    }

    public void setDurableYears(Integer durableYears) {
        this.durableYears = durableYears;
    }

    public byte[] getExtJson() {
        return extJson;
    }

    public void setExtJson(byte[] extJson) {
        this.extJson = extJson;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public String getProtocolTypeName() {
        return protocolTypeName;
    }

    public void setProtocolTypeName(String protocolTypeName) {
        this.protocolTypeName = protocolTypeName;
    }

    public String getProducerName() {
        return producerName;
    }

    public void setProducerName(String producerName) {
        this.producerName = producerName;
    }

    public String getEquipmentTypeName() {
        return equipmentTypeName;
    }

    public void setEquipmentTypeName(String equipmentTypeName) {
        this.equipmentTypeName = equipmentTypeName;
    }

    public String getCreateByName() {
        return createByName;
    }

    public void setCreateByName(String createByName) {
        this.createByName = createByName;
    }
    
    public String getIsGpsModel() {
        return isGpsModel;
    }
    
    public void setIsGpsModel(String isGpsModel) {
        this.isGpsModel = isGpsModel;
    }

    @Override
    public String toString() {
        return "EquipmentModel{" +
                "id=" + id +
                ", modelName='" + modelName + '\'' +
                ", modelCode='" + modelCode + '\'' +
                ", protocolTypeId=" + protocolTypeId +
                ", producerId=" + producerId +
                ", equipmentType='" + equipmentType + '\'' +
                ", durableYears=" + durableYears +
                ", extJson=" + Arrays.toString(extJson) +
                ", createDate='" + createDate + '\'' +
                ", createBy='" + createBy + '\'' +
                ", updateDate='" + updateDate + '\'' +
                ", updateBy='" + updateBy + '\'' +
                ", protocolTypeName='" + protocolTypeName + '\'' +
                ", producerName='" + producerName + '\'' +
                ", equipmentTypeName='" + equipmentTypeName + '\'' +
                ", createByName='" + createByName + '\'' +
                '}';
    }
}
