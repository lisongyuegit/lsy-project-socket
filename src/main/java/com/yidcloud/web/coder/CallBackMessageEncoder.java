package com.yidcloud.web.coder;

import com.yidcloud.web.model.CallbackMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 回写消息编码
 *
 * @author 胡洪瑜 huhongyu@edenep.net
 * @version 2.0
 * @company 易登科技
 * @since 2017/11/15 10:46
 */
public class CallBackMessageEncoder extends MessageToByteEncoder {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf)
            throws Exception {
        CallbackMessage message = (CallbackMessage) o;
        byteBuf.writeBytes(message.getHeaderMsg());
        byteBuf.writeBytes(message.getBodyMsg());
        byteBuf.writeBytes(message.getFooterMsg());

    }
}
