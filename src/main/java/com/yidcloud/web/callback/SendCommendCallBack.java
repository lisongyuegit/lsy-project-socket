package com.yidcloud.web.callback;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lsy.base.date.DateHelper;
import com.lsy.base.string.StringHelper;
import com.yidcloud.api.dto.UpgradeDto;
import com.yidcloud.web.util.WbBoxUpgradeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.yidcloud.web.cache.CollectRedisCacheService;
import com.yidcloud.web.convert.DefaultProtocolConvert;
import com.yidcloud.web.model.CallbackMessage;
import com.yidcloud.web.model.ReceiveMessage;
import com.yidcloud.web.util.EdenepBoxUpgradeUtil;


/**
 * 平台通用下发命定 回送到终端
 * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
 * @copyright: Copyright (c) 2018
 * @company: 易登科技
 * @author: zl
 * @version: 2.0
 * @date: 2018年4月27日 11:31:07
 */
public class SendCommendCallBack extends AbstractCallback {

    private static Logger logger = LoggerFactory.getLogger(SendCommendCallBack.class);

    @Override
    CallbackMessage createCallBackMsg(ReceiveMessage msg) throws Exception {

        //判断是否需要回写
        CallbackMessage callbackMessage = new CallbackMessage();
        //直接取协议协议头
        callbackMessage.setHeaderMsg(DefaultProtocolConvert.hexstring2bytes(msg.getHeadTag()));
        //直接取协议协议尾
        callbackMessage.setFooterMsg(DefaultProtocolConvert.hexstring2bytes(msg.getEndTag()));

        //拼接body
        String bodystr = "";
        switch (msg.getHeadTag().toLowerCase()) {
            case "7b"://卡片机
                bodystr = getBodyStr(msg);
                if(!StringHelper.isBlank(bodystr)) {
                    callbackMessage.setBodyMsg(DefaultProtocolConvert.hexstring2bytes(
                            DefaultProtocolConvert.stringToASCII(bodystr, bodystr.length(), "")));
                    return callbackMessage;
                }
                break;
            case "5b"://智能盒子
                bodystr = getSmartBoxStr(msg);
                if(!StringHelper.isBlank(bodystr)) {
                    logger.info("盒子指令回写hex字符："+bodystr);
                    callbackMessage.setBodyMsg(DefaultProtocolConvert.hexstring2bytes(bodystr));
                    return callbackMessage;
                }
                break;
            case "28"://OBD智能盒子
                bodystr = getOBDBoxStr(msg);
                if(!StringHelper.isBlank(bodystr)) {
                    logger.info("OBD盒子指令回写hex字符："+bodystr);
                    callbackMessage.setBodyMsg(DefaultProtocolConvert.hexstring2bytes(bodystr));
                    return callbackMessage;
                }
                break;
            case "fe"://易登智能盒子
            case "fc"://易登垃圾桶
            case "fb"://易登垃圾桶
            case "fa"://GPS定位盒子
            case "2323"://易登智能盒子
                callbackMessage = getEdenepSmartBoxStr(msg);
                if(null != callbackMessage) {
                    logger.info("[易登智能盒子,垃圾桶]指令回写hex字符："+DefaultProtocolConvert.bytes2hexstring(callbackMessage.getHeaderMsg())
                            +DefaultProtocolConvert.bytes2hexstring(callbackMessage.getBodyMsg())
                            +DefaultProtocolConvert.bytes2hexstring(callbackMessage.getFooterMsg()));
                    return callbackMessage;
                }
                break;
            case "7e"://博时杰
                callbackMessage = getBsjBoxStr(msg);
                if(null != callbackMessage) {
                    logger.info("[博时杰]修改IP指令回写hex字符："+DefaultProtocolConvert.bytes2hexstring(callbackMessage.getHeaderMsg())
                            +DefaultProtocolConvert.bytes2hexstring(callbackMessage.getBodyMsg())
                            +DefaultProtocolConvert.bytes2hexstring(callbackMessage.getFooterMsg()));
                    return callbackMessage;
                }
                break;
            default:
                break;
        }

        return null;
    }

