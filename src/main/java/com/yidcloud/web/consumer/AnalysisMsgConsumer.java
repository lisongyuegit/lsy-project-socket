package com.yidcloud.web.consumer;


import com.lsy.base.result.ResultVo;
import com.lsy.rabbitmq.client.consumer.AbstractMqConsumer;

import java.util.Map;

public class AnalysisMsgConsumer extends AbstractMqConsumer {
    @Override
    public ResultVo invoke(Map<String, String> param) throws Exception {
        return null;
    }
}
