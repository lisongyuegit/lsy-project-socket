package com.yidcloud.api.entity;


import com.lsy.mybatisplus.annotations.TableField;
import com.lsy.mybatisplus.annotations.TableId;
import com.lsy.mybatisplus.annotations.TableName;
import com.lsy.mybatisplus.enums.IdType;

import java.io.Serializable;

/**
 * 油箱类型标的
 * @description:  油箱类型标的 (用一句话描述该类做什么)
 * @copyright: Copyright (c) 2018
 * @company: 易登科技
 * @author huhongyu@edenep.net
 * @version 2.0
 * @since 2018/06/06 20:16:51
 */
@TableName("t_hwy_cot_oil_type_standard")
public class OilTypeStandard implements Serializable {

    private static final long serialVersionUID = 1L;

	@TableId(value="id", type= IdType.AUTO)
	private Integer id;
	@TableField("type_id")
	private Integer typeId;
    /**
     * 剩余油量百分比
     */
	private Double percentage;
    /**
     * 加油量（升）
     */
	private Double oil;
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

	public Integer getTypeId() {
		return typeId;
	}

	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}

	public Double getPercentage() {
		return percentage;
	}

	public void setPercentage(Double percentage) {
		this.percentage = percentage;
	}

	public Double getOil() {
		return oil;
	}

	public void setOil(Double oil) {
		this.oil = oil;
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
		return "OilTypeStandard{" +
			", id=" + id +
			", typeId=" + typeId +
			", percentage=" + percentage +
			", oil=" + oil +
			", createDate=" + createDate +
			", createBy=" + createBy +
			", updateDate=" + updateDate +
			", updateBy=" + updateBy +
			"}";
	}
}
