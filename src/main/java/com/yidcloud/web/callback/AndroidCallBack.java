package com.yidcloud.web.callback;

import java.util.Date;

import com.lsy.base.date.DateHelper;
import com.lsy.base.string.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yidcloud.web.cache.CollectRedisCacheService;
import com.yidcloud.web.convert.DefaultProtocolConvert;
import com.yidcloud.web.model.CallbackMessage;
import com.yidcloud.web.model.ReceiveMessage;


/**
 * 
 * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
 * @copyright: Copyright (c) 2018
 * @company: 易登科技
 * @author: 
 * @version: 2.0
 * @date: 2018 2018年5月17日 下午4:08:07
 */
public class AndroidCallBack extends AbstractCallback {

    private static Logger logger = LoggerFactory.getLogger(AndroidCallBack.class);

    @Override
    CallbackMessage createCallBackMsg(ReceiveMessage msg) throws Exception {
        //判断是否需要回写
        CallbackMessage callbackMessage = new CallbackMessage();
        //直接取协议协议头
        callbackMessage.setHeaderMsg(DefaultProtocolConvert.hexstring2bytes(msg.getHeadTag()));
        //直接取协议协议尾
        callbackMessage.setFooterMsg(DefaultProtocolConvert.hexstring2bytes(msg.getEndTag()));
        
        //时间
        String date = DateHelper.formatDate(new Date(), "yyyyMMddHHmmss");
        
        //取上传账号
        String bytesStr = msg.getMsgByte();
        String account = DefaultProtocolConvert.hexstring2string(bytesStr.split(msg.getSplitStr())[1]);
        
        String status = "0";//0正常 -1 已解绑
        String curCacheImeiAccount = CollectRedisCacheService.getImei2Account(msg.getImei());//当前缓存中IMEI绑定的账号
        //缓存中设备绑定的账号为空 或者 取到的账号与上传的账号不匹配  则状态置位-1
        if (StringHelper.isBlank(curCacheImeiAccount)
                || !account.equals(curCacheImeiAccount)) {
            status = "-1";
        }
        logger.info(msg.getImei()+" : Android事件上报回写方法,会写信息["+status+"]");
        callbackMessage.setBodyMsg(DefaultProtocolConvert.hexstring2bytes(
                DefaultProtocolConvert.stringToASCII(status, status.length(), "")));
        return callbackMessage;
    }
}
