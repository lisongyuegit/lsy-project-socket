package com.yidcloud.web.callback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.lsy.base.date.DateHelper;
import com.lsy.base.string.StringHelper;
import com.yidcloud.api.contants.CollectContants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.yidcloud.web.cache.CollectRedisCacheService;
import com.yidcloud.web.convert.DefaultProtocolConvert;
import com.yidcloud.web.model.CallbackMessage;
import com.yidcloud.web.model.ReceiveMessage;

/**
 * 卡片机登录协议需要回写的处理类
 *
 * @author 胡洪瑜 huhongyu@edenep.net
 * @version 2.0
 * @company 易登科技
 * @since 2017/11/15 14:16
 */
public class CardLoginCallBack extends AbstractCallback {

    private static Logger logger = LoggerFactory.getLogger(CardLoginCallBack.class);
    
    private long firstShitStartTime;
    
    public long getFirstShitStartTime() {
        return firstShitStartTime;
    }
    
    public void setFirstShitStartTime(long firstShitStartTime) {
        this.firstShitStartTime = firstShitStartTime;
    }

    @Override
    CallbackMessage createCallBackMsg(ReceiveMessage msg) throws Exception {
        logger.info("实现卡片机登录回写方法");
        CallbackMessage callbackMessage = new CallbackMessage();
        //直接取协议协议头
        callbackMessage.setHeaderMsg(DefaultProtocolConvert.hexstring2bytes(msg.getHeadTag()));
        //直接取协议协议尾
        callbackMessage.setFooterMsg(DefaultProtocolConvert.hexstring2bytes(msg.getEndTag()));
        //取上传流水号
        String bytesStr = msg.getMsgByte();
        String seq = bytesStr.split("2c")[2];
        seq = DefaultProtocolConvert.hexstring2string(seq);
        //时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String date="";
        try {
            long now_time = sdf.parse(sdf.format(new Date())).getTime();
            date = now_time/1000+"";
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            logger.error("日期转化为UTC时间失败"+e.getMessage());
        }
        
        String imei = msg.getImei();
        //取缓存中保洁员的排班信息 
        String attendanceScheduleStr = CollectRedisCacheService.getImeiAttScheduleData(imei, CollectContants.ATTENDANCESCHEDULESHIFT);
        //取缓存中网格长的排班信息 
        String girdManagerStr = CollectRedisCacheService.getImeiAttScheduleData(imei,CollectContants.GRIDMANAGERSHIFT);
        
        CardLoginCallBack cl = new CardLoginCallBack();
        //获取最近的两个排班的日期 并将日期转化为hex值
        String shiftTimeHex = getLastTwoWorkShiftHex(attendanceScheduleStr,cl);
        //获取网格长电话
        String phone = getGirdManagerPhone(girdManagerStr,cl);
        
        String reporttime = CollectRedisCacheService.getImeiReportTime(imei)==null?"20":CollectRedisCacheService.getImeiReportTime(imei);
        ////拼接body
        String result = String.format("%s,ACK,%s,%s,%s,LOGIN,%s,%s,%s,%s", imei,seq,date,seq,phone,phone,shiftTimeHex,reporttime);
        
        logger.info(result);
        callbackMessage.setBodyMsg(DefaultProtocolConvert.hexstring2bytes(
                DefaultProtocolConvert.stringToASCII(result, result.length(), "")));
        
        return callbackMessage;
    }
    

    /**
     * 获取网格长电话
     * @description: TODO (若某个网格长上下班时间在保洁员第一个排班时间段内 则返回该网格长电话)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018 2018年4月8日 下午6:09:49
     * @param girdManagerStr
     * @param cl
     * @return
     */
    private static String getGirdManagerPhone(String girdManagerStr,CardLoginCallBack cl) {
        
        if(StringHelper.isBlank(girdManagerStr)) {
            return "";
        }
        //将排班数据json字符串格式 转化为 list集合map对象结构
        List<Map> girdManagerlist = JSON.parseArray(girdManagerStr,Map.class);
        for (Map girdmanager : girdManagerlist) {
            
            //取得网格长今明两天的排班信息--并用fastjson转为list集合
            String adsStr = JSONArray.toJSONString(girdmanager.get(CollectContants.ATTENDANCESCHEDULESHIFT));
            List<Map> aslist = JSON.parseArray(adsStr,Map.class);
            
            List<Date> inWorkTimeList = new ArrayList<>();//存放所有班次的开始时间
            List<Date> offWorkTimeList = new ArrayList<>();//存放所有班次的结束时间
            extractWorkTimeFromlist(aslist, inWorkTimeList, offWorkTimeList);//提取网格长排班日期
            shiftTimeSort(inWorkTimeList, offWorkTimeList);//日期排序 升序
            
            //判断网格长的班次时间是否在 保洁员的第一个班次时间内  在则返回true 并且返回当前网格长的电话号码
            if(judgeGirdManagerBcTime(cl, inWorkTimeList, offWorkTimeList)) {
                return girdmanager.get("phone")+"";
            }
        }
        return "";
    }

