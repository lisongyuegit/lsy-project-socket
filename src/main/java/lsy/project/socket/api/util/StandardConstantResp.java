package lsy.project.socket.api.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 定义一些标准的打印信息
 *
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public enum StandardConstantResp {

    /************************************************
     * /* 标准常量Resp代码定义
     ************************************************/
    COLLECT_OK(0),
    EDENEP_BOX_UPGRADE_FILE_PACKAGE_SIZE(64),
    EDENEP_BOX_UPGRADE_FILE_SERCERT(10),
    EDENEP_BOX_UPGRADE_FILE_MINSIZE(1040),
    ERROR_PROTOCOLTYPE_NOTEXIST(100),
    ERROR_DATA_OVERLENGTH(101),
    ERROR_INCOMPLETE_PACKAGE(102),
    ERROR_PUSH_UPGRADE_FILE(103),
    ERROR_SERVER_UPGRADE_FILE(104),
    ERROR_NOTFOUND_UPGRADECACHE(105),
    UPGRADE_PACKAGE_DOWN_COMPLETE(106),
    UPGRADE_CURRENTPACKAGE_DOWN_COMPLETE(107),
    ERROR_UPGRADE_CURRENTPACKAGE_DOWN(108),
    ERROR_IO_EXCEPTION(109),
    DATA_TEST(100),
    ;

    /************************************************
     * /* 标准Response消息定义
     ************************************************/
    private static Map<StandardConstantResp, String> messageMap;

    static {
        messageMap = new HashMap<StandardConstantResp, String>();
        messageMap.put(StandardConstantResp.COLLECT_OK, "客户端[%s],协议头[%s],hex数据[%s]");
        messageMap.put(StandardConstantResp.EDENEP_BOX_UPGRADE_FILE_PACKAGE_SIZE, "盒子升级包大小");
        messageMap.put(StandardConstantResp.EDENEP_BOX_UPGRADE_FILE_SERCERT, "YDKJ");
        messageMap.put(StandardConstantResp.EDENEP_BOX_UPGRADE_FILE_MINSIZE, "易登盒子升级文件最少字节数,不能少于[%s]");
        messageMap.put(StandardConstantResp.ERROR_PROTOCOLTYPE_NOTEXIST, "系统中找不到对应的协议类型");
        messageMap.put(StandardConstantResp.ERROR_DATA_OVERLENGTH, "接收到的协议数据超长");
        messageMap.put(StandardConstantResp.ERROR_INCOMPLETE_PACKAGE, "客户端[%s],协议头[%s],当前接收到的包数据不完整,等待下一包数据到来,hex数据[%s]");
        messageMap.put(StandardConstantResp.ERROR_PUSH_UPGRADE_FILE, "推送[%s]升级文件,找不到服务器路径[%s]对应文件");
        messageMap.put(StandardConstantResp.ERROR_SERVER_UPGRADE_FILE, "设备[%s]升级,服务器路径[%s]文件异常,已被篡改,找不到约定的密钥,服务器暂停下载");
        messageMap.put(StandardConstantResp.ERROR_NOTFOUND_UPGRADECACHE, "IMEI[%s]升级缓存信息为空，无需进行升级下发升级包");
        messageMap.put(StandardConstantResp.UPGRADE_PACKAGE_DOWN_COMPLETE, "设备[%s],所有的升级包下载完成,正在下发最后一个指令包[%s]");
        messageMap.put(StandardConstantResp.UPGRADE_CURRENTPACKAGE_DOWN_COMPLETE, "设备[%s]正在升级,总共[%s]包,已完成下载[%s]包");
        messageMap.put(StandardConstantResp.ERROR_UPGRADE_CURRENTPACKAGE_DOWN, "设备[%s]正在升级,总共[%s]包,第[%s]个包下载失败即将重新下载");
        messageMap.put(StandardConstantResp.DATA_TEST, "原始缓冲区数据:");
        messageMap.put(StandardConstantResp.ERROR_IO_EXCEPTION, "IO异常,异常信息[%s]");
    }

    /************************************************
     * /* 定制的Enum定义
     ************************************************/
    private final int value;

    public int getValue() {
        return value;
    }

    private StandardConstantResp(int value) {
        this.value = value;
    }

    /**
     * 获得本对象对应的错误描述
     *
     * @return 描述文本
     */
    public String getMessage() {
        return messageMap.get(this);
    }

    /**
     * 生成标准的Response Json文本
     *
     * @param seq        消息序列号
     * @param addMessage 附加的消息，不需要此参数则为null
     * @return JSON文本
     */
    public String toJson(String seq, String addMessage) {
        return String.format("{\"code\":%1d,\"msg\":\"%2s\",\"seq\":\"%3s\"}", this.value, // 代码
                messageMap.get(this) // 标准应答消息
                        + (addMessage == null ? "" : ": " + addMessage),// 附加消息
                (seq == null || seq.isEmpty() ? "" : seq) // 序列号
        );
    }
}
