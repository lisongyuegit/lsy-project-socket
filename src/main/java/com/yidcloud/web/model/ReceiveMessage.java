package com.yidcloud.web.model;

/**
 * 接受消息实体
 *
 * @author 胡洪瑜 huhongyu@edenep.net
 * @version 2.0
 * @company 易登科技
 * @since 2017/11/10 13:46
 */
public class ReceiveMessage {

    private String headTag;

    private int port;

    private String mid;

    private String imei;

    private String msgByte;

    private String splitStr;

    private String endTag;

    private String callBackClazz;

    private String isForward;//是否转发

    private String forwardUrl;//转发地址
    
    private int selfCheckingProtocol;//自检协议 0否 1心跳自检 2合法数据自检

    /**
     * 获取到数据的时间
     */
    private String receiveDateStr;

    public void setReceiveDateStr(String receiveDateStr) {
        this.receiveDateStr = receiveDateStr;
    }

    public String getReceiveDateStr() {
        return receiveDateStr;
    }

    public void setCallBackClazz(String callBackClazz) {
        this.callBackClazz = callBackClazz;
    }

    public String getCallBackClazz() {
        return callBackClazz;
    }

    public void setEndTag(String endTag) {
        this.endTag = endTag;
    }

    public String getEndTag() {
        return endTag;
    }

    public void setSplitStr(String splitStr) {
        this.splitStr = splitStr;
    }

    public String getSplitStr() {
        return splitStr;
    }

    public String getHeadTag() {
        return headTag;
    }

    public void setHeadTag(String headTag) {
        this.headTag = headTag;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public void setMsgByte(String msgByte) {
        this.msgByte = msgByte;
    }

    public String getMsgByte() {
        return msgByte;
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

    public int getSelfCheckingProtocol() {
        return selfCheckingProtocol;
    }
    
    public void setSelfCheckingProtocol(int selfCheckingProtocol) {
        this.selfCheckingProtocol = selfCheckingProtocol;
    }

    @Override
    public String toString() {
        return "ReceiveMessage{" + "headTag='" + headTag + '\'' + ", port=" + port + ", mid='" + mid + '\'' + ", imei='" + imei + '\'' + ", isForward='" + isForward + '\'' + ", forwardUrl='" + forwardUrl + '\'' + ", selfCheckingProtocol='" + selfCheckingProtocol + '\'' + ", msgByte="
                + msgByte + '}';
    }
}
