package lsy.project.socket.api.netty;

import com.lsy.base.utils.PropertiesHelper;
import lsy.project.socket.api.coder.CallBackMessageEncoder;
import lsy.project.socket.api.coder.ReceiveMessageAuthDecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


/**
 * 消息处理
 *
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    static Logger logger = LoggerFactory.getLogger(NettyServerInitializer.class);

    private int port;

    private int requestTimeOut = 600;//默认值十分钟没发数据,自动

    public NettyServerInitializer(int port) {
        super();
        this.port = port;
        this.requestTimeOut = getRequestTimeOut();
    }

    /**
     * 获取客户端超时时间
     *
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018年7月7日 下午7:23:46
     */
    private int getRequestTimeOut() {
        try {
            PropertiesHelper helper = new PropertiesHelper("system.properties");
            requestTimeOut = helper.getIntegerProperty("RequestTimeOut");
        } catch (NumberFormatException ex) {
            logger.error("配置项RequestTimeOut定义错误，其不是一个有效的数字");
        } catch (Exception ex) {
            logger.info("配置项RequestTimeOut不存在，或定义错误.");
        }
        return requestTimeOut;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 字符串解码 和 编码
        pipeline.addLast(new CallBackMessageEncoder());
        pipeline.addLast(new ReceiveMessageAuthDecoder(port));
        pipeline.addLast(new IdleStateHandler(requestTimeOut, 60 * 60 * 24, 60 * 60 * 24, TimeUnit.SECONDS));
        // 自己的逻辑Handler
        pipeline.addLast(new NettyServerHandler());
    }

}
