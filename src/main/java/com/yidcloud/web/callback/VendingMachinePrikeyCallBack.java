package com.yidcloud.web.callback;

import com.lsy.base.string.StringHelper;
import com.yidcloud.web.convert.DefaultProtocolConvert;
import com.yidcloud.web.model.CallbackMessage;
import com.yidcloud.web.model.ReceiveMessage;
import com.yidcloud.web.util.VendingMachineUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @version 2.0
 * @company 易登科技
 * @since 2017/11/15 14:16
 */
public class VendingMachinePrikeyCallBack extends AbstractCallback {

    private static Logger logger = LoggerFactory.getLogger(VendingMachinePrikeyCallBack.class);
    
    @Override
    CallbackMessage createCallBackMsg(ReceiveMessage msg) throws Exception {
        CallbackMessage callbackMessage = new CallbackMessage();
        //直接取协议协议头
        callbackMessage.setHeaderMsg(DefaultProtocolConvert.hexstring2bytes(msg.getHeadTag()));
        //直接取协议协议尾
        callbackMessage.setFooterMsg(DefaultProtocolConvert.hexstring2bytes(msg.getEndTag()));

        Map<String, String> receiveMap = VendingMachineUtil.getReceiveValueMap(msg);
        String mid = msg.getMid();
        String result = VendingMachineUtil.getVcmSendResult(mid, receiveMap);
        logger.info("result==============="+result);
        if(StringHelper.isBlank(result)){
            return null;
        }
        callbackMessage.setBodyMsg(DefaultProtocolConvert.hexstring2bytes(
                DefaultProtocolConvert.stringToASCII(result, result.length(), "")));
        return callbackMessage;
    }

    public static void main(String[] args) {
    }
}
