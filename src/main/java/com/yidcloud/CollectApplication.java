package com.yidcloud;

import com.lsy.base.utils.PropertiesHelper;
import com.lsy.rabbitmq.client.MqClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yidcloud.web.netty.NettyClient;
import com.yidcloud.web.netty.NettyServer;


/**
 *
 * @describe: TODO 启动类
 * @Copyright:(c) 2017
 * @company: 易登科技
 * @author: 雷军
 * @email：leijun@edenep.net
 * @version: 2.0
 * @date: 2017年10月20日
 * @time: 上午11:17:16
 */
public class CollectApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(CollectApplication.class);
    
    private static final String SERVER_ADDRESS = "127.0.0.1";
    
    public static void main(String[] args) throws Exception {
        
        //打印欢迎信息
        printWelcome();

        //启动MQ消费者
        MqClientManager.startByCommandName("forwardMsgCommand");
        logger.info("启动forwardMsgCommand");
        MqClientManager.startByCommandName("authMsgCommand1");
        logger.info("启动authMsgCommand1");
        MqClientManager.startByCommandName("sendTerminalMsgToClient");
        logger.info("启动sendTerminalMsgToClient");
        //启动TCP服务
        TcpServer tcpServer = new TcpServer(SERVER_ADDRESS,getPort(PortType.TCP));
        Thread tcpThread = new Thread(tcpServer);
        tcpThread.setName("tcp thread");
        tcpThread.start();
        
                 //启动UDP服务
        UdpServer udpServer = new UdpServer(SERVER_ADDRESS,getPort(PortType.UDP));
        Thread udpThread = new Thread(udpServer);
        udpThread.setName("udp thread");
        udpThread.start();

        //启动HTTP服务
        HttpServer httpServer = new HttpServer(SERVER_ADDRESS,getPort(PortType.HTTP));
        Thread httpThread = new Thread(httpServer);
        httpThread.setName("http thread");
        httpThread.start();

        //启动转发线程
        PropertiesHelper helper = new PropertiesHelper("system.properties");
        boolean isForward = "True".equalsIgnoreCase(helper.getStringProperty("IsForward")==null?"false":helper.getStringProperty("IsForward"));
        if(isForward) {
            ForwardClient forwardClient = new ForwardClient(getServerAddress(PortType.FORWARD_SERVER), getPort(PortType.FORWARD_SERVER));
            Thread fcThread = new Thread(forwardClient);
            fcThread.setName("forwardClient thread");
            fcThread.start();
        }
    }
    
    private static void printWelcome() {
        printFozu();
        logger.info("===================================================");
        logger.info("*                                                 *");
        logger.info("*    WELCOME TO EDENEP COLLECT SOCKET SERVER      *");
        logger.info("*                                                 *");
        logger.info("*    version: 2.0                                 *");
        logger.info("*    Copyright @2018 Edenep All Rights Reserved   *");
        logger.info("*    http://www.edenep.com                        *");
        logger.info("*                                                 *");
        logger.info("===================================================");
    }
    
    public static void printFozu() {
        logger.info("                   _ooOoo_");
        logger.info("                  o8888888o");
        logger.info("                  88\" . \"88");
        logger.info("                  (| -_- |)");
        logger.info("                  O\\  =  /O");
        logger.info("               ____/`---'\\____");
        logger.info("             .'  \\\\|     |//  `.");
        logger.info("            /  \\\\|||  :  |||//  \\");
        logger.info("           /  _||||| -:- |||||-  \\");
        logger.info("           |   | \\\\\\  -  /// |   |");
        logger.info("           | \\_|  ''\\---/''  |   |");
        logger.info("           \\  .-\\__  `-`  ___/-. /");
        logger.info("         ___`. .'  /--.--\\  `. . __");
        logger.info("      .\"\" '<  `.___\\_<|>_/___.'  >'\"\".");
        logger.info("     | | :  `- \\`.;`\\ _ /`;.`/ - ` : | |");
        logger.info("     \\  \\ `-.   \\_ __\\ /__ _/   .-` /  /");
        logger.info("======`-.____`-.___\\_____/___.-`____.-'======");
        logger.info("                   `=---='");
        logger.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        logger.info("            佛祖保佑       永无BUG  永不宕机");
    }

    
    public enum PortType
    {
        //tcp端口
        TCP,
        //udp端口
        UDP,
        //http端口
        HTTP,
        //转发服务器端口
        FORWARD_SERVER
    }
    
    /**
     * 从配置文件中获取监听端口号
     * portType 端口类型
     * @return 端口号
     * @throws NumberFormatException 端口号不是一个合法的整数(6001-65535)
     * @throws Exception 配置文件尚未初始化，或不存在TcpPort定义项
     */
    private static int getPort(PortType portType) throws Exception
    {
        int port = 0;
        try
        {
            PropertiesHelper helper = new PropertiesHelper("system.properties");
            if(PortType.TCP==portType) {
                port = helper.getIntegerProperty("tcp.port");
            }else if(PortType.UDP==portType) {
                port = helper.getIntegerProperty("udp.port");
            }else if(PortType.HTTP==portType) {
                port = helper.getIntegerProperty("http.port");
            }else if(PortType.FORWARD_SERVER==portType) {
                port = helper.getIntegerProperty("Forward.server.port");
            }
            
            if (port <= 6000 || port > 65535)
            {
                logger.error("配置项TcpPort定义错误，其有效取值范围为: 6000-65535");
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex)
        {
            logger.error("配置项TcpPort定义错误，其不是一个有效的数字");
            throw ex;
        } catch (Exception ex)
        {
            logger.info("配置项TcpPort不存在，或定义错误.");
            throw ex;
        }
        return port;
    }
    
    /**
     * 获取转发服务器端ip地址
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: 
     * @version: 2.0
     * @date: 2018 2018年8月1日 上午11:12:31
     * @param portType
     * @return
     * @throws Exception
     */
    private static String getServerAddress(PortType portType) throws Exception
    {
        String serverAddress = null;
        try
        {
            PropertiesHelper helper = new PropertiesHelper("system.properties");
            if(PortType.FORWARD_SERVER==portType) {
                serverAddress = helper.getStringProperty("Forward.server.address");
            }
        } catch (Exception ex)
        {
            logger.info("配置项Forward.server.address不存在，或定义错误.");
            throw ex;
        }
        return serverAddress;
    }
}

