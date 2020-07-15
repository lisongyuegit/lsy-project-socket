package lsy.project.socket.api.callback;

import com.lsy.base.date.DateHelper;
import lsy.project.socket.api.cache.CollectRedisCacheService;
import lsy.project.socket.api.convert.DefaultProtocolConvert;
import lsy.project.socket.api.dto.UpgradeDto;
import lsy.project.socket.api.model.CallbackMessage;
import lsy.project.socket.api.model.ReceiveMessage;
import lsy.project.socket.api.util.EdenepBoxUpgradeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;


/**
 * 易登盒子心跳协议回写
 *
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public class HeartCallBack extends AbstractCallback {

    private static Logger logger = LoggerFactory.getLogger(HeartCallBack.class);

    private static final long TIMEOUT = 10;//盒子升级包 回复超时时间

    @Override
    CallbackMessage createCallBackMsg(ReceiveMessage msg) throws Exception {
        //判断是否需要回写
        CallbackMessage callbackMessage = new CallbackMessage();

        String replyHeadTag = null;//心跳回复协议头
        String replyEndTag = null;//心跳回复协议尾
        byte[] imeiByte = null;
        byte[] cmdByte = new byte[1];
        if ("FE".equalsIgnoreCase(msg.getHeadTag())) {//新智能盒子
            replyHeadTag = "FD";
            replyEndTag = "DF";
            cmdByte[0] = 0x02;
            imeiByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.stringToASCII(msg.getImei(), msg.getImei().length(), ""));
        } else if ("FC".equalsIgnoreCase(msg.getHeadTag()) //智能垃圾桶
                || "FB".equalsIgnoreCase(msg.getHeadTag()) //智能公厕
                || "FA".equalsIgnoreCase(msg.getHeadTag())) {//GPS定位
            replyHeadTag = msg.getHeadTag().toUpperCase();

            replyEndTag = new StringBuffer(replyHeadTag).reverse().toString();
            cmdByte[0] = 0x02;
            imeiByte = DefaultProtocolConvert.hexstring2bytes(msg.getImei());
        }

        //5=帧头+帧长+指令码+校验+帧尾
        byte[] protocolLength_byte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(imeiByte.length + 5), 2));//数据长度

        byte[] totalDataByte = DefaultProtocolConvert.add(DefaultProtocolConvert
                .add(protocolLength_byte, cmdByte), imeiByte);//从帧长到数据内容

        //协议校验码
        byte[] protocolVerify_byte = new byte[]{(byte) DefaultProtocolConvert.byteSum(totalDataByte)};

        //协议头 取消息第一个字节
        callbackMessage.setHeaderMsg(DefaultProtocolConvert.hexstring2bytes(replyHeadTag));
        //协议尾 为校验码
        callbackMessage.setFooterMsg(DefaultProtocolConvert.hexstring2bytes(replyEndTag));

        callbackMessage.setBodyMsg(DefaultProtocolConvert.add(totalDataByte, protocolVerify_byte));

        logger.info("[" + msg.getImei() + "]心跳回写=" + DefaultProtocolConvert.bytes2hexstring(callbackMessage.getHeaderMsg()) +
                DefaultProtocolConvert.bytes2hexstring(callbackMessage.getBodyMsg()) +
                DefaultProtocolConvert.bytes2hexstring(callbackMessage.getFooterMsg()) +
                " byte=" + callbackMessage.toString());

        //判断是否需要 重新启动下发升级包
        judgeSendUpgradePackage(msg);

        return callbackMessage;
    }

    /**
     * @param msg
     * @throws Exception
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zl
     * @version: 2.0
     * @date: 2018年7月16日 下午5:08:47
     */
    private void judgeSendUpgradePackage(ReceiveMessage msg) throws Exception {
        String imei = msg.getImei();//设备唯一识别码
        UpgradeDto upgradeDto = CollectRedisCacheService.getUpgradeDto(imei);

        //升级缓存信息为空
        if (null == upgradeDto) {
            return;
        }

        //升级缓存信息 当前包下载时间为空
        if (null == upgradeDto.getCurrentPackageDownTime()) {
            return;
        }

        //升级状态为已完成 升级状态 0 未开始 1正在升级 2已完成
        if ("2".equals(upgradeDto.getStatus())) {
            return;
        }

        Date currentTime = new Date();
        Date upgradePackageDownTime = DateHelper.parseString(upgradeDto.getCurrentPackageDownTime(), DateHelper.PATTERN_TIME);
        long interval = (currentTime.getTime() - upgradePackageDownTime.getTime()) / 1000;
        if (interval > TIMEOUT) {//大于10秒  认为盒子已经出现异常  重新下发升级包
            //下发升级包
            EdenepBoxUpgradeUtil.downPackage(upgradeDto.getFilePath(), imei);
        }
    }

    public static void main(String[] args) {
        System.out.println(DefaultProtocolConvert.hexstring2bytes("FD")[0]);
        byte a[] = new byte[]{-3};
        System.out.println(DefaultProtocolConvert.bytes2hexstring(a));

        String as = "fb".toUpperCase();
        String bs = new StringBuffer(as).reverse().toString();
        System.out.println(as + " == " + bs);
    }
}
