package lsy.project.socket.api.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * netty启动类
 *
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public class NettyServer {
    static Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private int port;
    private String ip;

    public NettyServer(String ip, int port) {
        this.port = port;
        this.ip = ip;
    }

    /* *
     * NioEventLoopGroup实际上就是个线程,
     * NioEventLoopGroup在后台启动了n个NioEventLoop来处理Channel事件,
     * 每一个NioEventLoop负责处理m个Channel,
     * NioEventLoopGroup从NioEventLoop数组里挨个取出NioEventLoop来处理Channel
     */
    private static ServerBootstrap bootstrap;

    private static ServerBootstrap httpbootstrap;

    public void start() throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker).channel(NioServerSocketChannel.class)
                    .childHandler(new NettyServerInitializer(port));
            // Start the client.
            ChannelFuture ch = bootstrap.bind(port).sync();
            ch.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("netty Server 启动失败" + e);
        } finally {
            if (boss != null) {
                boss.shutdownGracefully();
            }
            if (worker != null) {
                worker.shutdownGracefully();
            }
            logger.info("Stop Server!");
        }
    }

    public void startMore() throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new NettyServerInitializer(port));
            // Start the client.
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            bootstrap.childOption(ChannelOption.SO_LINGER, 0);
            bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
            bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.childOption(ChannelOption.SO_RCVBUF, 1024000);
            bootstrap.childOption(ChannelOption.SO_SNDBUF, 1024000);
            ChannelFuture future = bootstrap.bind(port).sync();
            logger.info("[TCP 启动了]绑定端口[" + port + "]");
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("netty Server 启动失败" + e);
        } finally {
            if (boss != null) {
                boss.shutdownGracefully();
            }
            if (worker != null) {
                worker.shutdownGracefully();
            }
            logger.info("Stop Server!");
        }
    }

    public void startUDP() throws Exception {
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();//udp不能使用ServerBootstrap
            bootstrap.group(workGroup);
            bootstrap.channel(NioDatagramChannel.class);
            // Start the client.
            bootstrap.option(ChannelOption.SO_BROADCAST, true);
            bootstrap.option(ChannelOption.SO_RCVBUF, 1024 * 1024);// 设置UDP读缓冲区为1M  
            bootstrap.option(ChannelOption.SO_SNDBUF, 1024 * 1024);// 设置UDP写缓冲区为1M 
            bootstrap.handler(new NettyUDPServerInitializer(port));
            ChannelFuture future = bootstrap.bind(port).sync();
            logger.info("[UDP 启动了]绑定端口[" + port + "]");
            future.channel().closeFuture().await();
        } catch (Exception e) {
            logger.error("netty Server 启动失败" + e);
        } finally {
            if (workGroup != null) {
                workGroup.shutdownGracefully();
            }
            logger.info("Stop Server!");
        }
    }

    /**
     * 启动http服务
     *
     * @throws Exception
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年8月16日 上午11:23:56
     */
    public void startHttp() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            httpbootstrap = new ServerBootstrap();
            httpbootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            // server端发送的是httpResponse，所以要使用HttpResponseEncoder进行编码
                            ch.pipeline().addLast(
                                    new HttpResponseEncoder());
                            // server端接收到的是httpRequest，所以要使用HttpRequestDecoder进行解码
                            ch.pipeline().addLast(
                                    new HttpRequestDecoder());
                            ch.pipeline().addLast(
                                    new NettyHttpServerHandler(port));
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = httpbootstrap.bind(port).sync();
            logger.info("[Http server 启动了]绑定端口[" + port + "]");
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("netty http Server 启动失败" + e);
        } finally {
            if (null != bossGroup) {
                bossGroup.shutdownGracefully();
            }
            if (null != workerGroup) {
                workerGroup.shutdownGracefully();
            }
            logger.info("Stop http Server!");
        }
    }

}  
