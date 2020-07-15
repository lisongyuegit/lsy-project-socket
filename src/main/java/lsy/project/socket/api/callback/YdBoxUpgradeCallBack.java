package lsy.project.socket.api.callback;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.lsy.base.date.DateHelper;
import com.lsy.rabbitmq.client.MqClientManager;
import lsy.project.socket.api.cache.CollectRedisCacheService;
import lsy.project.socket.api.contants.CollectContants;
import lsy.project.socket.api.convert.DefaultProtocolConvert;
import lsy.project.socket.api.dto.UpgradeDto;
import lsy.project.socket.api.model.CallbackMessage;
import lsy.project.socket.api.model.ReceiveMessage;
import lsy.project.socket.api.util.EdenepBoxUpgradeUtil;
import lsy.project.socket.api.util.StandardConstantResp;
import lsy.project.socket.api.util.WbBoxUpgradeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;


/**
 * 易登盒子升级回写
 *
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public class YdBoxUpgradeCallBack extends AbstractCallback {

    private static Logger logger = LoggerFactory.getLogger(YdBoxUpgradeCallBack.class);

    /**
     * 分包大小
     */
    private static final BigDecimal PACKAGE_SIZE = BigDecimal.valueOf(64);

    @Override
    CallbackMessage createCallBackMsg(ReceiveMessage msg) throws Exception {

        CallbackMessage callbackMessage = null;

        //设备唯一识别码
        String imei = msg.getImei();
        UpgradeDto upgradeDto = CollectRedisCacheService.getUpgradeDto(imei);
        if (null == upgradeDto) {
            return null;
        }

        //客户端回复的包号
        byte[] sourceByte = DefaultProtocolConvert.hexstring2bytes(msg.getMsgByte());
        byte[] statusByte = null;
        byte[] packageNumByte = null;
        if ("1".equals(upgradeDto.getImeiType())) {
            statusByte = DefaultProtocolConvert.subBytes(sourceByte, 9, 1);
            packageNumByte = DefaultProtocolConvert.subBytes(sourceByte, 7, 2);
        } else if ("0".equals(upgradeDto.getImeiType())) {
            statusByte = DefaultProtocolConvert.subBytes(sourceByte, 22, 1);
            packageNumByte = DefaultProtocolConvert.subBytes(sourceByte, 20, 2);
        } else if ("3".equals(upgradeDto.getImeiType())) {
            packageNumByte = DefaultProtocolConvert.subBytes(sourceByte, 24, 2);
            statusByte = DefaultProtocolConvert.subBytes(sourceByte, 26, 1);
            logger.info("packageNumByte:{},statusByte:{}", packageNumByte, statusByte);
        }

        String status = DefaultProtocolConvert.bytes2hexstring(statusByte);
        boolean downSuccess = status.equals("01") ? true : false;

        String packageNum = DefaultProtocolConvert.bytes2hexstring(packageNumByte).equals("0000") ? "0001" : DefaultProtocolConvert.bytes2hexstring(packageNumByte);
        if ("3".equals(upgradeDto.getImeiType())) {
            packageNum = DefaultProtocolConvert.bytes2hexstring(packageNumByte);
        }
        int iPackageNums = DefaultProtocolConvert.hexstring2int(packageNum);

        //已下载完成 回复终端
        if ((iPackageNums + "").equals(upgradeDto.getPackageTotal()) && downSuccess) {

            callbackMessage = new CallbackMessage();
            if ("3".equals(upgradeDto.getImeiType())) {
                callbackMessage = WbBoxUpgradeUtil.createLastPackageContent(imei, upgradeDto);
            } else {
                //直接取协议协议头
                callbackMessage.setHeaderMsg(DefaultProtocolConvert.hexstring2bytes(upgradeDto.getSendStartTag() == null ? "FD" : upgradeDto.getSendStartTag()));
                //直接取协议协议尾
                callbackMessage.setFooterMsg(DefaultProtocolConvert.hexstring2bytes(upgradeDto.getSendEndTag() == null ? "DF" : upgradeDto.getSendEndTag()));
                callbackMessage.setBodyMsg(EdenepBoxUpgradeUtil.createLastPackageContent(imei, upgradeDto));
            }

            logger.info(String.format(StandardConstantResp.UPGRADE_PACKAGE_DOWN_COMPLETE.getMessage(), imei,
                    DefaultProtocolConvert.bytes2hexstring(callbackMessage.getHeaderMsg()) +
                            DefaultProtocolConvert.bytes2hexstring(callbackMessage.getBodyMsg()) +
                            DefaultProtocolConvert.bytes2hexstring(callbackMessage.getFooterMsg())));

            //更新缓存
            upgradeDto.setStatus("2");
            upgradeDto.setDownEndTime(DateHelper.formatDate(new Date(), DateHelper.PATTERN_TIME));
            CollectRedisCacheService.addServerPushUpgrade(upgradeDto);

            //用于版本回写
            Map<String, String> paramsMap = JSONObject.parseObject(JSON.toJSONString(upgradeDto), new TypeReference<Map<String, String>>() {
            });
            MqClientManager.publish(CollectContants.COLLECT_ANALYSIS_MSG_QUEUE_COMMAND, paramsMap, null);

        } else {
            //记录缓存
            upgradeDto.setCurrentDown(downSuccess ? iPackageNums + 1 + "" : iPackageNums + "");

            CollectRedisCacheService.addServerPushUpgrade(upgradeDto);

            if ("3".equals(upgradeDto.getImeiType())) {
                WbBoxUpgradeUtil.downPackage(upgradeDto.getFilePath(), imei);
            } else {
                //下发升级包
                EdenepBoxUpgradeUtil.downPackage(upgradeDto.getFilePath(), imei);
            }

            if (downSuccess) {
                logger.info(String.format(StandardConstantResp.UPGRADE_CURRENTPACKAGE_DOWN_COMPLETE.getMessage(), imei, upgradeDto.getPackageTotal(), iPackageNums));
            } else {
                logger.info(String.format(StandardConstantResp.ERROR_UPGRADE_CURRENTPACKAGE_DOWN.getMessage(), imei, upgradeDto.getPackageTotal(), iPackageNums));
            }
        }
        return callbackMessage;
    }

}
