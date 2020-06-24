package com.yidcloud.web.callback;

import com.yidcloud.web.model.CallbackMessage;
import com.yidcloud.web.model.ReceiveMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 测试默认回写类
 *
 * @author 胡洪瑜 huhongyu@edenep.net
 * @version 2.0
 * @company 易登科技
 * @since 2017/11/14 22:46
 */
public class TestCallBack extends AbstractCallback {
    private  static Logger logger = LoggerFactory.getLogger(TestCallBack.class);
    @Override
    CallbackMessage createCallBackMsg(ReceiveMessage msg) throws Exception {
        CallbackMessage callbackMessage = new CallbackMessage();
        callbackMessage.setBodyMsg("body".getBytes());
        callbackMessage.setHeaderMsg("head".getBytes());
        callbackMessage.setFooterMsg("foot".getBytes());
        return callbackMessage;
    }
}
