package com.yidcloud.api.contants;

/**
 * 采集系统的常量配置
 *
 * @author 胡洪瑜 huhongyu@edenep.net
 * @version 2.0
 * @company 易登科技
 * @since 2017/11/8 19:20
 */
public class CollectContants {

    /**
     * 采集的原始数据，储存到redis中的集合名称
     *
     */
    public static final String  COLLECT_INFO_COLLECTION_NAME="com.yidcloud.web.collection.name";
    /**
     * 协议类型，在redis中key的前缀
     */
    public static final String  PROTOCOL_TYPE_REDIS_PREFIX="com.yidcloud.web.protocol_type.";

    /**
     * 协议，在reids中，key的前缀
     */
    public static final String PROTOCOL_REDIS_PREFIX="com.yidcloud.web.protocol.";

    /**
     * 设备，在redis中，key的前缀
     */
    public static final String EQUIPMENT_REDIS_PREFIX="com.yidcloud.collent.equipment";
    
    /**
     * 智能垃圾桶上传数据的唯一ID
     */
    public static final String TRASH_UPLOAD_SEQ="com.plat.web.trash:seq:%s";
    
    /**
     * 智能垃圾桶报警缓存
     */
    public static final String TRASH_ALERM="com.plat.web.trash:alerm:%s";

    /**
     * 协议解析数据的字段名称
     */
    public static final String PROTOCOL_ANALYSIS_FIELD_NAME = "analysis";

    /**
     * 协议回掉的类名称
     */
    public static final String PROTOCOL_CALL_BACK_CLAZZ_FIELD_NAME = "callBackClazz";
    
    /**
     * 是否转发名称
     */
    public static final String PROTOCOL_IS_FORWARD_NAME = "is_forward";
    
    /**
     * 转发地址
     */
    public static final String PROTOCOL_FORWARD_URL_NAME = "forward_url";
    
    /**
     * 自检协议 0否 1心跳自检 2合法数据自检
     */
    public static final String PROTOCOL_SELF_CHECKING_PROTOCOL_NAME = "self_checking_protocol";

    /**
     * 协议激活状态，00 未激活，01 激活
     */
    public static final String PROTOCOL_ACTIVE_STATUS_00="00";
    /**
     * 协议激活状态，00 未激活，01 激活
     */
    public static final String PROTOCOL_ACTIVE_STATUS_01="01";

    /**
     * 协议类型激活状态，00 未激活，01 激活
     */
    public static final String PROTOCOL_TYPE_ACTIVE_STATUS_00="00";
    /**
     * 协议类型激活状态，00 未激活，01 激活
     */
    public static final String PROTOCOL_TYPE_ACTIVE_STATUS_01="01";

    /**
     * 协议解码规则激活状态，00 未激活，01 激活
     */
    public static final String PROTOCOL_ANALYSIS_ACTIVE_STATUS_00="00";
    /**
     * 协议解码规则激活状态，00 未激活，01 激活
     */
    public static final String PROTOCOL_ANALYSIS_ACTIVE_STATUS_01="01";

    /**
     * 协议解码方式 00 智能解码，01 自定义解码
     */
    public static final String PROTOCOL_ANALYSIS_TYPE_00="00";
    /**
     * 协议解码方式 00 智能解码，01 自定义解码
     */
    public static final String PROTOCOL_ANALYSIS_TYPE_01="01";
    /**
     * 数据入es队列
     */
    public static final String COLLECT_TO_ES = "COLLECT_TO_ES";
    
    /**
     * 采集数据推送开放平台
     * */
    public static final String COLLECT_TO_DEVELOPER = "COLLECT_TO_DEVELOPER";

    /**
     * 合法消息入队列指令业务类型
     */
    public static final String COLLECT_AUTH_MSG_QUEUE_COMMAND = "COLLECT_AUTH_MSG_QUEUE_COMMAND";

    /**
     * 解析后的消息入队列指令业务类型
     */
    public static final String COLLECT_ANALYSIS_MSG_QUEUE_COMMAND = "COLLECT_ANALYSIS_MSG_QUEUE_COMMAND";
    
