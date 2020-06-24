package com.yidcloud.web.consumer;

import com.lsy.base.result.ResultVo;
import com.lsy.base.utils.ConvertHelper;
import com.lsy.rabbitmq.client.consumer.AbstractMqConsumer;
import com.yidcloud.api.contants.CollectContants;
import com.yidcloud.api.entity.Protocol;
import com.yidcloud.api.entity.ProtocolAnalysis;
import com.yidcloud.web.cache.CollectRedisCacheService;
import com.yidcloud.web.handler.AbstractMessageAnalysisHandler;
import com.yidcloud.web.handler.DefaultMessageAnalysisHandler;
import com.yidcloud.web.model.ReceiveMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * (一句话描述该类做什么)
 *
 * @author 胡洪瑜 huhongyu@edenep.net
 * @version 2.0
 * @company 易登科技
 * @since 2017/11/13 11:03
 */
public class AuthMsgConsumer extends AbstractMqConsumer {
    static Logger logger = LoggerFactory.getLogger(AuthMsgConsumer.class);
    @Override
    public ResultVo invoke(Map<String, String> param){
        ResultVo vo = new ResultVo();
        ReceiveMessage msg = (ReceiveMessage) ConvertHelper.mapStringToObject(param,ReceiveMessage.class);
        //获取协议
        Map<String,Object> protocolMap = CollectRedisCacheService
                .getProtocolsFromCache(msg.getHeadTag(),msg.getPort(),msg.getMid());
        if(protocolMap==null){
            return null;
        }else{
            Protocol protocol = (Protocol) protocolMap.get("protocol");
            List<ProtocolAnalysis> analyses = (List<ProtocolAnalysis>) protocolMap.get("protocolAnalyses");
            if(protocol!=null){
                try {
                    String analysisType = protocol.getAnalysisType();
                    AbstractMessageAnalysisHandler handler;
                    if(CollectContants.PROTOCOL_ANALYSIS_TYPE_01.equals(analysisType)){
                        String clazz = protocol.getAnalysisClazz();
                        handler = (AbstractMessageAnalysisHandler) ClassLoader.getSystemClassLoader()
                                .loadClass(clazz).newInstance();
                    }else{
                        handler = new DefaultMessageAnalysisHandler();
                    }
                    handler.process(msg,protocol,analyses);
                }catch (Exception e){
                    vo.setError_no(-1);
                    vo.setError_info(e.getMessage());
                    logger.error("",e);
                }

            }
        }
        return vo;
    }
}
