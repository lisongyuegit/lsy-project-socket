package lsy.project.socket.api.coder;

import lsy.project.socket.api.model.CallbackMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


/**
 * 回写消息编码
 *
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public class CallBackMessageEncoder extends MessageToByteEncoder {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) {
        CallbackMessage message = (CallbackMessage) o;
        byteBuf.writeBytes(message.getHeaderMsg());
        byteBuf.writeBytes(message.getBodyMsg());
        byteBuf.writeBytes(message.getFooterMsg());

    }
}