    /**
     * 获取下发指令消息体
     * @author:
     * @version: 2.0
     * @date: 2018 2018年4月27日 下午8:11:26
     * @param msg
     * @return
     */
    private String getBodyStr(ReceiveMessage msg) {

        String result = "";

        //将得到的原始字节转化为字符串
        byte[] allInfoByteArray = DefaultProtocolConvert.hexstring2bytes(msg.getMsgByte()); // 根据hex字符串得到字节数组
        String info = DefaultProtocolConvert.bytes2string(allInfoByteArray);
        String seq = info.split(",")[2];
        String imei = msg.getImei();

        //获取IMEI数据缓存---取下发指令
        Map<String,String> imeicache = CollectRedisCacheService.getImeiCache(imei);
        if(imeicache.isEmpty()) {
            return null;
        }
        boolean isProcess = imeicache.get("isProcess").toUpperCase().equals("FALSE")?false:true;

        if(msg.getMid().toLowerCase().equals("login")) {//登录指令需做特殊处理
            try {
                result = againSendCommendStr(imeicache, info, imei,seq,isProcess);
                //更改IMEI缓存信息
                CollectRedisCacheService.updateImeiCache(imei, imeicache);
            } catch (Exception e) {
                result = null;
                logger.error(e.getMessage());
            }
        }else if(!isProcess) {//非登录指令  且 指令未下发的情况下
            String paramVal = (String) imeicache.get("paramVal");//指令值

            result = String.format("%s,TEXT,%s,%s", imei,seq,paramVal);

            //判断是否透传指令
            if(paramVal.contains("TRANS")) {
                paramVal = String.format(paramVal, seq);
                result = String.format("%s,%s", imei,paramVal);
            }
            logger.debug("卡片机下发指令："+result);
            //TODO: 回写缓存数据 将下发状态改为true
            imeicache.put("isProcess", "true");
            CollectRedisCacheService.updateImeiCache(imei, imeicache);
        }
        return result;
    }

    /**
     * 已知条件（升级指令已下发的情况下）。
     * 卡片机登录协议---判断是否需要再次下发升级指令  满足以下条件：
     * 1：当前IMEI缓存中指令为 gprs或rfid升级指令
     * 2：当前终端上传的版本号<服务器保存的版本号
     * @author: zl
     * @version: 2.0
     * @date: 2018 2018年4月27日 下午8:16:19
     * @param imeicache IMEI缓存指令
     * @param info 上传信息（已转成字符串）
     * @param imei 设备号
     * @return
     * @throws Exception
     */
    private String againSendCommendStr(Map imeicache, String info,String imei,String seq,boolean isProcess) throws Exception {

        String paramVal = (String) imeicache.get("paramVal");//指令值
        String server_gprs_version = (String) imeicache.get("serverGpsVersion");
        String server_rdid_version = (String) imeicache.get("serverRfidVersion");
        Integer gprsVersionErrorNum = Integer.parseInt(imeicache.get("GpsVersionErrorNum")+"");//gprs 升级错误次数
        Integer rfidVersionErrorNum = Integer.parseInt(imeicache.get("RfidVersionErrorNum")+"");//rfid 升级错误次数

        String up_gprs_version = info.split(",").length>=9?info.split(",")[4]:"";
        String up_rfid_version = info.split(",").length>=9?info.split(",")[5]:"";

        //判断升级指令是gprs升级
        if(paramVal.contains("*MATUPD7538690")) {

            //终端登录接口上传的gprs版本号<服务器保存的gprs版本号
            if(!judgeGprsVersion(up_gprs_version,imei,server_gprs_version)) {
                return null;
            }
            if(gprsVersionErrorNum<10) {
                logger.info("登录指令LOGIN，平台第"+(gprsVersionErrorNum+1)+"下发升级命令: " + paramVal);
                imeicache.put("GpsVersionErrorNum", ""+(gprsVersionErrorNum+1));
                return String.format("%s,TEXT,%s,%s", imei,seq,paramVal);
            }
            logger.info("终端："+imei+" 已达到平台设置的最大错误升级次数"+gprsVersionErrorNum+"，不予下发升级指令 并且情况缓存指令值");
            imeicache.put("paramVal", "");
        }
        //判断升级指令是rfid升级
        else if(paramVal.contains("*MATUPD7538691")) {

            //终端登录接口上传的rfid版本号<服务器保存的rfid版本号
            if(!judgeRfidVersion(up_rfid_version,imei,server_rdid_version)) {
                return null;
            }

            //当前下发错误次数少于指令的次数
            if(rfidVersionErrorNum<10) {
                logger.info("登录指令LOGIN， 平台第"+(rfidVersionErrorNum+1)+"下发升级命令: " + paramVal);
                imeicache.put("RfidVersionErrorNum", ""+(rfidVersionErrorNum+1));
                return String.format("%s,TEXT,%s,%s", imei,seq,paramVal);
            }
            logger.info("终端："+imei+" 已达到平台设置的最大错误升级次数"+rfidVersionErrorNum+"，不予下发升级指令 并且情况缓存指令值");
            imeicache.put("paramVal", "");
        } else {

            if(!isProcess) {
                imeicache.put("isProcess", "true");
                return String.format("%s,TEXT,%s,%s", imei,seq,paramVal);
            }
        }

        return null;
    }