    /**
     * 转发消息入队列指令业务类型
     */
    public static final String COLLECT_FORWARD_MSG_QUEUE_COMMAND = "COLLECT_FORWARD_MSG_QUEUE_COMMAND";
    
    /**
     * 转发消息入队列指令业务类型
     */
    public static final String COLLECT_SEND_TERMINAL_MSG_QUEUE_COMMAND = "SEND_TERMINAL_MSG_QUEUE_COMMAND";

    /**
     * 解码顺序（0：正序，1：倒序）
     */
    public static final String PROTOCOL_ANALYSIS_ORDER_TYPE_0="0";

    /**
     * 解码顺序（0：正序，1：倒序）
     */
    public static final String PROTOCOL_ANALYSIS_ORDER_TYPE_1="1";
    
    /**
     * IMEI->卡片机今明两天排班，在reids中，key的前缀
     */
    public static final String IMEI_ATTENDANCE_SCHEDULE="com.yidcloud.merchant.project.imei_attendance_schedule:imei:";
    
    /**
     * 卡片机排班key 保卫员field
     */
    public static final String ATTENDANCESCHEDULESHIFT="attendanceScheduleList";

    /**
     * 卡片机排班key 网格管理员field
     */
    public static final String GRIDMANAGERSHIFT="girdManagerList";
    
    /**
     * 卡片机登录，未请求到最近的两个排班班次 则返回约定的默认值
     */
    public static final String DEFAULT_SCHEDULETIME_HEX_VALUE="01FFFFFFFFFFFFFFFF02FFFFFFFFFFFFFFFF";
    
    /**
     * 卡片机登录，约定的返回最近的两个班次  但缓存中只查询到了一个班次 第二个班次则返回如下约定值
     */
    public static final String DEFAULT_SECOND_SCHEDULETIME_HEX_VALUE="02FFFFFFFFFFFFFFFF";
    
    /**
     * IMEI 缓存指令信息
     */
    public static final String IMEI_CACHE="com.yidcloud.merchant.project.imei2publish:imei:";
    
    /**
     * IMEI取员工，给app单点登录
     */
    public static final String IMEI2EMPLOYEE="com.yidcloud.merchant.project.imei2employee:imei:%s";

    public static final String OIL_TYPE_REDISKEY = "com.yidcloud.web.oil_type";
    
    /**
     * IMEI ，升级任务缓存信息
     */
    public static final String IMEI_TASK_CACHE = "com.yidcloud.merchant.project.imei2publishtask:imei:";
    
    /**
     * IMEI(服务器推送升级缓存信息)
     */
    public static final String IMEI_UPGRADE_CACHE = "com.yidcloud.merchant.project.imei2upgrade:imei:";
    
    /**
     * IMEI 客户端通道
     */
    public static final String IMEI_CHANNEL_CACHE = "com.yidcloud.merchant.project.imei2channel:imei:";
    
    public static final String IMEI = "imei";//设备号 java字段
    
    public static final String CMD = "cmd";//命定码 java字段
    
    public static final String ISSUCCRSS = "isSuccess";//成功标志 java字段
    
    public static final String HEADTAG = "headTag";//协议头 java字段

    public static final String ENDTAG = "endTag";//协议尾java字段
    
    public static final String MID = "mid";//协议ID java字段 

    /**
     * 无屏售货机设备IMEI分配MID，将关系绑定到缓存
     */
    public static final String IMEI_VENDINGMACHINE_CACHE = "com.yidcloud.merchant.project.imei2vendingmachine.imei:%s";
    public static final String IMEI_VENDINGMACHINE_MID_CACHE = "com.yidcloud.merchant.project.imei2vendingmachine.mid:%s";
    public static final String IMEI_VENDINGMACHINE_WAY_CACHE = "com.yidcloud.merchant.project.imei2vendingmachine.way.imei:%s";
    public static final String IMEI_VENDINGMACHINE_ORDER_CACHE = "com.yidcloud.merchant.project.imei2vendingmachine.order.imei:%s:order:%s";
    public static final String IMEI_VENDINGMACHINE_HEART_CACHE = "com.yidcloud.merchant.project.imei2vendingmachine.heart.imei:%s:cmd:%s";

}
