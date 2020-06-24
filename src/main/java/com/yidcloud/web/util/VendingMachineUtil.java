package com.yidcloud.web.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lsy.base.result.ResultVo;
import com.lsy.base.string.StringHelper;
import com.lsy.base.utils.PropertiesHelper;
import com.yidcloud.web.cache.CollectRedisCacheService;
import com.yidcloud.web.convert.DefaultProtocolConvert;
import com.yidcloud.web.model.ReceiveMessage;
import okhttp3.FormBody;
import okhttp3.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.yidcloud.web.util.AESHelper.bytesToHexString;

/**
 * @author zl
 * @version 2.0
 * @description:
 * @copyright: Copyright (c) 2017
 */
public class VendingMachineUtil {

    static Logger logger = LoggerFactory.getLogger(VendingMachineUtil.class);

    public static String getRandomNumr(int length) {
        String val = new String();
        Random random = new Random();
        int i = 0;
        while (i < length) {
            val += String.valueOf(random.nextInt(10));
            i++;
        }
        return val;
    }

    /**
     * 密钥是随机生成的64位数字
     * @author zhouliang@edenep.net
     * @version 2.0
     * @description:
     * @copyright: Copyright (c) 2018
     * @company: 易登科技
     */
    public static String getPrivateKey() {
        String val = new String();
        Random random = new Random();
        for(int i = 0; i < 64; i++) {
            val += String.valueOf(random.nextInt(10));
        }
        return val.toUpperCase();
    }