    /**
     * 判断网格长的班次时间是否在 保洁员的第一个班次时间内  在则返回true 否则返回false
     * @description: TODO (目前判断的逻辑是 网格长班次上班时间的比保洁员的最近一个班次的开始时间小 且下班时间比保洁员的最近一个班次的开始时间大)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018 2018年4月8日 下午5:49:19
     * @param cl
     * @param inWorkTimeList
     * @param offWorkTimeList
     * @return
     */
    private static boolean judgeGirdManagerBcTime(CardLoginCallBack cl, List<Date> inWorkTimeList,
            List<Date> offWorkTimeList) {
        
        for(int i=0;i<offWorkTimeList.size();i++) {
            
            if(inWorkTimeList.get(i).getTime() <= cl.getFirstShitStartTime()
               && offWorkTimeList.get(i).getTime() > cl.getFirstShitStartTime()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018 2018年4月8日 下午2:46:52
     * @param aslist 待解析的集合列表
     * @param inWorkTimeList 班次开始时间集合-与offWorkTimeList 班次结束时间 组成对应的班次信息
     * @param offWorkTimeList 班次结束时间集合与inWorkTimeList 班次开始时间 组成对应的班次信息
     */
    private static void extractWorkTimeFromlist(List<Map> aslist, List<Date> inWorkTimeList,
            List<Date> offWorkTimeList) {
        
        try {
            int shiftId = 0;//班次id，0表示休息或未排班
            if(aslist!=null && aslist.size()>0){
                for (Map map : aslist) {
                    shiftId = (int) map.get("shiftId");
                    if(shiftId==0)continue;
                    String date = map.get("date").toString();
                    List<Map> shiftTimeList = JSON.parseArray(map.get("shiftTimeList").toString(),Map.class);
                    for (Map shiftTimeMap : shiftTimeList) {
                        String inWorkTime = String.format("%s %s", date,shiftTimeMap.get("inWorkTime"));//班次开始作业时间
                        String offWorkTime = String.format("%s %s", date,shiftTimeMap.get("offWorkTime"));//班次下班时间
                        inWorkTimeList.add(DateHelper.parseString(inWorkTime));
                        offWorkTimeList.add(DateHelper.parseString(offWorkTime));
                    }
                }
            }
        } catch(NumberFormatException e) {
            logger.error("从缓存中数据提取排班日期数据异常,班次ID(shiftId)转为整形出错。"+e.getMessage());
        } catch (Exception e) {
            logger.error("从缓存中数据提取排班日期数据异常"+e.getMessage());
        }
    }

    /**
     * 将集合中的日期进行排序
     * @description: TODO (使用jdk1.8 lamba表达式)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年4月8日 下午10:40:45
     * @param inWorkTimeList
     * @param offWorkTimeList
     */
    private static void shiftTimeSort(List<Date> inWorkTimeList, List<Date> offWorkTimeList) {
        //按照时间排序  -- 升序
        Collections.sort(offWorkTimeList, (arg0, arg1) -> arg0.compareTo(arg1)); 
        Collections.sort(inWorkTimeList, (arg0, arg1) -> arg0.compareTo(arg1));
    }

    /**
     * 取最近的两个班次 并将时间转为为对应的hex值
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018 2018年4月8日 下午2:41:18
     * @param inWorkTimeList
     * @param offWorkTimeList
     * return str
     */
    private static String getLastTwoWorkShiftHex(String schedulestr,CardLoginCallBack cl) {
        
        //将排班数据json字符串格式 转化为 list集合map对象结构
        List<Map> aslist = JSON.parseArray(schedulestr,Map.class);
        
        List<Date> inWorkTimeList = new ArrayList<>();//存放所有班次的开始时间，保存的日期格式yyyy-mm-dd HH:mm:ss
        List<Date> offWorkTimeList = new ArrayList<>();//存放所有班次的结束时间，保存的日期格式yyyy-mm-dd HH:mm:ss
        
        extractWorkTimeFromlist(aslist, inWorkTimeList, offWorkTimeList);//提取保洁员排班日期  
        shiftTimeSort(inWorkTimeList, offWorkTimeList);//日期排序 升序
        
        long nowtime = System.currentTimeMillis();
        int count=0;//用于判断结束时间大于当前时间的排班数量
        String timeList = "";// 返回的时间段字符串:"序号+时间戳转换的hex" 1 5AC17280总共需要两串
        for(int i=0;i<offWorkTimeList.size();i++) {
            
            //取最近的班次 -->下班时间>当前时间比较
            if(offWorkTimeList.get(i).getTime()>nowtime) {
                count++;
                logger.info(DateHelper.formatDate(inWorkTimeList.get(i),DateHelper.PATTERN_TIME) + "--" + 
                        DateHelper.formatDate(offWorkTimeList.get(i),DateHelper.PATTERN_TIME) + "--" + offWorkTimeList.get(i).getTime());
                long time1 = inWorkTimeList.get(i).getTime();
                long time2 = offWorkTimeList.get(i).getTime();
                
                timeList += String.format("0%s%s%s", count,
                            DefaultProtocolConvert.long2hexstring(time1/1000, 8),
                            DefaultProtocolConvert.long2hexstring(time2/1000, 8));
                if(count==1) {
                    cl.setFirstShitStartTime(time1);
                }
            }
            if(count==2) {//因为只需要返回最近两个排班数据给终端，故当排班数若等于2的时候，跳出循环
                break;
            }
        }
        //只有一个排班的情况下第二个排班时间默认为02FFFFFFFFFFFFFFFF,若IMEI对应的保洁员没有排班则返回约定值01FFFFFFFF02FFFFFFFF
        if(count==1) {
            timeList += CollectContants.DEFAULT_SECOND_SCHEDULETIME_HEX_VALUE;
        }else if(count==0){
            timeList = CollectContants.DEFAULT_SCHEDULETIME_HEX_VALUE;
        }
        return timeList;
    }
    
}
