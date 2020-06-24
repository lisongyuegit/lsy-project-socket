package com.yidcloud.web.callback;

import com.yidcloud.web.model.CallbackMessage;
import com.yidcloud.web.model.ReceiveMessage;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 回写数据的基类
 *
 * @author 胡洪瑜 huhongyu@edenep.net
 * @version 2.0
 * @company 易登科技
 * @since 2017/11/14 21:53
 */
public abstract class AbstractCallback {
    private  static Logger logger = LoggerFactory.getLogger(AbstractCallback.class);

    /**
     * 创建各个协议对应的回写数据
     * @return Object
     * @param msg
     * @since  2017/11/14  22:38
     * @throws  Exception
     */
    abstract CallbackMessage createCallBackMsg(ReceiveMessage msg)throws Exception;

    /**
     * 调用回写数据
     * @param ctx msg
     * @since  2017/11/14  22:39
     */
    public void callBack(ChannelHandlerContext ctx, ReceiveMessage msg)throws Exception{
        CallbackMessage callbackMessage = createCallBackMsg(msg);
        if(callbackMessage!=null && null !=callbackMessage.getBodyMsg()){
            logger.info("回写的消息内容为："+callbackMessage.getHeaderMsg()
                    +callbackMessage.getBodyMsg()
                    +callbackMessage.getFooterMsg());
            ctx.writeAndFlush(callbackMessage);
        }
    }
}