    /**
     *
     * @author zhouliang@edenep.net
     * @version 2.0
     * @description:
     * @copyright: Copyright (c) 2018
     * @company: 易登科技
     * @date 2019/8/30 0030 下午 1:49
     */
    public static String getVcmSendResult(String cmd, Map<String, String> rMap)
    {
        String key=null;
        String timeSp = rMap.get("TimeSp");
        String mid = rMap.get("Mid");
        Map<String,String> baseMap = new HashMap<>();
        baseMap.put("Mid",mid);
        baseMap.put("TimeSp",timeSp);

        //只有初始化时密钥 是 公钥,其他协议密钥是私钥
        if("1000".equalsIgnoreCase(cmd)){

            PropertiesHelper helper = new PropertiesHelper("system.properties");
            String initVcmUrl = helper.getStringProperty("init.vcm.url");

            String imei = rMap.get("IMEI");
            //初始化时密钥 是 公钥
            key = rMap.get("PubKey");
            String url = String.format("%s?imei=%s",initVcmUrl,imei);
            FormBody.Builder formBody = new FormBody.Builder();
            formBody.add("vmImei",imei);
            formBody.add("comPublicKey",key);
            logger.info(String.format("设备ID: %s,向服务器申请机器ID和密钥请求地址:%s, send ", imei, url));
            ResultVo resultVo = HttpClientUtil.httpPost(url, formBody.build(),"", MediaType.parse("application/json; charset=utf-8"));
            logger.info(String.format("设备ID: %s,向服务器申请机器ID和密钥请求地址:%s, receive : %s", imei, url, JSON.toJSONString(resultVo)));
            if(resultVo.getError_no()!=0){
                return null;
            }
            JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(resultVo.getResult()),JSONObject.class);
            String vmCode = jsonObject.getString("vmCode");
            if(StringHelper.isBlank(vmCode) || vmCode.length()!=10){
                logger.info("服务器下发的机器ID 必须是10位数字");
                return null;
            }
            String comPrivateKey = jsonObject.getString("comPrivateKey");
            if(StringHelper.isBlank(comPrivateKey) || comPrivateKey.length()!=64){
                logger.info("服务器下发的密钥 必须是64位数字");
                return null;
            }
            mid = vmCode;
            baseMap.put("Mid", mid);
            baseMap.put("PriKey", comPrivateKey);
            //机器初始化缓存-保存设备IMEI 与 下发的机器ID 关联关系
            CollectRedisCacheService.addVendingMachineImei(imei,key,baseMap);
        }else if("1001".equalsIgnoreCase(cmd)){
            String imei = CollectRedisCacheService.getVmcImeiByMid(mid);
            key = CollectRedisCacheService.getPrivateKey(imei);
            baseMap.put("TimeSpValue", ""+System.currentTimeMillis()/1000);
        }else if("2000".equalsIgnoreCase(cmd)||"2001".equalsIgnoreCase(cmd)){
            String imei = CollectRedisCacheService.getVmcImeiByMid(mid);
            key = CollectRedisCacheService.getPrivateKey(imei);
            //保存心跳请求到缓存,请求出货时先检验心跳数据是否正常
            CollectRedisCacheService.addVendingMachineHeart(cmd,imei,rMap);
        }else if("4000".equalsIgnoreCase(cmd)){
            String imei = CollectRedisCacheService.getVmcImeiByMid(mid);
            key =  CollectRedisCacheService.getPrivateKey(imei);
            //保存请求出货订单信息
            CollectRedisCacheService.editVendingMachineOrder(cmd,imei,rMap);
        }else if("5000".equalsIgnoreCase(cmd)){
            String imei = CollectRedisCacheService.getVmcImeiByMid(mid);
            key =  CollectRedisCacheService.getPrivateKey(imei);
            //保存机器设备货道信息，到缓存
            CollectRedisCacheService.addVendingMachineWay(imei,rMap);
        }else if("2002".equalsIgnoreCase(cmd) ){
            String imei = CollectRedisCacheService.getVmcImeiByMid(mid);
            key = CollectRedisCacheService.getPrivateKey(imei);
        }
        //下发出货指令
        else if("3000".equalsIgnoreCase(cmd)){
            String imei = CollectRedisCacheService.getVmcImeiByMid(mid);
            key = CollectRedisCacheService.getPrivateKey(imei);
            baseMap = rMap;
            //保存请求出货订单信息
            CollectRedisCacheService.addVendingMachineOrder(imei,rMap);
        }
        //远程清除货道故障并测试货道  6001参数设置
        else if("6002".equalsIgnoreCase(cmd) || "6001".equalsIgnoreCase(cmd) || "6000".equalsIgnoreCase(cmd)){
            String imei = CollectRedisCacheService.getVmcImeiByMid(mid);
            key = CollectRedisCacheService.getPrivateKey(imei);
            baseMap = rMap;
        }
        StringBuffer strAgreement = new StringBuffer();
        strAgreement.append(cmd);
        strAgreement.append("$");
        strAgreement.append(JSON.toJSONString(baseMap));
        strAgreement.append("$");
        strAgreement.append(getSign(JSON.toJSONString(baseMap), mid, timeSp, key));
        return strAgreement.toString();
    }

    /**
     * 生成签名
     * @author zhouliang@edenep.net
     * @version 2.0
     * @description:
     * @copyright: Copyright (c) 2018
     * @company: 易登科技
     * @date 2019/8/30 0030 下午 1:49
     */
    private static String getSign(String json, String mid, String timeSp, String signKey)
    {
        logger.info("getSign json: "+json+" mid: "+mid+" timeSp: "+timeSp+" signKey: "+signKey);
        String sign = "";
        String regEx = "[:\"-',{}\\[\\]]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(json);
        json = m.replaceAll("").trim();

        logger.info("getSign json: "+json);

        byte[] bCmdData = new byte[16];
        byte[] bMid = hexStringToBytes(getLengthData(10,mid));
        byte[] bTimeSp = hexStringToBytes(getLengthData(10,timeSp));
        byte[] bJson = hexStringToBytes(getLengthData(10,getCheckSum(json)));
        bCmdData[15] = 0x00;

        System.arraycopy(bMid, 0, bCmdData, 0, bMid.length);

        System.arraycopy(bTimeSp, 0, bCmdData, bMid.length, bTimeSp.length);

        System.arraycopy(bJson, 0, bCmdData, bMid.length + bTimeSp.length, bJson.length);

        logger.info("加密之前的数据 bCmdData: "+ bytesToHexString(bCmdData));

        byte[] aesData = AESHelper.aesEncryptGetBytes(bCmdData,signKey);

        logger.info("加密之后数据aesData: "+ bytesToHexString(aesData));

        byte[] bSignData = new byte[4];

        if ((aesData != null) && (aesData.length > 3)) {
            System.arraycopy(aesData, 0, bSignData, 0, 4);
            sign = bytesToHexString(bSignData);
            if (sign != null) {
                sign = sign.toUpperCase();
            }
        }
        logger.info("得到的签名 sign: "+ sign);
        return sign;
    }

    private static String getLengthData(int length, String data) {
        String retData = data;
        if (StringHelper.isEmpty(data)) {
            return retData;
        }
        int dataLength = data.length();
        if (dataLength < length) {
            for (int i = 0; i < (length - dataLength); i++) {
                retData = "0" + retData;
            }
        } else {
            retData = data.substring(dataLength - length);
        }

        return retData;
    }

    /**
     * Convert char to byte
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * Convert hex string to byte[]
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * 累加和校验码
     * @param str 数据
     * @return
     */
    public static String getCheckSum(String str) {
        if (StringHelper.isEmpty(str)) {
            return "";
        }
        int data = 0;
        for(int i=0; i < str.length(); i++){
            data += (int)str.charAt(i);
        }

        return String.valueOf(data);
    }

    public static boolean checkWayStatus(String imei,String slot) {

        Map<String,String> wayMap = CollectRedisCacheService.getVendingMachineWay(imei);
        if(null == wayMap || wayMap.isEmpty()){
            return false;
        }

        try {
            String slotInfo = wayMap.get("SlotInfo");
            List<Map> list = JSON.parseArray(slotInfo, Map.class);
            for (Map map:list) {
                if(slot.equalsIgnoreCase(map.get("SlotNum").toString()) && "1".equalsIgnoreCase(map.get("Status").toString())){
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static Map<String, String> getReceiveValueMap(ReceiveMessage msg) {
        String bodyStr = DefaultProtocolConvert.bytes2string(DefaultProtocolConvert.hexstring2bytes(msg.getMsgByte()));
        bodyStr = bodyStr.substring(bodyStr.indexOf("{"),bodyStr.lastIndexOf("}")+1);
        Map<String, String> rMap = new HashMap<>();
        Map<String, Object> objectMap = JSON.parseObject(bodyStr,Map.class);
        objectMap.forEach((k,v)->{
            if(v instanceof List){
                rMap.put(k, JSON.toJSONString(v).replace("\"","\'"));
            }else{
                rMap.put(k, v.toString());
            }
        });
        return rMap;
    }

    public static void main(String[] args) {

        PropertiesHelper helper = new PropertiesHelper("system.properties");
        String initVcmUrl = helper.getStringProperty("init.vcm.url");

        String imei = "9876543210";
        String url = String.format("%s?imei=%s",initVcmUrl,imei);
        logger.info(String.format("设备ID: %s,向服务器申请机器ID和密钥请求地址:%s, send ", imei, url));
        FormBody.Builder formBody = new FormBody.Builder();
        formBody.add("vmImei",imei);
        formBody.add("comPublicKey",imei);
        ResultVo resultVo = HttpClientUtil.httpPost(url, formBody.build(),"", MediaType.parse("application/json; charset=utf-8"));
        logger.info(String.format("设备ID: %s,向服务器申请机器ID和密钥请求地址:%s, receive : %s", imei, url, JSON.toJSONString(resultVo)));
        System.out.println(JSON.toJSONString(resultVo));
        if(resultVo.getError_no()==0){
            JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(resultVo.getResult()),JSONObject.class);
            System.out.println(jsonObject.getString("vmCode"));
            System.out.println(jsonObject.getString("comPrivateKey"));
        }
    }
}
