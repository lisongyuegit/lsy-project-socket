package com.yidcloud.web.handler;

import com.lsy.base.result.ResultVo;
import com.lsy.base.string.StringHelper;
import com.yidcloud.api.contants.CollectContants;
import com.yidcloud.api.entity.Protocol;
import com.yidcloud.api.entity.ProtocolAnalysis;
import com.yidcloud.web.convert.DefaultProtocolConvert;
import com.yidcloud.web.model.ReceiveMessage;
import com.yidcloud.web.util.CollectUtil;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * 智能解码类,
 *
 * @author 胡洪瑜 huhongyu@edenep.net
 * @version 2.0
 * @company 易登科技
 * @since 2017/11/10 9:12
 */
public class DefaultMessageAnalysisHandler extends AbstractMessageAnalysisHandler {

    @Override
    ResultVo invoke(ReceiveMessage receiveMessage, Protocol protocol, List<ProtocolAnalysis> analyses, Map<String,String > queueParamsMap){

        ResultVo vo = new ResultVo();
        byte [] msgBody = DefaultProtocolConvert.hexstring2bytes(queueParamsMap.get("sourceHexstring"));
        for (ProtocolAnalysis analysis:analyses) {
            //字段名称
            String fieldName = analysis.getFieldJavaCode();
            try {
                //获取字段的值
                String clazzAndMethodPath = analysis.getAnalysisJavaMethod();
                String clazzPath= CollectUtil.getClazzPath(clazzAndMethodPath);
                String methodPath = CollectUtil.getMethodPath(clazzAndMethodPath);
                Object o = Class.forName(clazzPath).newInstance();
                Method method = Class.forName(clazzPath).getDeclaredMethod(methodPath, byte[].class);
                //?如果是分隔符要特殊处理
                if(StringHelper.isBlank(receiveMessage.getSplitStr())||"null".equals(receiveMessage.getSplitStr())){
                    byte [] field;
                    if(CollectContants.PROTOCOL_ANALYSIS_ORDER_TYPE_0.equals(analysis.getOrderType())){
                        //特殊情况 处理
                        if(analysis.getFieldStartPosition().toString().equalsIgnoreCase("0") &&
                        analysis.getFieldEndPosition().toString().equalsIgnoreCase("0")){
                            field = msgBody;
                        }else{
                            field= DefaultProtocolConvert.getChildArray(msgBody,analysis.getFieldStartPosition(),analysis.getFieldSize());
                        }
                    }else{
                        //倒序获取
                        field = DefaultProtocolConvert.getChildArrayByEnd(msgBody,analysis.getFieldEndPosition(),analysis.getFieldSize());
                    }
                    queueParamsMap.put(fieldName,String.valueOf(method.invoke(o,field)));
                }else{
                    //获取转码之后的消息
                    String bodyString = queueParamsMap.get("sourceHexstring");

                    if(analysis.getFieldPosition()==-1){
                        queueParamsMap.put(fieldName,String.valueOf(method.invoke(o, DefaultProtocolConvert.hexstring2bytes(bodyString))));
                        continue;
                    }

                    //获取转码之后的消息体，并按照分隔符转成数组
                    String [] msgStringBody= (bodyString.substring(bodyString.indexOf(receiveMessage.getHeadTag())+receiveMessage.getHeadTag().length(),bodyString.lastIndexOf(receiveMessage.getEndTag()))).split(receiveMessage.getSplitStr());
                    String fieldValueString = msgStringBody[analysis.getFieldPosition()];
                    byte [] fieldValueBody = DefaultProtocolConvert.hexstring2bytes(fieldValueString);
                    queueParamsMap.put(fieldName,String.valueOf(method.invoke(o,fieldValueBody)));
                }
            }catch (Exception e){
                logger.error(fieldName+"解析失败！",e);
                 vo.setError_no(-1);
                break;
            }

        }
        vo.setResult(queueParamsMap);
        return vo;
    }



}
