package lsy.project.socket.api.callback;

import lsy.project.socket.api.model.CallbackMessage;
import lsy.project.socket.api.model.ReceiveMessage;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 回写数据的基类
 *
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public abstract class AbstractCallback {
    private static Logger logger = LoggerFactory.getLogger(AbstractCallback.class);

    /**
     * 创建各个协议对应的回写数据
     *
     * @param msg
     * @return Object
     * @throws Exception
     * @since 2017/11/14  22:38
     */
    abstract CallbackMessage createCallBackMsg(ReceiveMessage msg) throws Exception;

    /**
     * 调用回写数据
     *
     * @param ctx msg
     * @since 2017/11/14  22:39
     */
    public void callBack(ChannelHandlerContext ctx, ReceiveMessage msg) throws Exception {
        CallbackMessage callbackMessage = createCallBackMsg(msg);
        if (callbackMessage != null && null != callbackMessage.getBodyMsg()) {
            logger.info("回写的消息内容为：" + callbackMessage.getHeaderMsg()
                    + callbackMessage.getBodyMsg()
                    + callbackMessage.getFooterMsg());
            ctx.writeAndFlush(callbackMessage);
        }
    }
}
