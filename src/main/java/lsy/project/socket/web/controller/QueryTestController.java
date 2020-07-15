package lsy.project.socket.web.controller;

import com.lsy.base.result.ResultVo;
import lsy.project.socket.api.entity.Protocol;
import lsy.project.socket.api.service.IProtocolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
@RestController
@RequestMapping("/edenep/lsytest")
public class QueryTestController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private IProtocolService protocolService;

    /**
     * 校验手机验证码
     */
    @PostMapping(value = "/test")
    public ResultVo test(@RequestParam Map param) throws Exception {
        ResultVo resultVo = new ResultVo();
        logger.info("李松岳测试接口-start");
        Protocol protocol = new Protocol();
        protocol.setActiveStatus("1");
        protocol.setAnalysisClazz("1");
        protocol.setAnalysisType("1");
        protocol.setCallBackClazz("1");
        protocol.setIsForward("1");
        protocol.setProtocolName("1");
        protocol.setMid("1");
        protocol.setTypeId(1);
        protocolService.insert(protocol);
        return resultVo;
    }
}
