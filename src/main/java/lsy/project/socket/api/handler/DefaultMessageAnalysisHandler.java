package lsy.project.socket.api.handler;

import com.lsy.base.result.ResultVo;
import com.lsy.base.string.StringHelper;
import lsy.project.socket.api.contants.CollectContants;
import lsy.project.socket.api.convert.DefaultProtocolConvert;
import lsy.project.socket.api.entity.Protocol;
import lsy.project.socket.api.entity.ProtocolAnalysis;
import lsy.project.socket.api.model.ReceiveMessage;
import lsy.project.socket.api.util.CollectUtil;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;


/**
 * 智能解码类
 *
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public class DefaultMessageAnalysisHandler extends AbstractMessageAnalysisHandler {

    @Override
    ResultVo invoke(ReceiveMessage receiveMessage, Protocol protocol, List<ProtocolAnalysis> analyses, Map<String, String> queueParamsMap) {

        ResultVo vo = new ResultVo();
        byte[] msgBody = DefaultProtocolConvert.hexstring2bytes(queueParamsMap.get("sourceHexstring"));
        for (ProtocolAnalysis analysis : analyses) {
            //字段名称
            String fieldName = analysis.getFieldJavaCode();
            try {
                //获取字段的值
                String clazzAndMethodPath = analysis.getAnalysisJavaMethod();
                String clazzPath = CollectUtil.getClazzPath(clazzAndMethodPath);
                String methodPath = CollectUtil.getMethodPath(clazzAndMethodPath);
                Object o = Class.forName(clazzPath).newInstance();
                Method method = Class.forName(clazzPath).getDeclaredMethod(methodPath, byte[].class);
                //?如果是分隔符要特殊处理
                if (StringHelper.isBlank(receiveMessage.getSplitStr()) || "null".equals(receiveMessage.getSplitStr())) {
                    byte[] field;
                    if (CollectContants.PROTOCOL_ANALYSIS_ORDER_TYPE_0.equals(analysis.getOrderType())) {
                        //特殊情况 处理
                        if (analysis.getFieldStartPosition().toString().equalsIgnoreCase("0") &&
                                analysis.getFieldEndPosition().toString().equalsIgnoreCase("0")) {
                            field = msgBody;
                        } else {
                            field = DefaultProtocolConvert.getChildArray(msgBody, analysis.getFieldStartPosition(), analysis.getFieldSize());
                        }
                    } else {
                        //倒序获取
                        field = DefaultProtocolConvert.getChildArrayByEnd(msgBody, analysis.getFieldEndPosition(), analysis.getFieldSize());
                    }
                    queueParamsMap.put(fieldName, String.valueOf(method.invoke(o, field)));
                } else {
                    //获取转码之后的消息
                    String bodyString = queueParamsMap.get("sourceHexstring");

                    if (analysis.getFieldPosition() == -1) {
                        queueParamsMap.put(fieldName, String.valueOf(method.invoke(o, DefaultProtocolConvert.hexstring2bytes(bodyString))));
                        continue;
                    }

                    //获取转码之后的消息体，并按照分隔符转成数组
                    String[] msgStringBody = (bodyString.substring(bodyString.indexOf(receiveMessage.getHeadTag()) + receiveMessage.getHeadTag().length(), bodyString.lastIndexOf(receiveMessage.getEndTag()))).split(receiveMessage.getSplitStr());
                    String fieldValueString = msgStringBody[analysis.getFieldPosition()];
                    byte[] fieldValueBody = DefaultProtocolConvert.hexstring2bytes(fieldValueString);
                    queueParamsMap.put(fieldName, String.valueOf(method.invoke(o, fieldValueBody)));
                }
            } catch (Exception e) {
                logger.error(fieldName + "解析失败！", e);
                vo.setError_no(-1);
                break;
            }

        }
        vo.setResult(queueParamsMap);
        return vo;
    }


}
