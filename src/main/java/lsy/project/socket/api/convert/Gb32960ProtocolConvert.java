package lsy.project.socket.api.convert;

import com.lsy.base.date.DateHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;



/**
 * 国标协议字段转换
 *
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public class Gb32960ProtocolConvert {

    public static DecimalFormat df = new DecimalFormat("######0"); // 四舍五入转换成整数

    /**
     * 数据是否有效(此协议中所有以0xff作前缀,最后一位0xff,0xfe结尾都是无效数据)
     *
     * @param bt
     * @return
     */
    private static boolean isEffect(byte[] bt) {
        boolean effect = false;
        for (int i = 0; i < bt.length; i++) {
            if (i == bt.length - 1 && bt[i] == (byte) 0xfe) {
                continue;
            } else if (bt[i] != (byte) 0xff) {
                effect = true;
                break;
            }
        }
        return effect;
    }

    /**
     * gps速度
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018 2018年6月1日 下午4:09:11
     */
    public static int getGpsSpeed(byte[] bytes) {
        int speed = 0;
        if (!isEffect(bytes)) {//异常或者无效速度
            speed = -1;//速度错误标记-1
        } else {
            speed = (int) (DefaultProtocolConvert.bytes2int(bytes) * 0.1);
        }
        return speed;
    }

    /**
     * 累计里程
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018 2018年6月1日 下午4:09:11
     */
    public static int getGpsMileage(byte[] bytes) {
        int cumulative = 0;
        if (isEffect(bytes)) {//异常或者无效
            cumulative = -1;//
        } else {
            cumulative = (int) (DefaultProtocolConvert.bytes2int(bytes) * 0.1);
        }
        return cumulative;
    }

    public static int byte2Minint(byte[] bytes) {
        int value = 0;
        if (isEffect(bytes)) {//异常或者无效
            value = -1;//
        } else {
            value = (int) (DefaultProtocolConvert.bytes2int(bytes) * 0.1);
        }
        return value;
    }

    private static String byte2intStr(byte b) {
        int val = DefaultProtocolConvert.bytes2int(new byte[]{b});
        if (val < 10) {
            return "0" + val;
        }
        return "" + val;
    }

    /**
     * gb32960 转化为纬度
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018年6月4日 下午3:48:18
     */
    public static Double getLat(byte[] bytes) {
        BigDecimal lat = BigDecimal.valueOf(DefaultProtocolConvert.bytes2int(bytes) * 0.000001).setScale(6, RoundingMode.HALF_UP);
        return lat.doubleValue();
    }

    /**
     * gb32960 转化为精度
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018年6月4日 下午3:48:18
     */
    public static Double getLng(byte[] bytes) {
        BigDecimal lng = BigDecimal.valueOf(DefaultProtocolConvert.bytes2int(bytes) * 0.000001).setScale(6, RoundingMode.HALF_UP);
        return lng.doubleValue();
    }

    /**
     * 获取数据单元中的时间(数据单元前6位)
     */
    public static String getGpsTime(byte[] bytes) throws ParseException {//f109070c0526
        String dateStr = "" + byte2intStr(bytes[0]) + byte2intStr(bytes[1])
                + byte2intStr(bytes[2]) + byte2intStr(bytes[3]) + byte2intStr(bytes[4])
                + byte2intStr(bytes[5]);
        DateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        Date date = sdf.parse(dateStr);
        return DateHelper.formatTime(date);
    }

    /**
     * 获取can84 底盘速度
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018年6月1日 下午3:13:45
     */
    public static int getChassiSpeed(byte[] bytes) {
        int byte6 = DefaultProtocolConvert.bytes2int(bytes, 0, 1);
        int byte7 = DefaultProtocolConvert.bytes2int(bytes, 1, 1);
        int int0 = (byte7 * 256 + byte6) / 256;
        return int0;
    }

    /**
     * 获取can84 底盘行驶里程
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018年6月1日 下午3:13:45
     */
    public static int getChassiGpsMileage(byte[] bytes) {
        int byte4 = DefaultProtocolConvert.bytes2int(bytes, 0, 1);
        int byte5 = DefaultProtocolConvert.bytes2int(bytes, 1, 1);
        int byte6 = DefaultProtocolConvert.bytes2int(bytes, 2, 1);
        int byte7 = DefaultProtocolConvert.bytes2int(bytes, 3, 1);
        double int0 = ((byte4 + byte5 * 256 + byte6 * 65536 + byte7 * 256 * 65536) * 0.125);
        return Integer.parseInt(df.format(int0));
    }

    /**
     * 获取can84 底盘工作小时
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018年6月1日 下午3:21:37
     */
    public static int getChassiRunningTime(byte[] bytes) {
        //============底盘工作小时 hr==============
        int bytes0 = DefaultProtocolConvert.bytes2int(bytes, 0, 1);
        int bytes1 = DefaultProtocolConvert.bytes2int(bytes, 1, 1);
        int bytes2 = DefaultProtocolConvert.bytes2int(bytes, 2, 1);
        int bytes3 = DefaultProtocolConvert.bytes2int(bytes, 3, 1);
        double int0 = (bytes0 + bytes1 * 256 + bytes2 * 65536 + bytes3 * 256 * 65536) * 0.05;

        return Integer.parseInt(df.format(int0)) * 60;
    }

    /**
     * 获取can84 当前转速下的负荷百分比
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018年6月1日 下午3:53:24
     */
    public static Double getLoadPercentage(byte[] bytes) {
        //============当前转速下的负荷百分比==============
        int int0 = DefaultProtocolConvert.bytes2int(bytes, 0, 1);
        return Double.valueOf(int0);
    }

    /**
     * 获取can84 底盘发动机转速
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018年6月1日 下午4:00:36
     */
    public static Double getRpm(byte[] bytes) {
        //============底盘发动机转速==============
        int byte3 = DefaultProtocolConvert.bytes2int(bytes, 0, 1);
        int byte4 = DefaultProtocolConvert.bytes2int(bytes, 1, 1);
        double int0 = (byte3 * 0.125 + byte4 * 32);
        return Double.valueOf(int0);
    }

    /**
     * 获取can84 底盘发动机水温 ℃=
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018年6月1日 下午4:01:56
     */
    public static Double getCoolantTemperature(byte[] bytes) {
        //============底盘发动机水温 ℃==============
        int int0 = DefaultProtocolConvert.bytes2int(bytes, 0, 1) - 40;
        return Double.valueOf(int0);
    }

    /**
     * 获取can84 底盘发动机机油压力 kPa
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018年6月1日 下午4:02:24
     */
    public static Double getOilPressure(byte[] bytes) {
        //============底盘发动机机油压力 kPa==============
        int int0 = DefaultProtocolConvert.bytes2int(bytes, 0, 1) * 4;
        return Double.valueOf(int0);
    }

    /**
     * 获取定位状态
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018年6月1日 下午4:32:06
     */
    public static int getPositionStatus(byte[] bytes) {
        byte[] bits = DefaultProtocolConvert.byte2bitArray(bytes);
        int LocalStatus = bits[7];//0=有效定位  1=无效定位
        return LocalStatus;
    }

    /**
     * 获取can84 底盘发动机实时油耗 L/h
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018年6月1日 下午4:03:21
     */
    public static double getDynAmicalFuel(byte[] bytes) {
        //============底盘发动机实时油耗 L/h==============
        // 即为瞬时油耗
        int byte0 = DefaultProtocolConvert.bytes2int(bytes, 0, 1);
        int byte1 = DefaultProtocolConvert.bytes2int(bytes, 1, 1);
        double int0 = (byte0 * 0.05 + byte1 * 12.8);
        return int0;
    }

    /**
     * 获取can84 底盘发动机累计油耗
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018年6月1日 下午4:03:24
     */
    public static Double getTotalBurnOff(byte[] bytes) {
        //============底盘发动机累计油耗 L/h==============
        int byte4 = DefaultProtocolConvert.bytes2int(bytes, 0, 1);
        int byte5 = DefaultProtocolConvert.bytes2int(bytes, 1, 1);
        int byte6 = DefaultProtocolConvert.bytes2int(bytes, 2, 1);
        int byte7 = DefaultProtocolConvert.bytes2int(bytes, 3, 1);

        double int0 = (byte4 + byte5 * 256 + byte6 * 65536 + byte7 * 16777216) * 0.5;
        return int0;
    }

    /**
     * 获取油箱油量高度
     *
     * @param bytes
     * @return
     * @description: TODO (16进制 转化为10进制  * 0.1)
     * @author: zl
     * @version: 2.0
     * @date: 2018 2018年6月4日 下午5:22:11
     */
    public static double getTankOilQuantityHigh(byte[] bytes) {

        return DefaultProtocolConvert.hexstring2int(DefaultProtocolConvert.bytes2hexstring(bytes)) * 0.1;
    }


    /**
     * 博实结速度
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @version: 2.0
     * @date: 2019年7月12日14:19:51
     */
    public static int getBSJGpsSpeed(byte[] bytes) {
        int speed = (int) (DefaultProtocolConvert.bytes2int(bytes) * 0.1);
        return speed;
    }

    /**
     * gb32960 转化为纬度
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018年6月4日 下午3:48:18
     */
    public static Double getBSJLat(byte[] bytes) {
        BigDecimal lat = BigDecimal.valueOf(DefaultProtocolConvert.bytes2int(bytes) * 0.000001).setScale(6, RoundingMode.HALF_UP);
        return lat.doubleValue();
    }

    /**
     * gb32960 转化为精度
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018年6月4日 下午3:48:18
     */
    public static Double getBSJLng(byte[] bytes) {
        BigDecimal lng = BigDecimal.valueOf(DefaultProtocolConvert.bytes2int(bytes) * 0.000001).setScale(6, RoundingMode.HALF_UP);
        return lng.doubleValue();
    }

    /**
     * 获取数据单元中的时间(数据单元前6位)
     */
    public static String getBSJGpsTime(byte[] bytes) throws ParseException {//f109070c0526
        String dateStr = "" + byte2intStr(bytes[0]) + byte2intStr(bytes[1])
                + byte2intStr(bytes[2]) + byte2intStr(bytes[3]) + byte2intStr(bytes[4])
                + byte2intStr(bytes[5]);
        DateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        Date date = sdf.parse(dateStr);
        return DateHelper.formatTime(date);
    }

    /**
     * 获取博实结方向
     *
     * @param arg
     * @return
     * @author: zl@edenep.net
     * @version: 2.0
     * @date: 2019年7月12日11:38:12
     */
    public static Integer getBSJDirection(byte[] arg) {
        return DefaultProtocolConvert.bytes2int(arg);
    }

    /**
     * 累计里程
     *
     * @param bytes
     * @return
     * @author: zl
     * @version: 2.0
     * @date: 2019-7-12 14:35:00
     */
    public static int getBSJGpsMileage(byte[] bytes) {
        int cumulative = DefaultProtocolConvert.bytes2int(bytes) * 100;
        return cumulative;
    }

    /**
     * 累计里程
     *
     * @param bytes
     * @return
     * @author: zl
     * @version: 2.0
     * @date: 2019-7-12 14:35:00
     */
    public static int getBSJGpsMileage2(byte[] bytes) {
        int cumulative = DefaultProtocolConvert.bytes2int(bytes);
        return cumulative;
    }

    /**
     * 油杆数据解析
     *
     * @param bytes
     * @return
     * @author: zl
     * @version: 2.0
     * @date: 2019年7月18日15:01:06
     */
    public static String getBSJFuel(byte[] bytes) {
        String fuelFiexdStr = "00080023";
        String sourceStr = DefaultProtocolConvert.bytes2hexstring(bytes);
        //包含以下字符串说明装了油杆
        if (sourceStr.contains(fuelFiexdStr)) {
            String fuelHexStr = sourceStr.substring(sourceStr.indexOf(fuelFiexdStr) + fuelFiexdStr.length(), sourceStr.indexOf(fuelFiexdStr) + fuelFiexdStr.length() + 12);
            Double fuel = Double.parseDouble(DefaultProtocolConvert.hexstring2string(fuelHexStr)) / 100;
            return fuel.toString();
        }
        return "-1";
    }

    /**
     * 开关量数据解析
     *
     * @param bytes
     * @return
     * @author: zl
     * @version: 2.0
     * @date: 2019年7月19日14:01:06
     */
    public static String getEdenepSwitch(byte[] bytes) {
        String oneFourHex = "00000170";//开关量1-4 所在的报文ID
        String fiveTwelveHex = "00000171";//开关量5-12 所在的报文ID
        String sourceStr = DefaultProtocolConvert.bytes2hexstring(bytes);

        int openSwitchNumber = 0;//打开开关数量
        if (sourceStr.contains(oneFourHex)) {

            //取报文ID: 00000170 后的第四个字节
            String oneFourStr = sourceStr.substring(sourceStr.indexOf(oneFourHex) + oneFourHex.length() + 6, sourceStr.indexOf(oneFourHex) + oneFourHex.length() + 8);
            byte[] bs = DefaultProtocolConvert.byte2bitArray(DefaultProtocolConvert.hexstring2bytes(oneFourStr));
            for (int j = 4; j < bs.length; j++) {
                if (bs[j] == 1) {
                    openSwitchNumber++;
                }
            }
        }

        if (sourceStr.contains(fiveTwelveHex)) {
            //取报文ID: 00000171 后的第三个字节
            String fiveTwelveStr = sourceStr.substring(sourceStr.indexOf(fiveTwelveHex) + fiveTwelveHex.length() + 4, sourceStr.indexOf(fiveTwelveHex) + fiveTwelveHex.length() + 6);
            byte[] bs = DefaultProtocolConvert.byte2bitArray(DefaultProtocolConvert.hexstring2bytes(fiveTwelveStr));
            for (int j = 0; j < bs.length; j++) {
                if (bs[j] == 1) {
                    openSwitchNumber++;
                }
            }
        }
        return openSwitchNumber > 1 ? "1" : "0";
    }

    public static String getOneTwelveSwitch(byte[] bytes) {
        return getFiveTwelveSwitch(bytes) + getOneFourSwitch(bytes);
    }

    public static String getSixSwitch(byte[] bytes) {
        return getOneTwelveSwitch(bytes).charAt(6) + "";
    }

    public static String getOneFourSwitch(byte[] bytes) {
        //开关量1-4 所在的报文ID
        String oneFourHex = "00000170";
        String sourceStr = DefaultProtocolConvert.bytes2hexstring(bytes);
        StringBuffer sb = new StringBuffer();
        if (sourceStr.contains(sourceStr)) {
            //取报文ID: 00000170 后的第四个字节
            String oneFourStr = sourceStr.substring(sourceStr.indexOf(oneFourHex) + oneFourHex.length() + 6, sourceStr.indexOf(oneFourHex) + oneFourHex.length() + 8);
            byte[] bs = DefaultProtocolConvert.byte2bitArray(DefaultProtocolConvert.hexstring2bytes(oneFourStr));
            for (int j = 4; j < bs.length; j++) {
                if (bs[j] == 1) {
                    sb.append("1");
                } else {
                    sb.append("0");
                }
            }
        }
        return sb.toString();
    }

    public static String getFiveTwelveSwitch(byte[] bytes) {
        //开关量5-12 所在的报文ID
        String fiveTwelveHex = "00000171";
        String sourceStr = DefaultProtocolConvert.bytes2hexstring(bytes);
        StringBuffer sb = new StringBuffer();
        if (sourceStr.contains(fiveTwelveHex)) {
            //取报文ID: 00000171 后的第三个字节
            String fiveTwelveStr = sourceStr.substring(sourceStr.indexOf(fiveTwelveHex) + fiveTwelveHex.length() + 4, sourceStr.indexOf(fiveTwelveHex) + fiveTwelveHex.length() + 6);
            byte[] bs = DefaultProtocolConvert.byte2bitArray(DefaultProtocolConvert.hexstring2bytes(fiveTwelveStr));
            for (int j = 0; j < bs.length; j++) {
                if (bs[j] == 1) {
                    sb.append("1");
                } else {
                    sb.append("0");
                }
            }
        }
        return sb.toString();
    }


    public static void main(String[] args) {

        String hex = "232302fe313030303030303030303030303031383701027613071a0d1a2501000000000000000000000000000000000000000002020000000000000000000000000000000000000000000000000400000000000500070acc5e017527d080003000000105000000000000000000000106000000000000000000000107000000000000000000000109000000000000000081003c00000110000000000000000000000111000000000000000000000115000000000000000000000120000000000000000000000125000000000000000082000c0000013000000000000000008300300000014000000000000000000000014100000000000000000000014200000000000000000000014300000000000000008400d80cfe6ceeffff3f3f2a07600818fee0006c5101006851010018fee500ee3f0000d1c000000cf00300d0431a00ff3f9e7d0cf00400219393562100049318feee0079ffffffffffffff18feef00ffffff51fffffffa18fef2003b00a4050006f9ff18fee9001a1c00001a1c000018fedf0085a0287dfbfffff418fec10031f3200098f3200018feca0003ff00000000ffff00000211000000000000000000000279000000000000000000000392000000000000000000000431000000000000000000000523000000000000000018fcfefd000000000000000085006000000070000000000000000000000071000000000000000000000072000000000000000000000073000000000000000000000074000000000000000000000075000000000000000000000076000000000000000000000077000000000000000086003c00000160000000000000000000000161000000000000000000000162000000000000000000000170440000a15ec935b9000001710a820f00de0000036e";
        hex = "232302fe313030303030303030303030303031383701027613071a0d222501000000000000000000000000000000000000000002020000000000000000000000000000000000000000000000000400000000000500070adbe60175210780003000000105000000000000000000000106000000000000000000000107000000000000000000000109000000000000000081003c00000110000000000000000000000111000000000000000000000115000000000000000000000120000000000000000000000125000000000000000082000c0000013000000000000000008300300000014000000000000000000000014100000000000000000000014200000000000000000000014300000000000000008400d80cfe6ceeffff3f3f0000000018fee000755101007151010018fee500f03f0000d8c000000cf00300d1001800ff7f937d0cf00400407d8bec1500048c18feee0079ffffffffffffff18feef00ffffff2ffffffffa18fef2001f0000000006faff18fee9001b1c00001b1c000018fedf0084a0287dfbfffff418fec1000df4200076f4200018feca0003ff00000000ffff00000211000000000000000000000279000000000000000000000392000000000000000000000431000000000000000000000523000000000000000018fcfefd000000000000000085006000000070000000000000000000000071000000000000000000000072000000000000000000000073000000000000000000000074000000000000000000000075000000000000000000000076000000000000000000000077000000000000000086003c00000160000000000000000000000161000000000000000000000162000000000000000000000170230000a15e8d1b72000001710a8600010e00000346";
        byte[] bytes = DefaultProtocolConvert.hexstring2bytes(hex);
        System.out.println(getEdenepSwitch(bytes));

        int openSwitchNumber = 0;
        byte[] bs = DefaultProtocolConvert.byte2bitArray(DefaultProtocolConvert.hexstring2bytes("08"));
        for (int j = 0; j < bs.length; j++) {
            if (bs[j] == 1) {
                openSwitchNumber++;
            }
            System.out.println(bs[j]);
        }
    }
}
