package lsy.project.socket.api.handler;

import com.lsy.base.result.ResultVo;
import com.lsy.base.string.StringHelper;
import com.lsy.base.utils.ConvertHelper;
import com.lsy.base.utils.ErrorCodeManage;
import com.lsy.rabbitmq.client.MqClientManager;
import lsy.project.socket.api.contants.CollectContants;
import lsy.project.socket.api.convert.DefaultProtocolConvert;
import lsy.project.socket.api.entity.Protocol;
import lsy.project.socket.api.entity.ProtocolAnalysis;
import lsy.project.socket.api.enums.BooleanCharEnum;
import lsy.project.socket.api.model.ReceiveMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


/**
 * 消息解码基类
 *
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public abstract class AbstractMessageAnalysisHandler {

    static Logger logger = LoggerFactory.getLogger(AbstractMessageAnalysisHandler.class);

    /**
     * 具体解码实现,抽象方法，只需要把消息体解析到queueParamsMap就OK
     *
     * @param receiveMessage
     * @return ResultVo
     * @since 2017/10/20 22:55
     */
    abstract ResultVo invoke(ReceiveMessage receiveMessage, Protocol protocol, List<ProtocolAnalysis> analyses, Map<String, String> queueParamsMap)
            throws Exception;

    /**
     * 公共操作
     *
     * @param receiveMessage protocol analyses
     * @return
     * @since 2017/11/11 18:24
     */
    public ResultVo process(ReceiveMessage receiveMessage, Protocol protocol, List<ProtocolAnalysis> analyses) throws Exception {
        ResultVo vo = new ResultVo();
        if (receiveMessage == null || StringHelper.isBlank(receiveMessage.getMid()) || receiveMessage.getPort() == 0) {
            vo.setError_no(-10001);
            String errorMsg = ErrorCodeManage.getErrorMessage(-10001);
            vo.setError_info("接受消息错误");
            logger.error(errorMsg);
            return vo;
        }
        logger.info("接收到的消息为：" + receiveMessage.toString());
        //*公共数据组装 start*//
        //需要把解析后的数据以map的形式放入队列
        Map<String, String> queueParamsMap = null;
        //原始数据
        byte[] msgBody = DefaultProtocolConvert.hexstring2bytes(receiveMessage.getMsgByte());

        if (analyses == null || analyses.size() < 1) {
            queueParamsMap = null;
            vo.setResult(queueParamsMap);
            return vo;
        }

        /**
         * 把原始数据转hexstring，传递给分析系统
         */
        queueParamsMap = ConvertHelper.objectToMapString(receiveMessage);
        // queueParamsMap.remove("msgByte");
        String sourceHexstring = DefaultProtocolConvert.bytes2hexstring(msgBody, 0, msgBody.length);
        queueParamsMap.put("sourceHexstring", sourceHexstring);
        queueParamsMap.put("mid", protocol.getMid());
        queueParamsMap.put("protocolVersion", protocol.getVersion());
        //*公共数据组装 end*//
        vo = invoke(receiveMessage, protocol, analyses, queueParamsMap);
        if (vo.getError_no() < 0) {
            logger.error(vo.getError_info());
            logger.error("数据解析失败：" + sourceHexstring);
        } else {
            //把解析后的数据放入到队列中
            Map<String, String> paramsMap = (Map) vo.getResult();
            //补丁处理， 把点位上传时间全部用服务器当前时间  ==》 gpsTime 改为gpsTimeTerminal

            paramsMap.put("gpsTimeTerminal", paramsMap.get("gpsTime"));
            if ("007".equals(protocol.getMid())) {
                paramsMap.put("imei", String.format("TTX%s", StringHelper.isBlank(paramsMap.get("IMEI")) ? paramsMap.get("imei") : paramsMap.get("IMEI")));
            } else if ("03".equals(protocol.getMid()) && "2323".equals(receiveMessage.getHeadTag())) {
                paramsMap.put("imei", String.format("%s", StringHelper.isBlank(paramsMap.get("IMEI")) ? paramsMap.get("imei").trim() : paramsMap.get("IMEI").trim()));
                paramsMap.put("mid", "02");
            } else {
                paramsMap.put("gpsTime", receiveMessage.getReceiveDateStr());
                paramsMap.put("imei", String.format("%s", StringHelper.isBlank(paramsMap.get("IMEI")) ? paramsMap.get("imei").trim() : paramsMap.get("IMEI").trim()));
            }
            if (paramsMap != null && paramsMap.size() > 0) {
                MqClientManager.publish(CollectContants.COLLECT_ANALYSIS_MSG_QUEUE_COMMAND, paramsMap, null);
                logger.info(String.format("解析后的数据入队列 :%s,原始hex数据:%s", CollectContants.COLLECT_ANALYSIS_MSG_QUEUE_COMMAND, sourceHexstring));
                /**
                 * 是否需要转发
                 */
                if (BooleanCharEnum.TRUE.getValue().equals(receiveMessage.getIsForward())) {
                    /**
                     * 合法消息入智能垃圾桶的队列
                     */
                    MqClientManager.publish(CollectContants.COLLECT_FORWARD_MSG_QUEUE_COMMAND, paramsMap, null);
                    logger.info(String.format("解析后的数据入队列 :%s,原始hex数据:%s", CollectContants.COLLECT_FORWARD_MSG_QUEUE_COMMAND, sourceHexstring));
                }
            }
        }

        return vo;
    }

}
