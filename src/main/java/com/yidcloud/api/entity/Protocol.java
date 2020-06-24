package com.yidcloud.api.entity;


import com.lsy.mybatisplus.annotations.TableField;
import com.lsy.mybatisplus.annotations.TableId;
import com.lsy.mybatisplus.annotations.TableName;
import com.lsy.mybatisplus.enums.IdType;

import java.io.Serializable;

/**
 * @description: 协议表（指令表，相当于一个一个的请求接口信息） (用一句话描述该类做什么)
 * @copyright: Copyright (c) 2017
 * @company: 易登科技
 * @author 胡洪瑜
 * @version 2.0
 * @since 2017/11/14 09:51:47
 */
@TableName("t_hwy_cot_protocol")
public class Protocol implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 协议类型ID
     */
    @TableField("type_id")
    private Integer typeId;

    /**
     * 协议名称
     */
    @TableField("protocol_name")
    private String protocolName;

    /**
     * MID
     */
    private String mid;

    /**
     * 解析方式00：智能解码，01：自定义解码
     */
    @TableField("analysis_type")
    private String analysisType;

    /**
     * 解码类
     */
    @TableField("analysis_clazz")
    private String analysisClazz;

    /**
     * 协议版本
     */
    private String version;

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
     * 回写类
     */
    @TableField("call_back_clazz")
    private String callBackClazz;

    /**
     * 消息中验证码位置（不为空时 需要进行消息验证码的验证）当为负数时则表示倒数第几个
     */
    @TableField("verifycode_position")
    private Integer verifycodePosition;

    /**
     * 消息 验证起始字节
     */
    @TableField("verify_start_position")
    private Integer verifyStartPosition;

    /**
     * 消息 验证结束字节
     */
    @TableField("verify_end_position")
    private Integer verifyEndPosition;

    /**
     * 验证码 java转义方法
     */
    @TableField("verify_java_method")
    private String verifyJavaMethod;

    /**
     * 是否转发
     */
    @TableField("is_forward")
    private String isForward;

    /**
     * 转发地址
     */
    @TableField("forward_url")
    private String forwardUrl;
    
    /**
     * 自检协议 0否 1心跳自检 2合法数据自检
     */
    @TableField("self_checking_protocol")
    private Integer selfCheckingProtocol;

    public void setCallBackClazz(String callBackClazz) {
        this.callBackClazz = callBackClazz;
    }

    public String getCallBackClazz() {
        return callBackClazz;
    }

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

    public String getProtocolName() {
        return protocolName;
    }

    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(String analysisType) {
        this.analysisType = analysisType;
    }

    public String getAnalysisClazz() {
        return analysisClazz;
    }

    public void setAnalysisClazz(String analysisClazz) {
        this.analysisClazz = analysisClazz;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public Integer getVerifycodePosition() {
        return verifycodePosition;
    }

    public void setVerifycodePosition(Integer verifycodePosition) {
        this.verifycodePosition = verifycodePosition;
    }

    public Integer getVerifyStartPosition() {
        return verifyStartPosition;
    }

    public void setVerifyStartPosition(Integer verifyStartPosition) {
        this.verifyStartPosition = verifyStartPosition;
    }

    public Integer getVerifyEndPosition() {
        return verifyEndPosition;
    }

    public void setVerifyEndPosition(Integer verifyEndPosition) {
        this.verifyEndPosition = verifyEndPosition;
    }

    public String getVerifyJavaMethod() {
        return verifyJavaMethod;
    }

    public void setVerifyJavaMethod(String verifyJavaMethod) {
        this.verifyJavaMethod = verifyJavaMethod;
    }

    public String getIsForward() {
        return isForward;
    }

    public void setIsForward(String isForward) {
        this.isForward = isForward;
    }

    public String getForwardUrl() {
        return forwardUrl;
    }

    public void setForwardUrl(String forwardUrl) {
        this.forwardUrl = forwardUrl;
    }
    
    public Integer getSelfCheckingProtocol() {
        return selfCheckingProtocol;
    }
    
    public void setSelfCheckingProtocol(Integer selfCheckingProtocol) {
        this.selfCheckingProtocol = selfCheckingProtocol;
    }
    @Override
    public String toString() {
        return "Protocol{" + ", id=" + id + ", typeId=" + typeId + ", protocolName=" + protocolName + ", mid=" + mid + ", analysisType="
                + analysisType + ", analysisClazz=" + analysisClazz + ", version=" + version + ", createDate=" + createDate + ", createBy=" + createBy
                + ", updateDate=" + updateDate + ", updateBy=" + updateBy + ", activeStatus=" + activeStatus + ", verifycodePosition="
                + verifycodePosition + ", verifyStartPosition=" + verifyStartPosition + ", verifyEndPosition=" + verifyEndPosition
                + ", verifyJavaMethod=" + verifyJavaMethod + "}";
    }
}
