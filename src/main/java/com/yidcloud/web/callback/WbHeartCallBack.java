package com.yidcloud.web.callback;

import com.lsy.base.date.DateHelper;
import com.yidcloud.api.dto.UpgradeDto;
import com.yidcloud.web.cache.CollectRedisCacheService;
import com.yidcloud.web.convert.DefaultProtocolConvert;
import com.yidcloud.web.model.CallbackMessage;
import com.yidcloud.web.model.ReceiveMessage;
import com.yidcloud.web.util.WbBoxUpgradeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * 外包盒子自定义心跳协议数据回写
 * @author zhouliang@edenep.net
 * @version 2.0
 * @description:
 * @copyright: Copyright (c) 2018
 * @company: 易登科技
 * @date 2019/12/13 0013 上午 9:23
 */
public class WbHeartCallBack extends AbstractCallback {

    private static Logger logger = LoggerFactory.getLogger(WbHeartCallBack.class);

    //盒子升级包 回复超时时间
    private static final long TIMEOUT = 10;

    @Override
    CallbackMessage createCallBackMsg(ReceiveMessage msg) throws Exception {

        byte[] headByte = DefaultProtocolConvert.hexstring2bytes(msg.getHeadTag());

        byte[] cmdByte = new byte[] {(byte) DefaultProtocolConvert.hexstring2int("09").intValue()};
        byte[] ydCmdByte = new byte[] {(byte) DefaultProtocolConvert.hexstring2int("01").intValue()};

        byte[] imeibyte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.stringToASCII(msg.getImei(), msg.getImei().length(), ""));

        byte[] encryptionByte = new byte[] {(byte) DefaultProtocolConvert.hexstring2int("01").intValue()};

        //数据长度
        byte[] dataContentByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(0),4));

        //从帧长到数据内容
        byte[] totalDataByte = (DefaultProtocolConvert.add(DefaultProtocolConvert.add(
                DefaultProtocolConvert.add(DefaultProtocolConvert
                        .add(cmdByte, ydCmdByte), imeibyte),encryptionByte),dataContentByte));

        //协议校验码
        byte[] yhbyte = new byte[] {(byte) DefaultProtocolConvert.byteSum(totalDataByte)};

        CallbackMessage result = new CallbackMessage();
        //协议头
        result.setHeaderMsg(headByte);
        result.setBodyMsg(totalDataByte);
        result.setFooterMsg(yhbyte);

        logger.info("["+msg.getImei()+"]外包盒子自定义心跳数据回写="+DefaultProtocolConvert.bytes2hexstring(result.getHeaderMsg())+
                DefaultProtocolConvert.bytes2hexstring(result.getBodyMsg())+
                DefaultProtocolConvert.bytes2hexstring(result.getFooterMsg())+" byte="+result.toString());

        //判断是否需要 重新启动下发升级包
        judgeSendUpgradePackage(msg);

        return result;
    }

    /**
     *
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zl
     * @version: 2.0
     * @date: 2018年7月16日 下午5:08:47
     * @param msg
     * @throws Exception
     */
    private void judgeSendUpgradePackage(ReceiveMessage msg) throws Exception {
        //设备唯一识别码
        String imei = msg.getImei();
        UpgradeDto upgradeDto = CollectRedisCacheService.getUpgradeDto(imei);

        //升级缓存信息为空
        if(null == upgradeDto) {
            return;
        }

        //升级缓存信息 当前包下载时间为空
        if(null == upgradeDto.getCurrentPackageDownTime()) {
            return;
        }

        //升级状态为已完成 升级状态 0 未开始 1正在升级 2已完成
        if("2".equals(upgradeDto.getStatus())) {
            return;
        }

        Date currentTime = new Date();
        Date upgradePackageDownTime = DateHelper.parseString(upgradeDto.getCurrentPackageDownTime(), DateHelper.PATTERN_TIME);
        long interval = (currentTime.getTime() - upgradePackageDownTime.getTime())/1000;
        //大于10秒  认为盒子已经出现异常  重新下发升级包
        if(interval>TIMEOUT) {
            //下发升级包
            WbBoxUpgradeUtil.downPackage(upgradeDto.getFilePath(), imei);
        }
    }
}