    /**
     * 比对终端当前gprs版本 与 服务器gprs版本
     * 当前终端版本 < 服务器版本  则为可升级条件
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018 2018年4月27日 下午6:24:32
     * @param curpgrs_version
     * @param temid
     * @param server_pgrs_version
     * @return
     */
    private boolean judgeGprsVersion(String curpgrs_version,String temid,String server_pgrs_version){
        if(null!=curpgrs_version && curpgrs_version.compareTo(server_pgrs_version)<0){
            logger.info(String.format("IMEI:%s 需要下发gprs升级指令,当前终端版本%s,服务器版本%s",temid,curpgrs_version,server_pgrs_version));
            return true;
        }
        return false;
    }

    /**
     * 比对终端当前rfid版本 与 服务器rfid版本
     * 当前终端版本 < 服务器版本  则为可升级条件
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018 2018年4月27日 下午6:25:47
     * @param currfid_version
     * @param temid
     * @return
     */
    private boolean judgeRfidVersion(String currfid_version,String temid,String server_rfid_version){
        if(null!=currfid_version && currfid_version.compareTo(server_rfid_version)<0){
            logger.info(String.format("IMEI:%s 需要下发rfid升级指令,当前终端版本%s,服务器版本%s",temid,currfid_version,server_rfid_version));
            return true;
        }
        return false;
    }

    /**
     * 获取智能盒子下发指令消息体
     * @author: zl
     * @version: 2.0
     * @date: 2018 2018年5月3日 下午2:22:07
     * @param msg
     * @return
     */
    private String getSmartBoxStr(ReceiveMessage msg) {

        String result = "";

        String imei = msg.getImei();
        //获取IMEI数据缓存---取下发指令
        Map<String, String> imeicache = CollectRedisCacheService.getImeiCache(imei);
        if(imeicache.isEmpty()) {
            return null;
        }
        boolean isProcess = imeicache.get("isProcess").toUpperCase().equals("FALSE")?false:true;
        if(!isProcess) {//非登录指令  且 指令未下发的情况下
            result = getSmartBoxHexResult(imei, imeicache.get("paramVal"));
            logger.debug("智能盒子下发指令："+result);
            boolean isSoftUpgrade = imeicache.get("isSoftUpgrade").equalsIgnoreCase("YES")?true:false;
            if(isSoftUpgrade) {
                imeicache.put("isSoftUpgrade", "no");
                //设置缓存key
                Map<String, String> imeiTastMap = new HashMap<String, String>();
                imeiTastMap.put("paramId", imeicache.get("paramId"));//升级指定
                imeiTastMap.put("create_time", DateHelper.formatDate(new Date(), DateHelper.PATTERN_TIME));
                imeiTastMap.put("status", "0");//状态  0 未开始 1执行中 2已结束
                imeiTastMap.put("process_count", "0");//执行次数
                imeiTastMap.put("imei", imei);//设备唯一码
                imeiTastMap.put("taskid", imeicache.get("taskid")==null?"":imeicache.get("taskid"));
                Calendar nowTime = Calendar.getInstance();
                nowTime.add(Calendar.MINUTE, 5);
                imeiTastMap.put("next_processtime", DateHelper.formatDate(nowTime.getTime(), DateHelper.PATTERN_TIME));//下次可执行时间
                imeiTastMap.put("getVersionParam", imeicache.get("getVersionParam")==null?"":imeicache.get("getVersionParam"));
                imeiTastMap.put("softversion", imeicache.get("softversion")==null?"":imeicache.get("softversion"));
                CollectRedisCacheService.addImeiTast(imei, imeiTastMap);
            }

            //TODO: 回写缓存数据 将下发状态改为true
            imeicache.put("isProcess", "true");
            CollectRedisCacheService.updateImeiCache(imei, imeicache);

            return result;
        }

        //没有指定下发的情况下-查询是否需要进行查询版本指定的下发
        Map<String, String> imeiTastMap = CollectRedisCacheService.getImeiTast(imei);
        if(null!=imeiTastMap && !imeiTastMap.isEmpty()) {
            result = sendQueryVersionCommand(imeiTastMap, imei);
        }
        return result;
    }

