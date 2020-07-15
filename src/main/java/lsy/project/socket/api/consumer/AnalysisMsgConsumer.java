package lsy.project.socket.api.consumer;


import com.lsy.base.result.ResultVo;
import com.lsy.rabbitmq.client.consumer.AbstractMqConsumer;

import java.util.Map;

/**
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public class AnalysisMsgConsumer extends AbstractMqConsumer {
    @Override
    public ResultVo invoke(Map<String, String> param) throws Exception {
        return null;
    }
}
