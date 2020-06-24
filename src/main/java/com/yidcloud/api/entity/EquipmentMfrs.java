package com.yidcloud.api.entity;

import com.lsy.mybatisplus.annotations.TableField;
import com.lsy.mybatisplus.annotations.TableId;
import com.lsy.mybatisplus.annotations.TableName;
import com.lsy.mybatisplus.enums.IdType;

import java.io.Serializable;

/**
 * @description:  设备厂商 (用一句话描述该类做什么)
 * @copyright: Copyright (c) 2017
 * @company: 易登科技
 * @author 胡洪瑜 huhongyu@edenep.net
 * @version 2.0
 * @since 2017/11/28 15:50:13
 */
@TableName("t_hwy_cot_equipment_mfrs")
public class EquipmentMfrs implements Serializable {

    private static final long serialVersionUID = 1L;

	@TableId(value="id", type= IdType.AUTO)
	private Integer id;
    /**
     * 厂商名称
     */
	@TableField("mfrs_name")
	private String mfrsName;
    /**
     * 英文名
     */
	@TableField("mfrs_ename")
	private String mfrsEname;
    /**
     * 厂商地址
     */
	@TableField("mfrs_address")
	private String mfrsAddress;
    /**
     * 联系人
     */
	@TableField("mfrs_contact_person")
	private String mfrsContactPerson;
    /**
     * 联系电话
     */
	@TableField("mfrs_contact_tel")
	private String mfrsContactTel;
    /**
     * 联系邮箱
     */
	@TableField("mfrs_contact_email")
	private String mfrsContactEmail;
    /**
     * 厂商网站
     */
	@TableField("mfrs_site")
	private String mfrsSite;
    /**
     * 厂商主要业务及产品
     */
	@TableField("mfrs_main_products")
	private String mfrsMainProducts;
    /**
     * 厂商描述
     */
	@TableField("mfrs_desc")
	private String mfrsDesc;
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

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getMfrsName() {
		return mfrsName;
	}

	public void setMfrsName(String mfrsName) {
		this.mfrsName = mfrsName;
	}

	public String getMfrsEname() {
		return mfrsEname;
	}

	public void setMfrsEname(String mfrsEname) {
		this.mfrsEname = mfrsEname;
	}

	public String getMfrsAddress() {
		return mfrsAddress;
	}

	public void setMfrsAddress(String mfrsAddress) {
		this.mfrsAddress = mfrsAddress;
	}

	public String getMfrsContactPerson() {
		return mfrsContactPerson;
	}

	public void setMfrsContactPerson(String mfrsContactPerson) {
		this.mfrsContactPerson = mfrsContactPerson;
	}

	public String getMfrsContactTel() {
		return mfrsContactTel;
	}

	public void setMfrsContactTel(String mfrsContactTel) {
		this.mfrsContactTel = mfrsContactTel;
	}

	public String getMfrsContactEmail() {
		return mfrsContactEmail;
	}

	public void setMfrsContactEmail(String mfrsContactEmail) {
		this.mfrsContactEmail = mfrsContactEmail;
	}

	public String getMfrsSite() {
		return mfrsSite;
	}

	public void setMfrsSite(String mfrsSite) {
		this.mfrsSite = mfrsSite;
	}

	public String getMfrsMainProducts() {
		return mfrsMainProducts;
	}

	public void setMfrsMainProducts(String mfrsMainProducts) {
		this.mfrsMainProducts = mfrsMainProducts;
	}

	public String getMfrsDesc() {
		return mfrsDesc;
	}

	public void setMfrsDesc(String mfrsDesc) {
		this.mfrsDesc = mfrsDesc;
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
		return "EquipmentMfrs{" +
			", id=" + id +
			", mfrsName=" + mfrsName +
			", mfrsEname=" + mfrsEname +
			", mfrsAddress=" + mfrsAddress +
			", mfrsContactPerson=" + mfrsContactPerson +
			", mfrsContactTel=" + mfrsContactTel +
			", mfrsContactEmail=" + mfrsContactEmail +
			", mfrsSite=" + mfrsSite +
			", mfrsMainProducts=" + mfrsMainProducts +
			", mfrsDesc=" + mfrsDesc +
			", createDate=" + createDate +
			", createBy=" + createBy +
			", updateDate=" + updateDate +
			", updateBy=" + updateBy +
			"}";
	}
}