    /**
     * 查询构建需要下发查询版本指令，更改设备任务缓存imeiTastMap
     * @author:
     * @version: 2.0
     * @date: 2018年6月28日 下午5:43:37
     * @param imeiTastMap 设备任务缓存map对象
     * @param imei 设备号
     * @return
     */
    private String sendQueryVersionCommand(Map<String,String> imeiTastMap, String imei) {

        String result = null;
        //当前时间
        Date now = new Date();
        int status = imeiTastMap.get("status")==null?0:Integer.parseInt(imeiTastMap.get("status"));

        //判断任务的可执行开始时间>当前系统时间 则开始下发获取版本指令
        long nextProcessTime = imeiTastMap.get("next_processtime")==null?now.getTime():DateHelper.parseString(imeiTastMap.get("next_processtime")).getTime();
        long currenttime = now.getTime();
        if(currenttime>=nextProcessTime && (status==1||status==0)) {
            result = getSmartBoxHexResult(imei, imeiTastMap.get("getVersionParam"));//获取版本命定
            imeiTastMap.put("status", "0");//状态  0 未开始 1执行中 2已结束
            int process_count = imeiTastMap.get("process_count")==null ?0 :(Integer.parseInt(imeiTastMap.get("process_count"))+1);
            imeiTastMap.put("process_count", process_count+"");
            imeiTastMap.put("processtime", DateHelper.formatDate(now, DateHelper.PATTERN_TIME));//执行时间
            Calendar nowTime = Calendar.getInstance();
            nowTime.add(Calendar.MINUTE, 5);
            imeiTastMap.put("next_processtime", DateHelper.formatDate(nowTime.getTime(), DateHelper.PATTERN_TIME));//下次可执行时间
            CollectRedisCacheService.addImeiTast(imei, imeiTastMap);
        }
        return result;
    }

    /**
     * 盒子（将要下发的命定转为hex字符）
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018年6月25日 下午5:41:58
     * @param imei
     * @param paramVal
     * @return
     */
    private String getSmartBoxHexResult(String imei,String paramVal) {
        byte[] c1b= new byte[] {(byte) 0x08};//命令字1
        byte[] c2b= new byte[] {(byte) 0x00};//命令字2
        byte[] imeibyte = DefaultProtocolConvert.hexstring2bytes(imei);
        byte[] commendbyte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.stringToASCII(paramVal, paramVal.length(), ""));//命定

        byte[] testYH = DefaultProtocolConvert.add(DefaultProtocolConvert.add(
                DefaultProtocolConvert.add(c1b, c2b),imeibyte),commendbyte);