class TcpServer implements Runnable{
    
    private static final Logger logger = LoggerFactory.getLogger(CollectApplication.class);
    private String serverAddress;
    private int port;
    public TcpServer(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;
    }
    @Override
    public void run() {
        try {
            new NettyServer(serverAddress,port).startMore();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.error("监听TCP端口时发生IO错误：" + e.getMessage());
        }
    }
}

class UdpServer implements Runnable{
    
    private static final Logger logger = LoggerFactory.getLogger(CollectApplication.class);
    private String serverAddress;
    private int port;
    public UdpServer(String serverAddress, int port) {
        this.port = port;
    }
    @Override
    public void run() {
        try {
            new NettyServer(serverAddress,port).startUDP();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.error("监听UDP端口时发生IO错误：" + e.getMessage());
        }
    }
}

class HttpServer implements Runnable{
    
    private static final Logger logger = LoggerFactory.getLogger(CollectApplication.class);
    private String serverAddress;
    private int port;
    public HttpServer(String serverAddress,int port) {
        this.port = port;
    }
    @Override
    public void run() {
        try {
            new NettyServer(serverAddress,port).startHttp();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.error("监听Http端口时发生IO错误：" + e.getMessage());
        }
    }
}

class ForwardClient implements Runnable{
    
    private static final Logger logger = LoggerFactory.getLogger(CollectApplication.class);
    private String serverAddress;
    private int port;
    public ForwardClient(String serverAddress,int port) {
        this.port = port;
        this.serverAddress = serverAddress;
    }
    @Override
    public void run() {
        try {
            new NettyClient(port,serverAddress);
            NettyClient.start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.error("启动转发客户端发生IO错误：" + e.getMessage());
        }
    }
}
