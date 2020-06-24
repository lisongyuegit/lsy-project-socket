package com.yidcloud.api.entity;



import com.lsy.mybatisplus.annotations.TableField;
import com.lsy.mybatisplus.annotations.TableId;
import com.lsy.mybatisplus.annotations.TableName;
import com.lsy.mybatisplus.enums.IdType;

import java.io.Serializable;

/**
 * @description:  设备型号属性定义
 * @copyright: Copyright (c) 2017
 * @company: 易登科技
 * @author 陈炎发 chenyanfa@edenep.net
 * @version 2.0
 * @since 2018/1/13 17:12
 */
@TableName("t_hwy_cot_equipment_model_attdef")
public class EquipmentModelAttdef implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
	@TableId(value="id", type= IdType.AUTO)
	private Integer id;
	/**
	 * 设备型号内码
	 */
	@TableField("model_code")
	private String modelCode;
	/**
	 * 属性显示名
	 */
	@TableField("attribute_name")
	private String attributeName;
	/**
	 * 属性代码名
	 */
	@TableField("attribute_code")
	private String attributeCode;
	/**
	 * 属性数据类型(数据字典)
	 */
	@TableField("attribute_data_type")
	private String attributeDataType;
	/**
	 * 属性值
	 */
	@TableField("attribute_value")
	private String attributeValue;


	public EquipmentModelAttdef() {
	}

	public EquipmentModelAttdef(String modelCode, String attributeName, String attributeCode, String attributeDataType, String attributeValue) {
		this.modelCode = modelCode;
		this.attributeName = attributeName;
		this.attributeCode = attributeCode;
		this.attributeDataType = attributeDataType;
		this.attributeValue = attributeValue;
	}

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

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public String getAttributeCode() {
		return attributeCode;
	}

	public void setAttributeCode(String attributeCode) {
		this.attributeCode = attributeCode;
	}

	public String getAttributeDataType() {
		return attributeDataType;
	}

	public void setAttributeDataType(String attributeDataType) {
		this.attributeDataType = attributeDataType;
	}

	public String getAttributeValue() {
		return attributeValue;
	}

	public void setAttributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}

	@Override
	public String toString() {
		return "Equipment{" +
				"id=" + id +
				", modelCode='" + modelCode + '\'' +
				", attributeName='" + attributeName + '\'' +
				", attributeCode='" + attributeCode + '\'' +
				", attributeDataType='" + attributeDataType + '\'' +
				", attributeValue='" + attributeValue + '\'' +
				'}';
	}
}
