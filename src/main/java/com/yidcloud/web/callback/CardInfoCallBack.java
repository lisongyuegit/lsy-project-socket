package com.yidcloud.web.callback;

import java.util.Date;

import com.lsy.base.date.DateHelper;
import com.yidcloud.web.convert.DefaultProtocolConvert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yidcloud.web.model.CallbackMessage;
import com.yidcloud.web.model.ReceiveMessage;


/**
 * 事件上报的时候回写类
 *
 * @author 胡洪瑜 huhongyu@edenep.net
 * @version 2.0
 * @company 易登科技
 * @since 2017/11/15 14:19
 */
public class CardInfoCallBack extends AbstractCallback {

    private static Logger logger = LoggerFactory.getLogger(CardInfoCallBack.class);

    @Override
    CallbackMessage createCallBackMsg(ReceiveMessage msg) throws Exception {
        logger.info("实现事件上报回写方法");
        //判断是否需要回写
        CallbackMessage callbackMessage = new CallbackMessage();
        //直接取协议协议头
        callbackMessage.setHeaderMsg(DefaultProtocolConvert.hexstring2bytes(msg.getHeadTag()));
        //直接取协议协议尾
        callbackMessage.setFooterMsg(DefaultProtocolConvert.hexstring2bytes(msg.getEndTag()));
        //取上传流水号
        String bytesStr = msg.getMsgByte();
        String seq = bytesStr.split("2c")[2];
        seq = DefaultProtocolConvert.hexstring2string(seq);
        //时间
        String date = DateHelper.formatDate(new Date(), "yyMMddHHmmss");
        //拼接body
        String result = msg.getImei() + "," + "ACK," + seq + "," + date + "," + seq + "," + "INFO";
        callbackMessage.setBodyMsg(DefaultProtocolConvert.hexstring2bytes(
                DefaultProtocolConvert.stringToASCII(result, result.length(), "")));
        return callbackMessage;
    }
}
