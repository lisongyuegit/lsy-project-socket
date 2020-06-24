package com.yidcloud.api.entity;


import com.lsy.mybatisplus.annotations.TableField;
import com.lsy.mybatisplus.annotations.TableId;
import com.lsy.mybatisplus.annotations.TableName;
import com.lsy.mybatisplus.enums.IdType;

import java.io.Serializable;

/**
 * 油箱类型
 * @description:  油箱类型 (用一句话描述该类做什么)
 * @copyright: Copyright (c) 2018
 * @company: 易登科技
 * @author huhongyu@edenep.net
 * @version 2.0
 * @since 2018/06/06 20:52:06
 */
@TableName("t_hwy_cot_oil_type")
public class OilType implements Serializable {

    private static final long serialVersionUID = 1L;

	@TableId(value="id", type= IdType.AUTO)
	private Integer id;
    /**
     * 邮箱名称
     */
	@TableField("oil_name")
	private String oilName;
    /**
     * 油箱的长（米）
     */
	@TableField("oil_length")
	private Double oilLength;
    /**
     * 油箱的宽（米）
     */
	@TableField("oil_width")
	private Double oilWidth;
    /**
     * 油箱的高（米）
     */
	@TableField("oil_height")
	private Double oilHeight;
    /**
     * 油箱的体积（立方米）
     */
	@TableField("oil_volume")
	private Double oilVolume;
	@TableField("create_date")
	private String createDate;
	@TableField("create_by")
	private String createBy;
	@TableField("update_date")
	private String updateDate;
	@TableField("update_by")
	private String updateBy;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getOilName() {
		return oilName;
	}

	public void setOilName(String oilName) {
		this.oilName = oilName;
	}

	public Double getOilLength() {
		return oilLength;
	}

	public void setOilLength(Double oilLength) {
		this.oilLength = oilLength;
	}

	public Double getOilWidth() {
		return oilWidth;
	}

	public void setOilWidth(Double oilWidth) {
		this.oilWidth = oilWidth;
	}

	public Double getOilHeight() {
		return oilHeight;
	}

	public void setOilHeight(Double oilHeight) {
		this.oilHeight = oilHeight;
	}

	public Double getOilVolume() {
		return oilVolume;
	}

	public void setOilVolume(Double oilVolume) {
		this.oilVolume = oilVolume;
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

	@Override
	public String toString() {
		return "OilType{" +
			", id=" + id +
			", oilName=" + oilName +
			", oilLength=" + oilLength +
			", oilWidth=" + oilWidth +
			", oilHeight=" + oilHeight +
			", oilVolume=" + oilVolume +
			", createDate=" + createDate +
			", createBy=" + createBy +
			", updateDate=" + updateDate +
			", updateBy=" + updateBy +
			"}";
	}
}
