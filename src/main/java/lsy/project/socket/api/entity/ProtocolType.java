package lsy.project.socket.api.entity;


import com.lsy.mybatisplus.annotations.TableField;
import com.lsy.mybatisplus.annotations.TableId;
import com.lsy.mybatisplus.annotations.TableName;
import com.lsy.mybatisplus.enums.IdType;

import java.io.Serializable;

/**
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
@TableName("t_hwy_cot_protocol_type")
public class ProtocolType implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 类型名称
     */
    @TableField("type_name")
    private String typeName;
    /**
     * 接受消息头端口
     */
    private Integer port;
    /**
     * 消息头
     */
    @TableField("start_tag")
    private String startTag;
    /**
     * 消息尾
     */
    @TableField("end_tag")
    private String endTag;
    /**
     * MID开始位置,split为空有效。
     */
    @TableField("mid_start_position")
    private Integer midStartPosition;
    /**
     * MID结束位置,split为空有效。
     */
    @TableField("mid_end_position")
    private Integer midEndPosition;
    /**
     * MID长度,split为空有效。
     */
    @TableField("mid_size")
    private Integer midSize;
    /**
     * IMEI开始位置,split为空有效。
     */
    @TableField("imei_start_position")
    private Integer imeiStartPosition;
    /**
     * IMEI结束位置,split为空有效。
     */
    @TableField("imei_end_position")
    private Integer imeiEndPosition;
    /**
     * IMEI长度,split为空有效。
     */
    @TableField("imei_size")
    private Integer imeiSize;
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
     * 分隔符,null表示不是分隔符处理的数据
     */
    @TableField("split_str")
    private String splitStr;
    /**
     * mid的下标位置 split不为空有效。
     */
    @TableField("mid_position")
    private Integer midPosition;
    /**
     * mid的下标位置 split不为空有效。
     */
    @TableField("imei_position")
    private Integer imeiPosition;

    @TableField("escape")
    private String escape;

    @TableField("imei_java_method")
    private String imeiJavaMethod;
    @TableField("mid_java_method")
    private String midJavaMethod;

    /**
     * 批量上传分隔符
     */
    @TableField("split_str_batch")
    private String splitStrBatch;

    public void setImeiJavaMethod(String imeiJavaMethod) {
        this.imeiJavaMethod = imeiJavaMethod;
    }

    public String getImeiJavaMethod() {
        return imeiJavaMethod;
    }

    public void setMidJavaMethod(String midJavaMethod) {
        this.midJavaMethod = midJavaMethod;
    }

    public String getMidJavaMethod() {
        return midJavaMethod;
    }

    public void setEscape(String escape) {
        this.escape = escape;
    }

    public String getEscape() {
        return escape;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getStartTag() {
        return startTag;
    }

    public void setStartTag(String startTag) {
        this.startTag = startTag;
    }

    public String getEndTag() {
        return endTag;
    }

    public void setEndTag(String endTag) {
        this.endTag = endTag;
    }

    public Integer getMidStartPosition() {
        return midStartPosition;
    }

    public void setMidStartPosition(Integer midStartPosition) {
        this.midStartPosition = midStartPosition;
    }

    public Integer getMidEndPosition() {
        return midEndPosition;
    }

    public void setMidEndPosition(Integer midEndPosition) {
        this.midEndPosition = midEndPosition;
    }

    public Integer getMidSize() {
        return midSize;
    }

    public void setMidSize(Integer midSize) {
        this.midSize = midSize;
    }

    public Integer getImeiStartPosition() {
        return imeiStartPosition;
    }

    public void setImeiStartPosition(Integer imeiStartPosition) {
        this.imeiStartPosition = imeiStartPosition;
    }

    public Integer getImeiEndPosition() {
        return imeiEndPosition;
    }

    public void setImeiEndPosition(Integer imeiEndPosition) {
        this.imeiEndPosition = imeiEndPosition;
    }

    public Integer getImeiSize() {
        return imeiSize;
    }

    public void setImeiSize(Integer imeiSize) {
        this.imeiSize = imeiSize;
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

    public String getSplitStr() {
        return splitStr;
    }

    public void setSplitStr(String splitStr) {
        this.splitStr = splitStr;
    }

    public Integer getMidPosition() {
        return midPosition;
    }

    public void setMidPosition(Integer midPosition) {
        this.midPosition = midPosition;
    }

    public Integer getImeiPosition() {
        return imeiPosition;
    }

    public void setImeiPosition(Integer imeiPosition) {
        this.imeiPosition = imeiPosition;
    }

    public String getSplitStrBatch() {
        return splitStrBatch;
    }

    public void setSplitStrBatch(String splitStrBatch) {
        this.splitStrBatch = splitStrBatch;
    }

    @Override
    public String toString() {
        return "ProtocolType{" +
                ", id=" + id +
                ", typeName=" + typeName +
                ", port=" + port +
                ", startTag=" + startTag +
                ", endTag=" + endTag +
                ", midStartPosition=" + midStartPosition +
                ", midEndPosition=" + midEndPosition +
                ", midSize=" + midSize +
                ", imeiStartPosition=" + imeiStartPosition +
                ", imeiEndPosition=" + imeiEndPosition +
                ", imeiSize=" + imeiSize +
                ", createDate=" + createDate +
                ", createBy=" + createBy +
                ", updateDate=" + updateDate +
                ", updateBy=" + updateBy +
                ", activeStatus=" + activeStatus +
                ", splitStr=" + splitStr +
                ", midPosition=" + midPosition +
                ", imeiPosition=" + imeiPosition +
                ", splitStrBatch=" + splitStrBatch +
                "}";
    }
}
