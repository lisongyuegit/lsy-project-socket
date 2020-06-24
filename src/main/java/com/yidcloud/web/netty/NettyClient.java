package com.yidcloud.web.netty;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yidcloud.web.convert.DefaultProtocolConvert;
import com.yidcloud.web.model.ReceiveMessage;

/**
 * 当做客户端连接转发服务器
 * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
 * @copyright: Copyright (c) 2018
 * @company: 易登科技
 * @author: zhouliang@edenep.net
 * @version: 2.0
 * @date: 2018年8月1日 下午11:33:24
 */
public class NettyClient {
    
    static Logger logger = LoggerFactory.getLogger(NettyServer.class);

    public static Socket socket = null;
    private static int port;
    private static String host;
    private static OutputStream output = null;
    
    public static boolean isForwardConnected=false;
    
    public static boolean isForwardConnected_gb=false;
    
    public NettyClient(int port, String host) {
        NettyClient.port = port;
        NettyClient.host = host;
    }
    
    /**
     * 启动socket连接
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018 2018年8月2日 下午4:33:05
     */
    public static void start(){
        
        try {
            //关闭连接
            close();
            if(null==host||host.isEmpty()) {
                return;
            }
            //表示连接到服务器的 地址以及端口
            SocketAddress address = new InetSocketAddress(host,port);
            socket = new Socket();
            socket.connect(address, 0);
            socket.setKeepAlive(true);
            isForwardConnected = true;
            logger.info(String.format("客户端 转发到服务器[%s:%s] 连接成功", host,port));
        } catch (Exception e) {
            logger.error(String.format("客户端 转发到服务器[%s:%s] 连接异常", host,port));
        } finally {
            /*if(null!=socket && socket.isConnected()) {
                logger.info(String.format("客户端 转发到服务器[%s:%s] 连接已建立", host,port));
            }*/
        }
    }
    
    public static void startGb(){
    	try {
            //关闭连接
            close();
            if(null==host||host.isEmpty()) {
                return;
            }
            //表示连接到服务器的 地址以及端口
            SocketAddress address = new InetSocketAddress(host,port);
            socket = new Socket();
            socket.connect(address, 0);
            socket.setKeepAlive(true);
            logger.info(String.format("客户端 转发到GB服务器[%s:%s] 连接成功", host,port));
        } catch (Exception e) {
            logger.error(String.format("客户端 转发到GB服务器[%s:%s] 连接异常", host,port));
        }
    }
    
    /**
     * 数据转发
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年8月2日 下午4:32:24
     * @param msg 接收到的原始消息
     */
    public static void dataForward(ReceiveMessage msg) {
        if(null!=NettyClient.socket && NettyClient.socket.isConnected()) {
            byte[] allInfoByteArray = DefaultProtocolConvert.hexstring2bytes(msg.getMsgByte()); // 根据hex字符串得到字节数组
            try {
                output = socket.getOutputStream();
                if (output == null) {
                    output = socket.getOutputStream();
                }
                socket.getOutputStream().write(allInfoByteArray);
                socket.getOutputStream().flush();
                socket.getOutputStream().close();
                logger.info(String.format("客户端 转发到服务器[%s:%s] 数据转发成功,原始数据{%s}", host,port,DefaultProtocolConvert.bytes2hexstring(allInfoByteArray)));
            } catch (Exception e) {
                //重连
                start();
                //数据转发
                dataForward(msg);
            }
        }else {
            //重连
            start();
        }
    }

    /**
     * 关闭连接，释放资源
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: 
     * @version: 2.0
     * @date: 2018年8月9日 下午6:09:43
     */
    private static void close() {
        
        if (output != null) {
            try {
                output.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            output = null;
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            socket = null;
        }
    }
}