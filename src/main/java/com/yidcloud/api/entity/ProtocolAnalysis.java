package com.yidcloud.api.entity;


import com.lsy.mybatisplus.annotations.TableField;
import com.lsy.mybatisplus.annotations.TableId;
import com.lsy.mybatisplus.annotations.TableName;
import com.lsy.mybatisplus.enums.IdType;

import java.io.Serializable;

/**
 * @description:  协议解码表(每一个字段的解析规则) (用一句话描述该类做什么)
 * @copyright: Copyright (c) 2017
 * @company: 易登科技
 * @author 胡洪瑜
 * @version 2.0
 * @since 2017/11/14 09:51:47
 */
@TableName("t_hwy_cot_protocol_analysis")
public class ProtocolAnalysis implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 协议
     */
	@TableId(value="id", type= IdType.AUTO)
	private Integer id;
    /**
     * 协议ID
     */
	@TableField("protocol_id")
	private Integer protocolId;
    /**
     * 字段名称
     */
	@TableField("field_name")
	private String fieldName;
    /**
     * 字段java代码
     */
	@TableField("field_java_code")
	private String fieldJavaCode;
    /**
     * 字段java类型
     */
	@TableField("field_java_type")
	private String fieldJavaType;
    /**
     * 解码方法
     */
	@TableField("analysis_java_method")
	private String analysisJavaMethod;
    /**
     * 解码顺序（0：正序，1：倒序）
     */
	@TableField("order_type")
	private String orderType;
    /**
     * 字段开始位置
     */
	@TableField("field_start_position")
	private Integer fieldStartPosition;
    /**
     * 字段结束位置
     */
	@TableField("field_end_position")
	private Integer fieldEndPosition;
    /**
     * 字段长度
     */
	@TableField("field_size")
	private Integer fieldSize;
    /**
     * 字段类型（0：字段，n*${mid}:标识指令集合）
     */
	@TableField("field_type")
	private String fieldType;
	@TableField("create_date")
	private String createDate;
	@TableField("create_by")
	private String createBy;
	@TableField("update_date")
	private String updateDate;
	@TableField("update_by")
	private String updateBy;
    /**
     * 激活状态（00：未激活，01：激活）
     */
	@TableField("active_status")
	private String activeStatus;
    /**
     * 方法入参类型，多个用，分割
     */
	@TableField("field_params_java_type")
	private String fieldParamsJavaType;
    /**
     *  字段坐标，split不为空有效。
     */
	@TableField("field_position")
	private Integer fieldPosition;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getProtocolId() {
		return protocolId;
	}

	public void setProtocolId(Integer protocolId) {
		this.protocolId = protocolId;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldJavaCode() {
		return fieldJavaCode;
	}

	public void setFieldJavaCode(String fieldJavaCode) {
		this.fieldJavaCode = fieldJavaCode;
	}

	public String getFieldJavaType() {
		return fieldJavaType;
	}

	public void setFieldJavaType(String fieldJavaType) {
		this.fieldJavaType = fieldJavaType;
	}

	public String getAnalysisJavaMethod() {
		return analysisJavaMethod;
	}

	public void setAnalysisJavaMethod(String analysisJavaMethod) {
		this.analysisJavaMethod = analysisJavaMethod;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public Integer getFieldStartPosition() {
		return fieldStartPosition;
	}

	public void setFieldStartPosition(Integer fieldStartPosition) {
		this.fieldStartPosition = fieldStartPosition;
	}

	public Integer getFieldEndPosition() {
		return fieldEndPosition;
	}

	public void setFieldEndPosition(Integer fieldEndPosition) {
		this.fieldEndPosition = fieldEndPosition;
	}

	public Integer getFieldSize() {
		return fieldSize;
	}

	public void setFieldSize(Integer fieldSize) {
		this.fieldSize = fieldSize;
	}

	public String getFieldType() {
		return fieldType;
	}

	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
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

	public String getActiveStatus() {
		return activeStatus;
	}

	public void setActiveStatus(String activeStatus) {
		this.activeStatus = activeStatus;
	}

	public String getFieldParamsJavaType() {
		return fieldParamsJavaType;
	}

	public void setFieldParamsJavaType(String fieldParamsJavaType) {
		this.fieldParamsJavaType = fieldParamsJavaType;
	}

	public Integer getFieldPosition() {
		return fieldPosition;
	}

	public void setFieldPosition(Integer fieldPosition) {
		this.fieldPosition = fieldPosition;
	}
	@Override
	public String toString() {
		return "ProtocolAnalysis{" +
			", id=" + id +
			", protocolId=" + protocolId +
			", fieldName=" + fieldName +
			", fieldJavaCode=" + fieldJavaCode +
			", fieldJavaType=" + fieldJavaType +
			", analysisJavaMethod=" + analysisJavaMethod +
			", orderType=" + orderType +
			", fieldStartPosition=" + fieldStartPosition +
			", fieldEndPosition=" + fieldEndPosition +
			", fieldSize=" + fieldSize +
			", fieldType=" + fieldType +
			", createDate=" + createDate +
			", createBy=" + createBy +
			", updateDate=" + updateDate +
			", updateBy=" + updateBy +
			", activeStatus=" + activeStatus +
			", fieldParamsJavaType=" + fieldParamsJavaType +
			", fieldPosition=" + fieldPosition +
			"}";
	}
}
