package com.yidcloud.api.dto;

/**
 * 
 * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
 * @copyright: Copyright (c) 2018
 * @company: 易登科技
 * @author: zhouliang@edenep.net
 * @version: 2.0
 * @date: 2018年7月4日 上午11:55:22
 */
public class ImeiChannelDto {
    
    private String imei;//设备唯一码
    
    private String onlineTime;//上线时间
    
    private String remote;//客户端地址
    
    private String heartTime;//最近心跳时间
    
    private String reportTime;//最近上传数据时间
    
    private String lastOfflineTime;//最近一次掉线时间
    
    private String isOnline;//是否在线 0 否 1是
    
    public String getImei() {
        return imei;
    }
    
    public void setImei(String imei) {
        this.imei = imei;
    }
    
    public String getOnlineTime() {
        return onlineTime;
    }

    public void setOnlineTime(String onlineTime) {
        this.onlineTime = onlineTime;
    }
    
    public String getRemote() {
        return remote;
    }
    
    public void setRemote(String remote) {
        this.remote = remote;
    }
    
    public String getHeartTime() {
        return heartTime;
    }
    
    public void setHeartTime(String heartTime) {
        this.heartTime = heartTime;
    }
    
    public String getReportTime() {
        return reportTime;
    }
    
    public void setReportTime(String reportTime) {
        this.reportTime = reportTime;
    }
    
    public String getLastOfflineTime() {
        return lastOfflineTime;
    }
    
    public void setLastOfflineTime(String lastOfflineTime) {
        this.lastOfflineTime = lastOfflineTime;
    }

    public String getIsOnline() {
        return isOnline;
    }
    
    public void setIsOnline(String isOnline) {
        this.isOnline = isOnline;
    }
}
