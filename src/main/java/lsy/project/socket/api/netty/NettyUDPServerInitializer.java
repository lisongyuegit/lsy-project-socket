/**
 *
 */
package lsy.project.socket.api.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioDatagramChannel;


/**
 *
 *
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public class NettyUDPServerInitializer extends ChannelInitializer<NioDatagramChannel> {

    private int port;

    public NettyUDPServerInitializer(int port) {
        super();
        this.port = port;
    }

    @Override
    protected void initChannel(NioDatagramChannel ch) throws Exception {
        // TODO Auto-generated method stub
        ChannelPipeline pipeline = ch.pipeline();
        /**
         * 从TCP与UDP的区别讲起 
         * 网络数据经过路由器,如果数据很小,没有超过路由器的封包大小,就会直接直接经过路由器到达下一个路由器,一层一层最终到达目的地 
         * 如果数据很大,这里指一个发送,超过了路由器的封包大小,那么路由器就会把这个数据包进行拆分,比如拆分成A B 
         * C三个包,这三个包都没有超过路由器的封包大小,到达下一个路由器的时候,TCP与UDP的区别就来了： 
         * TCP收到A的时候,会resp通知源路由器,A到达,B C包依然如此,如果由于网络的各种原因，目的路由收到了A 
         * C,B没有收到，TCP会要求源路由把B包重新发一次 
         * ，直到ABC包目的路由都接受到了，那么目的路由把ABC包重新组成起始包，继续往下一个路由发送 
         * ，这就是TCP安全连接的由来，只要发送，我就能保证目的一定能收到（网络断开能检测到） 
         * UDP则不是这样，如果ABC包拆分之后，目的路由只收到AC 
         * ，经过检测，B没有被收到，那么此包就会被当作不完整，直接被丢弃。由于UDP没有resp的通知过程 
         * ，所以，UDP的传输效率要高一些，当然安全性也低一些 
         * 由上面的这些可以得出结论：UDP是绝对不会被粘包，因为路由器收到的只会是完整数据才会继续下发，什么粘包处理完全没有必要 
         * 一般网络编程时候，也会定义数据包头，包体 TCP接收数据的时候，可以先接收包头进行安全验证，通过继续接受包体，不通过直接断开连接 
         * UDP接受则没有办法这样做 
         * ，你再大的一个数据，一个RECV,也是直接接受，不能说我先接受多长，这样是不可能的（不过一般大文件数据，都不会用UDP这种不安全传输） 
         */
        // 添加UDP解码器
//         pipeline.addLast("datagramPacketDecoder", new UDPMessageDecoder(port));  
        // 添加UDP编码器
//         pipeline.addLast("datagramPacketEncoder", ); 

        pipeline.addLast("handler", new NettyUDPServerHandler(port));//消息处理器  

    }

}
