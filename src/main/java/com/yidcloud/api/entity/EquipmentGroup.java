package com.yidcloud.api.entity;


import com.lsy.mybatisplus.annotations.TableField;
import com.lsy.mybatisplus.annotations.TableId;
import com.lsy.mybatisplus.annotations.TableName;
import com.lsy.mybatisplus.enums.IdType;

import java.io.Serializable;

/**
 * @description:  设备组 (用一句话描述该类做什么)
 * @copyright: Copyright (c) 2017
 * @company: 易登科技
 * @author 胡洪瑜
 * @version 2.0
 * @since 2017/11/14 09:51:47
 */
@TableName("t_hwy_cot_equipment_group")
public class EquipmentGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
	@TableId(value="id", type= IdType.AUTO)
	private Integer id;
    /**
     * 归属项目id
     */
	@TableField("project_id")
	private Integer projectId;
    /**
     * 组名称
     */
	@TableField("group_name")
	private String groupName;
    /**
     * 授权地址
     */
	@TableField("auth_address")
	private String authAddress;
    /**
     * 授权端口
     */
	@TableField("auth_port")
	private Integer authPort;
    /**
     * 发送地址
     */
	@TableField("send_address")
	private String sendAddress;
    /**
     * 发送端口
     */
	@TableField("send_port")
	private Integer sendPort;
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

	public Integer getProjectId() {
		return projectId;
	}

	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}

	public String getAuthAddress() {
		return authAddress;
	}

	public void setAuthAddress(String authAddress) {
		this.authAddress = authAddress;
	}

	public Integer getAuthPort() {
		return authPort;
	}

	public void setAuthPort(Integer authPort) {
		this.authPort = authPort;
	}

	public String getSendAddress() {
		return sendAddress;
	}

	public void setSendAddress(String sendAddress) {
		this.sendAddress = sendAddress;
	}

	public Integer getSendPort() {
		return sendPort;
	}

	public void setSendPort(Integer sendPort) {
		this.sendPort = sendPort;
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

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	@Override
	public String toString() {
		return "EquipmentGroup{" +
			", id=" + id +
			", projectId=" + projectId +
			", groupName=" + groupName +
			", authAddress=" + authAddress +
			", authPort=" + authPort +
			", sendAddress=" + sendAddress +
			", sendPort=" + sendPort +
			", createDate=" + createDate +
			", createBy=" + createBy +
			", updateDate=" + updateDate +
			", updateBy=" + updateBy +
			"}";
	}
}
