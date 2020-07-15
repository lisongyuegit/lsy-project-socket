package lsy.project.socket.api.consumer;

import com.lsy.base.result.ResultVo;
import com.lsy.base.utils.ConvertHelper;
import com.lsy.rabbitmq.client.consumer.AbstractMqConsumer;
import lsy.project.socket.api.cache.CollectRedisCacheService;
import lsy.project.socket.api.contants.CollectContants;
import lsy.project.socket.api.entity.Protocol;
import lsy.project.socket.api.entity.ProtocolAnalysis;
import lsy.project.socket.api.handler.AbstractMessageAnalysisHandler;
import lsy.project.socket.api.handler.DefaultMessageAnalysisHandler;
import lsy.project.socket.api.model.ReceiveMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public class AuthMsgConsumer extends AbstractMqConsumer {
    static Logger logger = LoggerFactory.getLogger(AuthMsgConsumer.class);

    @Override
    public ResultVo invoke(Map<String, String> param) {
        ResultVo vo = new ResultVo();
        ReceiveMessage msg = (ReceiveMessage) ConvertHelper.mapStringToObject(param, ReceiveMessage.class);
        //获取协议
        Map<String, Object> protocolMap = CollectRedisCacheService
                .getProtocolsFromCache(msg.getHeadTag(), msg.getPort(), msg.getMid());
        if (protocolMap == null) {
            return null;
        } else {
            Protocol protocol = (Protocol) protocolMap.get("protocol");
            List<ProtocolAnalysis> analyses = (List<ProtocolAnalysis>) protocolMap.get("protocolAnalyses");
            if (protocol != null) {
                try {
                    String analysisType = protocol.getAnalysisType();
                    AbstractMessageAnalysisHandler handler;
                    if (CollectContants.PROTOCOL_ANALYSIS_TYPE_01.equals(analysisType)) {
                        String clazz = protocol.getAnalysisClazz();
                        handler = (AbstractMessageAnalysisHandler) ClassLoader.getSystemClassLoader()
                                .loadClass(clazz).newInstance();
                    } else {
                        handler = new DefaultMessageAnalysisHandler();
                    }
                    handler.process(msg, protocol, analyses);
                } catch (Exception e) {
                    vo.setError_no(-1);
                    vo.setError_info(e.getMessage());
                    logger.error("", e);
                }

            }
        }
        return vo;
    }
}