        byte[] seq = DefaultProtocolConvert.hexstring2bytes("15");//seq
        int xor = DefaultProtocolConvert.xor(testYH);//异或值
        return DefaultProtocolConvert.bytes2hexstring(c1b)
                + DefaultProtocolConvert.bytes2hexstring(c2b)
                + DefaultProtocolConvert.bytes2hexstring(imeibyte)
                + DefaultProtocolConvert.bytes2hexstring(commendbyte)
                + DefaultProtocolConvert.int2hexstring(xor)
                + DefaultProtocolConvert.bytes2hexstring(seq);
    }

    /**
     * 获取易登盒子下发指令
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018年7月13日 下午3:19:25
     * @param msg
     * @return
     * @throws Exception
     */
    private CallbackMessage getEdenepSmartBoxStr(ReceiveMessage msg) throws Exception {

        CallbackMessage result = null;

        String imei = msg.getImei();
        //获取IMEI数据缓存---取下发指令
        Map<String, String> imeicache = CollectRedisCacheService.getImeiCache(imei);
        if(imeicache.isEmpty()) {
            return null;
        }
        //判断指令是否已下载
        boolean isProcess = imeicache.get("isProcess").toUpperCase().equals("FALSE")?false:true;
        if(isProcess) {//
            return null;
        }

        String paramVal = imeicache.get("paramVal");
        //升级指定 处理
        boolean isSoftUpgrade = imeicache.get("isSoftUpgrade").equalsIgnoreCase("YES")?true:false;
        if(isSoftUpgrade) {
            imeicache.put("isSoftUpgrade", "no");
            //设置缓存  计划升级任务回写
            Map<String, String> imeiTastMap = new HashMap<String, String>();
            //升级指令
            imeiTastMap.put("paramId", imeicache.get("paramId"));
            imeiTastMap.put("create_time", DateHelper.formatDate(new Date(), DateHelper.PATTERN_TIME));
            //状态  0 未开始 1执行中 2已结束
            imeiTastMap.put("status", "1");
            //设备唯一码
            imeiTastMap.put("imei", imei);
            imeiTastMap.put("taskid", imeicache.get("taskid")==null?"":imeicache.get("taskid"));
            imeiTastMap.put("softversion", imeicache.get("softversion")==null?"":imeicache.get("softversion"));
            CollectRedisCacheService.addImeiTast(imei, imeiTastMap);

            Map<String,Object> paramMap = JSON.parseObject(paramVal, Map.class);

            String cmd = paramMap.get("cmd")==null?"":paramMap.get("cmd").toString();

            String lastPackageCmd = paramMap.get("lastPackageCmd")==null?"":paramMap.get("lastPackageCmd").toString();
            String startTag = paramMap.get("startTag")==null?"":paramMap.get("startTag").toString();
            String endTag = paramMap.get("endTag")==null?"":paramMap.get("endTag").toString();
            String filePath = paramMap.get("filePath")==null?"":paramMap.get("filePath").toString();
            //IMEI 0 智能盒子,1 垃圾桶
            String itype = paramMap.get("itype")==null?"":paramMap.get("itype").toString();

            //用于控制采集客户端分包下载
            UpgradeDto upgradeDto = new UpgradeDto();
            upgradeDto.setTaskid(imeicache.get("taskid")==null?"":imeicache.get("taskid"));
            //下载路径
            upgradeDto.setFilePath(filePath);
            upgradeDto.setImei(imei);
            //正在下载
            upgradeDto.setStatus("1");
            upgradeDto.setDownStartTime(DateHelper.formatDate(new Date(),DateHelper.PATTERN_TIME));
            upgradeDto.setSendCmd(cmd);
            upgradeDto.setSendLastPackageCmd(lastPackageCmd);
            upgradeDto.setSendStartTag(startTag);
            upgradeDto.setSendEndTag(endTag);
            upgradeDto.setImeiType(itype);
            upgradeDto.setCurrentDown("1");
            if("8C".equals(cmd)){
                upgradeDto.setCurrentDown("0");
            }
            CollectRedisCacheService.addServerPushUpgrade(upgradeDto);
            //下发升级包
            if("8C".equals(cmd)){
                WbBoxUpgradeUtil.downPackage(upgradeDto.getFilePath(), imei);
            } else {
                EdenepBoxUpgradeUtil.downPackage(upgradeDto.getFilePath(), imei);
            }
        }else {
            result = getEdenepBoxHexResult(imei, paramVal);
        }

        //回写缓存数据 将下发状态改为true
        imeicache.put("isProcess", "true");
        CollectRedisCacheService.updateImeiCache(imei, imeicache);

        return result;
    }

    /**
     * 拼装易登盒子指令下发数据
     * @author:
     * @version: 2.0
     * @date: 2018年8月6日 上午11:13:09
     * @param imei
     * @param paramVal
     * @return
     */
    private CallbackMessage getEdenepBoxHexResult(String imei,String paramVal) {

        if(StringHelper.isBlank(paramVal)) {
            return null;
        }

        @SuppressWarnings("unchecked")
        Map<String,Object> paramMap = JSON.parseObject(paramVal, Map.class);

        String startTag = paramMap.get("startTag")==null?"":paramMap.get("startTag").toString();
        String endTag = paramMap.get("endTag")==null?"":paramMap.get("endTag").toString();

        CallbackMessage result;
        if("2323".equals(startTag) && "0000".equals(endTag)){
            result = createWBCallBackResult(imei, paramMap, startTag);
        }else {
            result = createEdenepBoxCallBackResult(imei, paramMap, startTag, endTag);
        }
        return result;
    }

    private CallbackMessage createEdenepBoxCallBackResult(String imei, Map<String, Object> paramMap, String startTag, String endTag) {

        CallbackMessage result = new CallbackMessage();
        //数据长度
        int dataLength = 0;
        //下发数据
        byte[] dataByte = new byte[0];

        try {
            byte[] headByte = DefaultProtocolConvert.hexstring2bytes(startTag);

            Integer cmd = Integer.parseInt(paramMap.get("cmd").toString());
            byte[] cmdByte = new byte[] {(byte) DefaultProtocolConvert.hexstring2int(cmd.toString()).intValue()};

            //IMEI 0 智能盒子,1 垃圾桶
            String itype = paramMap.get("itype")==null?"":paramMap.get("itype").toString();
            byte[] imeibyte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.stringToASCII(imei, imei.length(), ""));
            if("1".equals(itype)){
                imeibyte = DefaultProtocolConvert.hexstring2bytes(imei);
            }
            //数据项固定长度
            Integer fixlen = paramMap.get("fixlen")==null?null:Integer.parseInt(paramMap.get("fixlen").toString());

            byte[] footerByte = DefaultProtocolConvert.hexstring2bytes(endTag);
            //将data数据转为list集合，并且跟进sort排序
            @SuppressWarnings("unchecked")
            List<Map> list = JSON.parseObject(paramMap.get("data").toString(), List.class);
            list.sort((h1, h2) -> (h1.get("sort")==null?"0":h1.get("sort").toString()).compareTo(h2.get("sort")==null?"0":h2.get("sort").toString()));

            for (Map map : list) {
                //属性名称
                String name = map.get("name").toString();
                //属性长度
                Integer itemlen = map.get("len")==null?0:Integer.parseInt(map.get("len").toString());
                byte [] itemByte = null;
                //数据
                if(null == itemlen || itemlen==0) {
                    itemByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.stringToASCII(name, name.length(), ""));
                }else {
                    itemByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(Integer.parseInt(name)),2*itemlen));
                }
                dataByte = DefaultProtocolConvert.add(dataByte, itemByte);
                dataLength += itemlen;
            }

            byte[] fixlenByte = dataByte;
            if(null != fixlen) {
                //数据固定长度
                dataLength = fixlen;

                fixlenByte = new byte[dataLength];
                /**
                 * 补零操作
                 */
                if(dataByte.length<dataLength) {
                    byte [] lastPackageByte = new byte[dataLength-dataByte.length];
                    for(int i=0;i<dataLength-dataByte.length;i++) {
                        lastPackageByte[i]=0x00;
                    }
                    fixlenByte = DefaultProtocolConvert.add(dataByte, lastPackageByte);
                }
            }

            //5=帧头+帧长+指令码+校验+帧尾 //数据长度
            byte[] protocolLength_byte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(dataByte.length+imeibyte.length+5),2));

            //从帧长到数据内容
            byte[] totalDataByte = DefaultProtocolConvert.add(
                    DefaultProtocolConvert.add(DefaultProtocolConvert
                            .add(protocolLength_byte, cmdByte), imeibyte),fixlenByte);

            //协议校验码
            byte[] protocolVerify_byte = new byte[] {(byte) DefaultProtocolConvert.byteSum(totalDataByte)};

            //协议头
            result.setHeaderMsg(headByte);
            result.setBodyMsg(DefaultProtocolConvert.add(totalDataByte,protocolVerify_byte));
            //协议尾
            result.setFooterMsg(footerByte);
        } catch (Exception e) {
            logger.error("下发数据异常："+e.getMessage());
            return null;
        }
        return result;
    }

    private CallbackMessage createWBCallBackResult(String imei, Map<String,Object> paramMap, String startTag) {

        byte[] headByte = DefaultProtocolConvert.hexstring2bytes(startTag);

        byte[] ydCmdByte = new byte[] {(byte) DefaultProtocolConvert.hexstring2int("FE").intValue()};

        byte[] imeibyte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.stringToASCII(imei, imei.length(), ""));

        byte[] encryptionByte = new byte[] {(byte) DefaultProtocolConvert.hexstring2int("01").intValue()};

        //数据长度
        byte[] dataContentLengthByte = null;
        //协议包数据内容
        byte[] dataByte = new byte[0];

        String cmd = paramMap.get("cmd")+"";
        byte[] cmdByte = new byte[] {(byte) DefaultProtocolConvert.hexstring2int(cmd).intValue()};
        if("88".equals(cmd)){
            dataContentLengthByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(6),4));
            //将data数据转为list集合，并且跟进sort排序
            @SuppressWarnings("unchecked")
            List<Map> list = JSON.parseObject(paramMap.get("data").toString(), List.class);
            list.sort((h1, h2) -> (h1.get("sort")==null?"0":h1.get("sort").toString()).compareTo(h2.get("sort")==null?"0":h2.get("sort").toString()));
            for (Map map : list) {
                //属性名称
                String name = map.get("name").toString();
                //属性长度
                Integer itemlen = map.get("len")==null?0:Integer.parseInt(map.get("len").toString());
                byte [] itemByte;
                if(null == itemlen || itemlen==0) {
                    //数据
                    itemByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.stringToASCII(name, name.length(), ""));
                }else {
                    itemByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(Integer.parseInt(name)),2*itemlen));
                }
                dataByte = DefaultProtocolConvert.add(dataByte, itemByte);
            }
        }else if("89".equals(cmd)){

            dataContentLengthByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(6),4));
            String name = paramMap.get("data").toString();
            dataByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.stringToASCII(name, name.length(), ""));
        }else if("8A".equals(cmd) || "8E".equals(cmd)){

            dataContentLengthByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(0),4));
        }else if("8C".equals(cmd)){

            dataContentLengthByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(0),4));
        } else if("8B".equals(cmd)){
            dataContentLengthByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(4),4));
            Integer first = Integer.parseInt(paramMap.get("first").toString());
            Integer second = Integer.parseInt(paramMap.get("second").toString());
            Integer third = Integer.parseInt(paramMap.get("third").toString());
            Integer four = Integer.parseInt(paramMap.get("four").toString());
            byte [] firstByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(first),2));
            byte [] secondByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(second),2));
            byte [] thirdByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(third),2));
            byte [] fourByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(four),2));
            dataByte = DefaultProtocolConvert.add(DefaultProtocolConvert.add(DefaultProtocolConvert.add(firstByte, secondByte), thirdByte), fourByte);
        }

        //从帧长到数据内容
        byte[] totalDataByte = DefaultProtocolConvert.add(DefaultProtocolConvert.add(DefaultProtocolConvert.add(
                DefaultProtocolConvert.add(DefaultProtocolConvert
                        .add(cmdByte, ydCmdByte), imeibyte),encryptionByte),dataContentLengthByte),dataByte);

        //协议校验码
        byte[] yhbyte = new byte[] {(byte) DefaultProtocolConvert.byteSum(totalDataByte)};

        CallbackMessage result = new CallbackMessage();
        //协议头
        result.setHeaderMsg(headByte);
        result.setBodyMsg(totalDataByte);
        result.setFooterMsg(yhbyte);
        return result;
    }

    private String getOBDBoxStr(ReceiveMessage msg) {

        String imei = msg.getImei();
        //获取IMEI数据缓存---取下发指令
        Map<String, String> imeicache = CollectRedisCacheService.getImeiCache(imei);
        if(imeicache.isEmpty()) {
            return null;
        }
        boolean isProcess = imeicache.get("isProcess").toUpperCase().equals("FALSE")?false:true;
        if(!isProcess) {//非登录指令  且 指令未下发的情况下
            String result = getOBDBoxHexResult(imei, imeicache.get("paramVal"));
            logger.debug("OBD智能盒子下发指令："+result);
            //TODO: 回写缓存数据 将下发状态改为true
            imeicache.put("isProcess", "true");
            CollectRedisCacheService.updateImeiCache(imei, imeicache);
            return result;
        }
        return null;
    }

    private String getOBDBoxHexResult(String imei,String paramVal) {
        byte[] startByte= new byte[] {(byte) 0x28};//协议头
        byte[] imeibyte = DefaultProtocolConvert.hexstring2bytes(imei);
        byte[] commendbyte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.stringToASCII(paramVal, paramVal.length(), ""));//命定
        byte[] protocolLength_byte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(commendbyte.length+1),4));//数据长度
        byte[] c2b= new byte[] {(byte) 0x10};//命令字2
        byte[] c3b= new byte[] {(byte) 0x01};//命令字2

        byte[] setByte= new byte[] {(byte) 0x01};//命令字2
        byte[] testYH = DefaultProtocolConvert.add(
                DefaultProtocolConvert.add(
                        DefaultProtocolConvert.add(
                                DefaultProtocolConvert.add(
                                        DefaultProtocolConvert.add(imeibyte, c2b),
                                        c3b),
                                protocolLength_byte),
                        setByte),
                commendbyte);
        byte[] endByte= new byte[] {(byte) 0x29};//协议尾
        int xor = DefaultProtocolConvert.xor(testYH);//异或值
        return DefaultProtocolConvert.bytes2hexstring(startByte)
                + DefaultProtocolConvert.bytes2hexstring(imeibyte)
                + DefaultProtocolConvert.bytes2hexstring(c2b)
                + DefaultProtocolConvert.bytes2hexstring(c3b)
                + DefaultProtocolConvert.bytes2hexstring(protocolLength_byte)
                + DefaultProtocolConvert.bytes2hexstring(setByte)
                + DefaultProtocolConvert.bytes2hexstring(commendbyte)
                + DefaultProtocolConvert.int2hexstring(xor)
                + DefaultProtocolConvert.bytes2hexstring(endByte);
    }

    private CallbackMessage getBsjBoxStr(ReceiveMessage msg) {

        CallbackMessage result = new CallbackMessage();

        String imei = msg.getImei();
        //获取IMEI数据缓存---取下发指令
        Map<String, String> imeicache = CollectRedisCacheService.getImeiCache(imei);
        if(imeicache.isEmpty()) {
            return null;
        }
        boolean isProcess = imeicache.get("isProcess").toUpperCase().equals("FALSE")?false:true;
        if(!isProcess) {//指令未下发的情况下

            result = getBsjBoxHexResult(imei, imeicache.get("paramVal"));

            //TODO: 回写缓存数据 将下发状态改为true
            imeicache.put("isProcess", "true");
            CollectRedisCacheService.updateImeiCache(imei, imeicache);
            return result;
        }
        return null;
    }

    private CallbackMessage getBsjBoxHexResult(String imei,String paramVal) {

        if(StringHelper.isBlank(paramVal)) {
            return null;
        }
        CallbackMessage result = new CallbackMessage();

        try {
            @SuppressWarnings("unchecked")
            Map<String,Object> paramMap = JSON.parseObject(paramVal, Map.class);

            String startTag = paramMap.get("startTag")==null?"":paramMap.get("startTag").toString();
            String endTag = paramMap.get("endTag")==null?"":paramMap.get("endTag").toString();

            byte[] headByte = DefaultProtocolConvert.hexstring2bytes(startTag);
            byte[] footerByte = DefaultProtocolConvert.hexstring2bytes(endTag);

            //将data数据转为list集合，并且跟进sort排序
            @SuppressWarnings("unchecked")
            List<Map> list = JSON.parseObject(paramMap.get("data").toString(), List.class);
            list.sort((h1, h2) -> (h1.get("sort")==null?"0":h1.get("sort").toString()).compareTo(h2.get("sort")==null?"0":h2.get("sort").toString()));

            String ip = "";
            Integer port = 0;
            for (Map map : list) {
                String name = map.get("name").toString();//属性名称
                String value = map.get("value").toString();//属性值
                if(name.equalsIgnoreCase("IP")){
                    ip = value;
                }
                if(name.equalsIgnoreCase("PORT")){
                    port = Integer.parseInt(value);
                }
            }

            byte[] bodyLengthByte = DefaultProtocolConvert.hexstring2bytes("02");
            byte [] urlCmdByte = DefaultProtocolConvert.hexstring2bytes("00000013");
            byte [] urlByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.string2hexstring(ip));
            byte [] urlLengthByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.int2hexstring(urlByte.length,2));
            byte [] portCmdByte = DefaultProtocolConvert.hexstring2bytes("00000018");
            byte [] portByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.int2hexstring(port,8));
            byte [] portLengthByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.int2hexstring(portByte.length,2));

            byte [] msgBodyByte = DefaultProtocolConvert.add(bodyLengthByte,
                    DefaultProtocolConvert.add(urlCmdByte,
                            DefaultProtocolConvert.add(urlLengthByte,DefaultProtocolConvert.add(urlByte,
                                    DefaultProtocolConvert.add(portCmdByte,DefaultProtocolConvert.add(portLengthByte,portByte))))));

            byte[] cmdByte = DefaultProtocolConvert.hexstring2bytes(paramMap.get("cmd").toString());
            byte[] imeiByte = DefaultProtocolConvert.hexstring2bytes(imei);
            byte [] msgbodyLengthByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.int2hexstring(msgBodyByte.length,4));
            byte[] msgSeqByte = DefaultProtocolConvert.hexstring2bytes("0000");

            byte [] msgByte = DefaultProtocolConvert.add(cmdByte,
                    DefaultProtocolConvert.add(msgbodyLengthByte ,
                            DefaultProtocolConvert.add(imeiByte,
                                    DefaultProtocolConvert.add(msgSeqByte,msgBodyByte))));

            //协议校验码
            int xor = DefaultProtocolConvert.xor(msgByte);
            byte[] xorByte =  DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.int2hexstring(xor,2));

            //从消息ID到校验码;
            byte[] msgBody = DefaultProtocolConvert.add(msgByte, xorByte);

            //协议头
            result.setHeaderMsg(headByte);
            result.setBodyMsg(msgBody);
            //协议尾
            result.setFooterMsg(footerByte);
        } catch (Exception e) {
            logger.error("下发数据异常："+e.getMessage());
            return null;
        }
        return result;
    }

    public static void main(String[] args) {

        int name = 5012;
        String hexServerUrl = "183.63.1.123";
        System.out.println(DefaultProtocolConvert.int2hexstring(name,8));
        System.out.println(DefaultProtocolConvert.string2hexstring(hexServerUrl));

        byte[] bodyLengthByte = DefaultProtocolConvert.hexstring2bytes("02");
        byte [] urlCmdByte = DefaultProtocolConvert.hexstring2bytes("00000013");
        byte [] urlByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.string2hexstring(hexServerUrl));
        byte [] urlLengthByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.int2hexstring(urlByte.length,2));
        byte [] portCmdByte = DefaultProtocolConvert.hexstring2bytes("00000018");
        byte [] portByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.int2hexstring(name,8));
        byte [] portLengthByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.int2hexstring(portByte.length,2));

        byte [] msgBodyByte = DefaultProtocolConvert.add(bodyLengthByte,
                DefaultProtocolConvert.add(urlCmdByte,
                DefaultProtocolConvert.add(urlLengthByte,DefaultProtocolConvert.add(urlByte,
                        DefaultProtocolConvert.add(portCmdByte,DefaultProtocolConvert.add(portLengthByte,portByte))))));

        byte[] cmdByte = DefaultProtocolConvert.hexstring2bytes("8103");
        byte[] imeiByte = DefaultProtocolConvert.hexstring2bytes("015069029770");
        byte [] msgbodyLengthByte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.int2hexstring(msgBodyByte.length,4));
        byte[] msgSeqByte = DefaultProtocolConvert.hexstring2bytes("0000");

        byte [] msgByte = DefaultProtocolConvert.add(cmdByte,
                DefaultProtocolConvert.add(msgbodyLengthByte ,
                        DefaultProtocolConvert.add(imeiByte,
                                DefaultProtocolConvert.add(msgSeqByte,msgBodyByte))));

        //协议校验码
        int xor = DefaultProtocolConvert.xor(msgByte);
        byte[] xorByte =  DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.int2hexstring(xor,2));

        //从消息ID到校验码;
        byte[] msgBody = DefaultProtocolConvert.add(msgByte, xorByte);

        System.out.println(DefaultProtocolConvert.bytes2hexstring(msgByte));
    }
}
