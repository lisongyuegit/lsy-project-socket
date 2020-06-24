package com.yidcloud.api.entity;



import com.lsy.mybatisplus.annotations.TableField;
import com.lsy.mybatisplus.annotations.TableId;
import com.lsy.mybatisplus.annotations.TableName;
import com.lsy.mybatisplus.enums.IdType;

import java.io.Serializable;

/**
 * @author chenyanfa@edenep.net
 * @version 2.0
 * @description: 车辆型号表
 * @copyright: Copyright (c) 2018
 * @company: 易登科技
 * @since 2018/01/19 11:49:06
 */
@TableName("t_hwy_cot_vehicle_model")
public class VehicleModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 车辆型号内码
     */
    @TableField("model_code")
    private String modelCode;
    /**
     * 车辆型号名称
     */
    @TableField("model_name")
    private String modelName;
    /**
     * 设备厂商
     */
    @TableField("equipment_mfrs")
    private Integer equipmentMfrs;
    /**
     * 车辆类型, 来源数据字典
     */
    @TableField("vehicle_type")
    private String vehicleType;
    /**
     * 油箱容量
     */
    @TableField("fuel_tank_capacity")
    private Double fuelTankCapacity;
    /**
     * 油箱长度
     */
    @TableField("fuel_tank_length")
    private Double fuelTankLength;
    /**
     * 油箱宽度
     */
    @TableField("fuel_tank_width")
    private Double fuelTankWidth;
    /**
     * 油箱高度
     */
    @TableField("fuel_tank_height")
    private Double fuelTankHeight;
    /**
     * 波动值
     */
    @TableField("fluctuate_value")
    private Double fluctuateValue;
    /**
     * 油箱形状, 来源数据字典
     */
    @TableField("fuel_tank_shape")
    private String fuelTankShape;
    /**
     * 燃料类型, 来源数据字典
     */
    @TableField("fuel_type")
    private String fuelType;
    /**
     * 传感器类型, 来源数据字典
     */
    @TableField("sensor_type")
    private String sensorType;
    /**
     * 开关量
     */
    @TableField("switch_value")
    private String switchValue;
    /**
     * 排放标准
     */
    @TableField("emission_standard")
    private Double emissionStandard;
    /**
     * 发动机型号
     */
    @TableField("engine_model")
    private String engineModel;
    /**
     * 发动机排量
     */
    @TableField("engine_displacement")
    private Double engineDisplacement;
    /**
     * 速度限制
     */
    @TableField("rate_limitation")
    private Double rateLimitation;
    /**
     * 额定功率
     */
    @TableField("rated_power")
    private Double ratedPower;
    /**
     * 百公里油耗
     */
    @TableField("fuel_consumption_for_100km")
    private Double fuelConsumptionFor100km;
    /**
     * 车宽
     */
    @TableField("vehicle_width")
    private Double vehicleWidth;
    /**
     * 车高
     */
    @TableField("vehicle_height")
    private Double vehicleHeight;
    /**
     * 车长
     */
    @TableField("vehicle_length")
    private Double vehicleLength;
    /**
     * 总质量
     */
    @TableField("total_weight")
    private Double totalWeight;
    /**
     * 整备质量
     */
    @TableField("curb_weight")
    private Double curbWeight;
    /**
     * 核定载质量
     */
    @TableField("ratified_load_weight")
    private Double ratifiedLoadWeight;
    /**
     * 底盘类型
     */
    @TableField("chassis_type")
    private String chassisType;
    /**
     * 准牵引总质量
     */
    @TableField("traction_total_weight")
    private Double tractionTotalWeight;
    /**
     * 核定载人数
     */
    @TableField("ratified_load_number_of_people")
    private Integer ratifiedLoadNumberOfPeople;
    /**
     * 图片
     */
    @TableField("vehicle_image")
    private String vehicleImage;

    @TableField("create_date")
    private String createDate;
    @TableField("create_by")
    private String createBy;
    @TableField("update_date")
    private String updateDate;
    @TableField("update_by")
    private String updateBy;

    @TableField(exist = false)
    private String equipmentMfrsName;

    @TableField(exist = false)
    private String equipmentModelName;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getModelCode() {
        return modelCode;
    }

    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Integer getEquipmentMfrs() {
        return equipmentMfrs;
    }

    public void setEquipmentMfrs(Integer equipmentMfrs) {
        this.equipmentMfrs = equipmentMfrs;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Double getFuelTankCapacity() {
        return fuelTankCapacity;
    }

    public void setFuelTankCapacity(Double fuelTankCapacity) {
        this.fuelTankCapacity = fuelTankCapacity;
    }

    public Double getFuelTankLength() {
        return fuelTankLength;
    }

    public void setFuelTankLength(Double fuelTankLength) {
        this.fuelTankLength = fuelTankLength;
    }

    public Double getFuelTankWidth() {
        return fuelTankWidth;
    }

    public void setFuelTankWidth(Double fuelTankWidth) {
        this.fuelTankWidth = fuelTankWidth;
    }

    public Double getFuelTankHeight() {
        return fuelTankHeight;
    }

    public void setFuelTankHeight(Double fuelTankHeight) {
        this.fuelTankHeight = fuelTankHeight;
    }

    public Double getFluctuateValue() {
        return fluctuateValue;
    }

    public void setFluctuateValue(Double fluctuateValue) {
        this.fluctuateValue = fluctuateValue;
    }

    public String getFuelTankShape() {
        return fuelTankShape;
    }

    public void setFuelTankShape(String fuelTankShape) {
        this.fuelTankShape = fuelTankShape;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public String getSwitchValue() {
        return switchValue;
    }

    public void setSwitchValue(String switchValue) {
        this.switchValue = switchValue;
    }

    public Double getEmissionStandard() {
        return emissionStandard;
    }

    public void setEmissionStandard(Double emissionStandard) {
        this.emissionStandard = emissionStandard;
    }

    public String getEngineModel() {
        return engineModel;
    }

    public void setEngineModel(String engineModel) {
        this.engineModel = engineModel;
    }

    public Double getEngineDisplacement() {
        return engineDisplacement;
    }

    public void setEngineDisplacement(Double engineDisplacement) {
        this.engineDisplacement = engineDisplacement;
    }

    public Double getRateLimitation() {
        return rateLimitation;
    }

    public void setRateLimitation(Double rateLimitation) {
        this.rateLimitation = rateLimitation;
    }

    public Double getRatedPower() {
        return ratedPower;
    }

    public void setRatedPower(Double ratedPower) {
        this.ratedPower = ratedPower;
    }

    public Double getFuelConsumptionFor100km() {
        return fuelConsumptionFor100km;
    }

    public void setFuelConsumptionFor100km(Double fuelConsumptionFor100km) {
        this.fuelConsumptionFor100km = fuelConsumptionFor100km;
    }

    public Double getVehicleWidth() {
        return vehicleWidth;
    }

    public void setVehicleWidth(Double vehicleWidth) {
        this.vehicleWidth = vehicleWidth;
    }

    public Double getVehicleHeight() {
        return vehicleHeight;
    }

    public void setVehicleHeight(Double vehicleHeight) {
        this.vehicleHeight = vehicleHeight;
    }

    public Double getVehicleLength() {
        return vehicleLength;
    }

    public void setVehicleLength(Double vehicleLength) {
        this.vehicleLength = vehicleLength;
    }

    public Double getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(Double totalWeight) {
        this.totalWeight = totalWeight;
    }

    public Double getCurbWeight() {
        return curbWeight;
    }

    public void setCurbWeight(Double curbWeight) {
        this.curbWeight = curbWeight;
    }

    public Double getRatifiedLoadWeight() {
        return ratifiedLoadWeight;
    }

    public void setRatifiedLoadWeight(Double ratifiedLoadWeight) {
        this.ratifiedLoadWeight = ratifiedLoadWeight;
    }

    public String getChassisType() {
        return chassisType;
    }

    public void setChassisType(String chassisType) {
        this.chassisType = chassisType;
    }

    public Double getTractionTotalWeight() {
        return tractionTotalWeight;
    }

    public void setTractionTotalWeight(Double tractionTotalWeight) {
        this.tractionTotalWeight = tractionTotalWeight;
    }

    public Integer getRatifiedLoadNumberOfPeople() {
        return ratifiedLoadNumberOfPeople;
    }

    public void setRatifiedLoadNumberOfPeople(Integer ratifiedLoadNumberOfPeople) {
        this.ratifiedLoadNumberOfPeople = ratifiedLoadNumberOfPeople;
    }

    public String getVehicleImage() {
        return vehicleImage;
    }

    public void setVehicleImage(String vehicleImage) {
        this.vehicleImage = vehicleImage;
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

    public String getEquipmentMfrsName() {
        return equipmentMfrsName;
    }

    public void setEquipmentMfrsName(String equipmentMfrsName) {
        this.equipmentMfrsName = equipmentMfrsName;
    }

    public String getEquipmentModelName() {
        return equipmentModelName;
    }

    public void setEquipmentModelName(String equipmentModelName) {
        this.equipmentModelName = equipmentModelName;
    }

    @Override
    public String toString() {
        return "VehicleModel{" +
                "id=" + id +
                ", modelCode='" + modelCode + '\'' +
                ", modelName='" + modelName + '\'' +
                ", equipmentMfrs=" + equipmentMfrs +
                ", vehicleType='" + vehicleType + '\'' +
                ", fuelTankCapacity=" + fuelTankCapacity +
                ", fuelTankLength=" + fuelTankLength +
                ", fuelTankWidth=" + fuelTankWidth +
                ", fuelTankHeight=" + fuelTankHeight +
                ", fluctuateValue=" + fluctuateValue +
                ", fuelTankShape='" + fuelTankShape + '\'' +
                ", fuelType='" + fuelType + '\'' +
                ", sensorType='" + sensorType + '\'' +
                ", switchValue='" + switchValue + '\'' +
                ", emissionStandard=" + emissionStandard +
                ", engineModel='" + engineModel + '\'' +
                ", engineDisplacement=" + engineDisplacement +
                ", rateLimitation=" + rateLimitation +
                ", ratedPower=" + ratedPower +
                ", fuelConsumptionFor100km=" + fuelConsumptionFor100km +
                ", vehicleWidth=" + vehicleWidth +
                ", vehicleHeight=" + vehicleHeight +
                ", vehicleLength=" + vehicleLength +
                ", totalWeight=" + totalWeight +
                ", curbWeight=" + curbWeight +
                ", ratifiedLoadWeight=" + ratifiedLoadWeight +
                ", chassisType='" + chassisType + '\'' +
                ", tractionTotalWeight=" + tractionTotalWeight +
                ", ratifiedLoadNumberOfPeople=" + ratifiedLoadNumberOfPeople +
                ", vehicleImage='" + vehicleImage + '\'' +
                ", createDate='" + createDate + '\'' +
                ", createBy='" + createBy + '\'' +
                ", updateDate='" + updateDate + '\'' +
                ", updateBy='" + updateBy + '\'' +
                '}';
    }
}
