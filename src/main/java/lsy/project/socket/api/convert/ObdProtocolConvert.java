package lsy.project.socket.api.convert;

import java.math.BigDecimal;


/**
 * OBD协议的字段转换工具
 *
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public class ObdProtocolConvert {

    /**
     * 纬度转换例如：19度59.5945分,因为地图只认度，所以需要把分转成度，即59.5945/60 =
     * 0.99324，所以最后得到19.99324度
     *
     * @param lat 传入的纬度
     * @return newlat
     */
    public static Double transformationLat(String lat) {
        String du = lat.substring(0, 2);
        String fen = lat.substring(2, 8);
        BigDecimal bd = new BigDecimal(Double.valueOf(fen)).setScale(0, BigDecimal.ROUND_HALF_UP);
        fen = addRightZeroForNum(Integer.parseInt(bd.toString()) + "", 6);
        Double newfen = div(Double.valueOf(fen) / 10000, 60D, 10);
        Double newlatd = add(Double.valueOf(du), newfen);
        return newlatd;
    }

    /**
     * 纬度转换
     *
     * @param arg
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018年6月7日 下午9:12:56
     */
    public static Double transformationLat_new(byte[] arg) {

        String lat = DefaultProtocolConvert.bytes2hexstring(arg);
        //取度数
        String du = lat.substring(0, 2);
        //取分
        String fen = lat.substring(2, 8);

        //取分的整数
        String integerBit = fen.substring(0, 2);
        //取分的小数
        String decimalBit = fen.substring(2, 6);

        //用分的整数+小数 
        double nfen = add(Double.valueOf(integerBit), Double.valueOf(String.format("0.%s", decimalBit)));
        //分转为度数 除以 60
        Double newfen = div(Double.valueOf(nfen), 60D, 10);

        Double newlatd = add(Double.valueOf(du), newfen);
        return newlatd;
    }

    /**
     * 经度转换
     *
     * @param arg 传入的经度
     * @return newlng
     */
    public static Double transformationLng_new(byte[] arg) {

        String lngStr = DefaultProtocolConvert.bytes2hexstring(arg);
        lngStr = (String) lngStr.subSequence(0, 9);

        //取度数
        String du = lngStr.substring(0, 3);
        //取分
        String fen = lngStr.substring(3, 9);
        //取分的整数
        String integerBit = fen.substring(0, 2);
        //取分的小数
        String decimalBit = fen.substring(2, 6);

        //用分的整数+小数 
        double nfen = add(Double.valueOf(integerBit), Double.valueOf(String.format("0.%s", decimalBit)));
        //分转为度数 除以 60
        Double newfen = div(Double.valueOf(nfen), 60D, 10);
        Double newlngd = add(Double.valueOf(du), newfen);
        return newlngd;
    }

    /**
     * 经度转换，例如：110度18.0524分，和纬度一样的转化方式，18.0524/60 =
     * 0.3008733，最后得到110.3008733度
     *
     * @param lng 传入的经度
     * @return newlng
     */
    public static Double transformationLng(String lng) {
        String du = lng.substring(0, 3);
        String fen = lng.substring(3, 9);
        Double newfen = div(Double.valueOf(fen) / 10000, 60D, 10);
        Double newlngd = add(Double.valueOf(du), newfen);
        return newlngd;
    }

    /**
     * 盒子协议 油杆外设解析方法 获取油箱油量高度百分比
     *
     * @param bytes
     * @return -1 与外设通讯中断 -2 表示未通讯上  -3 解析报异常  正常情况下 返回百分比
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018年6月7日 上午11:43:20
     */
    public static String getFuel(byte[] bytes) {

        BigDecimal dfuel = BigDecimal.ZERO;
        try {
            int fuel = DefaultProtocolConvert.hexstring2int(DefaultProtocolConvert.bytes2hexstring(bytes));

            BigDecimal bfuel = new BigDecimal(fuel);
            //FFFF是初始值，表示未通讯上
            if (fuel == 65535) {
                return "-2";
            } else if (fuel == 0) {//0000 与外设通讯中断
                return "-1";
            } else {
                dfuel = bfuel.subtract(BigDecimal.valueOf(1)).divide(BigDecimal.valueOf(65533), 2, BigDecimal.ROUND_HALF_EVEN);
            }
        } catch (Exception e) {
            dfuel = BigDecimal.valueOf(-3);
        }
        return dfuel.toString();
    }

    /**
     * 提供精确的加法运算
     *
     * @param v1
     * @param v2
     * @return
     */
    public static double add(Double v1, Double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }

    /**
     * 提供精确的除法运算
     *
     * @param v1
     * @param v2
     * @param scale
     * @return
     */
    public static double div(Double v1, Double v2, int scale) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();// scale 后的四舍五入
    }

    /**
     * 字符串右边补零
     *
     * @param str
     * @param strLength 位数
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018 2018年6月6日 下午6:39:38
     */
    public static String addRightZeroForNum(String str, int strLength) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer sb = new StringBuffer();
                sb.append(str).append("0");//右补0
                str = sb.toString();
                strLen = str.length();
            }
        }
        return str;
    }

    /**
     * 获取指令字节的第0位，byte 总共占8位 8个bit
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018 2018年8月6日 下午4:10:00
     */
    public static int getBit0OnByte(byte[] bytes) {
        byte[] bits = DefaultProtocolConvert.byte2bitArray(bytes);
        return bits[0];
    }

    /**
     * 获取指令字节的第1位，byte 总共占8位 8个bit
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年8月6日 下午4:10:34
     */
    public static int getBit1OnByte(byte[] bytes) {
        byte[] bits = DefaultProtocolConvert.byte2bitArray(bytes);
        return bits[1];
    }

    /**
     * 获取指令字节的第2位，byte 总共占8位 8个bit
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年8月6日 16:12:08
     */
    public static int getBit2OnByte(byte[] bytes) {
        byte[] bits = DefaultProtocolConvert.byte2bitArray(bytes);
        return bits[2];
    }

    /**
     * 获取指令字节的第3位，byte 总共占8位 8个bit
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年8月6日 16:12:05
     */
    public static int getBit3OnByte(byte[] bytes) {
        byte[] bits = DefaultProtocolConvert.byte2bitArray(bytes);
        return bits[3];
    }

    /**
     * 获取指令字节的第4位，byte 总共占8位 8个bit
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年8月6日 16:12:01
     */
    public static int getBit4OnByte(byte[] bytes) {
        byte[] bits = DefaultProtocolConvert.byte2bitArray(bytes);
        return bits[4];
    }

    /**
     * 获取指令字节的第5位，byte 总共占8位 8个bit
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年8月6日 16:11:55
     */
    public static int getBit5OnByte(byte[] bytes) {
        byte[] bits = DefaultProtocolConvert.byte2bitArray(bytes);
        return bits[5];
    }

    /**
     * 获取指令字节的第6位，byte 总共占8位 8个bit
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年8月6日 16:11:51
     */
    public static int getBit6OnByte(byte[] bytes) {
        byte[] bits = DefaultProtocolConvert.byte2bitArray(bytes);
        return bits[6];
    }

    /**
     * 获取指令字节的第7位，byte 总共占8位 8个bit
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年8月6日 16:11:46
     */
    public static int getBit7OnByte(byte[] bytes) {
        byte[] bits = DefaultProtocolConvert.byte2bitArray(bytes);
        return bits[7];
    }


    public static int getCoolantTemperature(byte[] arg) {
        int coolant = DefaultProtocolConvert.bytes2int(arg);
        return coolant - 40;
    }

    /**
     * 得到机油压力
     *
     * @param arg
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: huosibo@edenep.net
     * @version: 2.0
     * @date: 2018年3月1日 上午11:41:49
     */
    public static int getOilPressure(byte[] arg) {
        int coolant = DefaultProtocolConvert.bytes2int(arg);
        return coolant / 100;
    }

    public static void main(String[] args) {
        
        /*String lngStr = "2506023f";
        lngStr = (String) lngStr.subSequence(0, 8);
        //取度数
        String du = lngStr.substring(0, 2);
        //取分
        String fen = lngStr.substring(2, 8);
        
        //取分的整数
        String integerBit = fen.substring(0,2);
        //取分的小数
        String decimalBit = fen.substring(2,6);
        
        //用分的整数+小数 
        double nfen = add(Double.valueOf(integerBit), Double.valueOf(String.format("0.%s", decimalBit)));
        //分转为度数 除以 60
        Double newfen = div(Double.valueOf(nfen), 60D, 10);
        Double newlngd = add(Double.valueOf(du), newfen);
        System.out.println(newlngd);//25.1003833333 25.1038333333
        
        byte [] abyte = DefaultProtocolConvert.hexstring2bytes(lngStr);
        System.out.println(transformationLat_new(abyte));
        */

        String str = "286018042505512084003401029f2901190110093602179711347917470f58071f0000177100700100515413800f804790270000008c0000020000240106c04c29";
        byte[] sbyte = DefaultProtocolConvert.hexstring2bytes(str);

        byte[] abyte = DefaultProtocolConvert.subBytes(sbyte, 33, 4);
        System.out.println(DefaultProtocolConvert.bytes2int(abyte));
        
/*        byte[] bitByte = DefaultProtocolConvert.byte2bitArray(abyte);
        for (byte b : bitByte) {
            System.out.println(b);
        }
        System.out.println(getFuel(DefaultProtocolConvert.subBytes(sbyte, 57, 2)));
*/        //
    }
}
