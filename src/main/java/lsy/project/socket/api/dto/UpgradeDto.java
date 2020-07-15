package lsy.project.socket.api.dto;


/**
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public class UpgradeDto {

    private String imei;//设备唯一码

    private String filePath;//文件存放地址

    private String fileByteSize;//文件总字节数

    private String fileByteSum;//文件总字节数求和

    private String packageTotal;//包总数--分包

    private String currentDown;//已下载包数

    private String currentPackageDownTime;//当前包下发时间

    private String downStartTime;//升级开始时间

    private String downEndTime;//升级完成时间

    private String gversion;//当前版本

    private String status = "0";//下载状态 0未开始 1正在下载 2已完成

    private String taskid;//升级任务ID

    /**
     * 用于组装数据协议下发
     **/
    private String sendStartTag;//起始帧头

    private String sendEndTag;//帧尾

    private String sendCmd;//指令码

    private String imeiType;//Imei 0 智能盒子,1 垃圾桶

    private String sendLastPackageCmd;//最后一包指令码

    public String getImeiType() {
        return imeiType;
    }

    public void setImeiType(String imeiType) {
        this.imeiType = imeiType;
    }

    public String getSendLastPackageCmd() {
        return sendLastPackageCmd;
    }

    public void setSendLastPackageCmd(String sendLastPackageCmd) {
        this.sendLastPackageCmd = sendLastPackageCmd;
    }

    public String getTaskid() {
        return taskid;
    }

    public void setTaskid(String taskid) {
        this.taskid = taskid;
    }

    public String getCurrentPackageDownTime() {
        return currentPackageDownTime;
    }

    public void setCurrentPackageDownTime(String currentPackageDownTime) {
        this.currentPackageDownTime = currentPackageDownTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getPackageTotal() {
        return packageTotal;
    }

    public void setPackageTotal(String packageTotal) {
        this.packageTotal = packageTotal;
    }

    public String getCurrentDown() {
        return currentDown;
    }

    public void setCurrentDown(String currentDown) {
        this.currentDown = currentDown;
    }

    public String getGversion() {
        return gversion;
    }

    public void setGversion(String gversion) {
        this.gversion = gversion;
    }

    public String getFileByteSize() {
        return fileByteSize;
    }

    public void setFileByteSize(String fileByteSize) {
        this.fileByteSize = fileByteSize;
    }

    public String getFileByteSum() {
        return fileByteSum;
    }

    public void setFileByteSum(String fileByteSum) {
        this.fileByteSum = fileByteSum;
    }

    public String getDownStartTime() {
        return downStartTime;
    }

    public void setDownStartTime(String downStartTime) {
        this.downStartTime = downStartTime;
    }

    public String getDownEndTime() {
        return downEndTime;
    }

    public void setDownEndTime(String downEndTime) {
        this.downEndTime = downEndTime;
    }

    public String getSendStartTag() {
        return sendStartTag;
    }

    public void setSendStartTag(String sendStartTag) {
        this.sendStartTag = sendStartTag;
    }

    public String getSendEndTag() {
        return sendEndTag;
    }

    public void setSendEndTag(String sendEndTag) {
        this.sendEndTag = sendEndTag;
    }

    public String getSendCmd() {
        return sendCmd;
    }

    public void setSendCmd(String sendCmd) {
        this.sendCmd = sendCmd;
    }

}
