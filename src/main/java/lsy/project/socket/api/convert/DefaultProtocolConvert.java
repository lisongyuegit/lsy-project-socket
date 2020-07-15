package lsy.project.socket.api.convert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.lsy.base.date.DateHelper;
import lsy.project.socket.api.cache.CollectRedisCacheService;
import lsy.project.socket.api.contants.VehicleSweepingConditionEnum;
import org.springframework.util.Assert;

/**
 * 默认的字段转换工具
 *
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public class DefaultProtocolConvert {

    /**
     * 将一个16进制字符串转换为int
     *
     * @param val
     * @return Integer
     */
    public static Integer hexstring2int(String val) {
        return Integer.valueOf(val, 16);
    }

    /**
     * 将一个int类型转换为16进制字符串
     *
     * @param val int
     * @return String
     */
    public static String int2hexstring(int val) {
        return Integer.toHexString(val);
    }

    public static String int2hexstringBig(int val, int size) {
        String hex = fixHexStringOffset(Integer.toHexString(val), size);
        StringBuffer buf = new StringBuffer();
        for (int i = hex.length() / 2; i > 0; i--) {
            buf.append(hex.substring((i - 1) * 2, i * 2));
        }
        return buf.toString();
    }

    /**
     * a
     *
     * @param byteArr
     * @return
     */
    public static Double bytes2Double(byte[] byteArr) {
        return Double.parseDouble(bytes2string(byteArr));
    }

    public static String int2hexstring(int val, int size) {
        return fixHexStringOffset(Integer.toHexString(val), size);
    }

    public static String twoStr2hexstring(String val, int size) {
        return int2hexstring(Integer.parseInt(val, 2), size);
    }

    /**
     * 将int转成指定长度的二进制字符串，位数不足的用0补足 <br>
     *
     * @param val    : int值 <br>
     * @param length ： 指定长度 <br>
     * @return
     */
    public static String int2BinaryString(int val, int length) {
        StringBuffer sb = new StringBuffer();
        String result = Integer.toBinaryString(val);
        if (result.length() < length) {
            for (int i = 0; i < length - result.length(); i++) {
                sb.append("0");
            }
        }
        sb.append(result);
        return sb.toString();
    }

    /**
     * 将一个Sting转换为ASCII码
     *
     * @param val
     * @param length
     * @param appendHex
     * @return
     */
    public static String stringToASCII(String val, int length, String appendHex) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; (i < val.length()) && (i < length); i++) {
            sb.append(int2hexstring(val.charAt(i), 2));
        }
        for (int i = 0; i < length - val.length(); i++) {
            sb.append(appendHex);
        }
        return sb.toString();
    }

    /**
     * 将一个字符串连续拼接多少次
     *
     * @param hex
     * @param repeat
     * @return String
     */
    public static String stringRepeat(String hex, int repeat) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < repeat; i++) {
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * 将一个char转换为byte[] 字节
     *
     * @param c
     * @return byte[]
     */
    public static byte[] char2bytes(char c) {
        byte[] b = new byte[2];
        b[0] = (byte) ((c & 0xFF00) >> '\b');
        b[1] = (byte) (c & 0xFF);
        return b;
    }

    /**
     * @param b
     * @return
     * @description: TODO (将一个byte[]字节转换为char)
     * @author:
     * @version: 2.0
     * @date: 2017 2017年11月11日 上午10:23:23
     */
    public static char bytes2char(byte[] b) {
        char c = (char) ((b[0] & 0xFF) << 8 | b[1] & 0xFF);
        return c;
    }

    /**
     * @param src
     * @param size
     * @return String
     * @description: TODO (对指定字符串用零补齐指定位数的空缺位)
     * @author:
     * @version: 2.0
     * @date: 2017 2017年11月11日 上午10:22:53
     */
    public static String bytes2hexstring(byte[] src, int size) {
        return fixHexStringOffset(bytes2hexstring(src), size);
    }

    /**
     * @param src
     * @param size
     * @param addHeader
     * @return String
     * @description: TODO (将一个bate字节转换成16进制字符串)
     * @author: 霍思博
     * @version: 2.0
     * @date: 2017 2017年11月11日 上午10:21:53
     */
    public static String bytes2hexstring(byte[] src, int size, boolean addHeader) {
        return fixHexStringOffset22(bytes2hexstring(src), size, addHeader);
    }

    /**
     * @param src
     * @return String
     * @description: TODO
     * (这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。)
     * @author: 霍思博
     * @version: 2.0
     * @date: 2017 2017年11月11日 上午10:23:57
     */
    public static String bytes2hexstring(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if ((src == null) || (src.length <= 0)) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF; // 把byte值还原成无符号值并放入int 中
            String hv = Integer.toHexString(v); // 将10进制数转换成16进制
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * @param src
     * @param start
     * @param size
     * @return String
     * @description: TODO (将字节数组指定的字节转换成16进制字符串。)
     * @author: 霍思博
     * @version: 2.0
     * @date: 2017 2017年11月11日 上午10:24:24
     */
    public static String bytes2hexstring(byte[] src, int start, int size) {
        return bytes2hexstring(getChildArray(src, start, size));
    }

    /**
     * @param src
     * @param start
     * @param size
     * @return byte[]
     * @description: TODO (根据指定的起始位置和长度得到所给字节数组的子数组)
     * @author:
     * @version: 2.0
     * @date: 2017 2017年11月11日 上午10:25:55
     */
    public static byte[] getChildArray(byte[] src, int start, int size) {
        if ((src == null) || (src.length <= 0) || (size <= 0)) {
            return null;
        }
        byte[] bytes = new byte[size];
        for (int i = 0; i < size; i++) {
            bytes[i] = src[(start + i)];
        }
        return bytes;
    }

    /**
     * @param src
     * @param end  最后一位坐标
     * @param size
     * @return byte[]
     * @description: TODO (根据指定的起始位置和长度得到所给字节数组的子数组)
     * @author:
     * @version: 2.0
     * @date: 2017 2017年11月11日 上午10:25:55
     */
    public static byte[] getChildArrayByEnd(byte[] src, int end, int size) {
        if ((src == null) || (src.length <= 0) || (size <= 0)) {
            return null;
        }
        byte[] bytes = new byte[size];
        for (int i = 0; i < size; i++) {
            bytes[i] = src[(end - size + 1 + i)];
        }
        return bytes;
    }

    /**
     * @param hexString
     * @return byte[]
     * @description: TODO (将一个16进制字符串转换成字节数组)
     * @author: 霍思博
     * @version: 2.0
     * @date: 2017 2017年11月11日 上午10:26:34
     */
    public static byte[] hexstring2bytes(String hexString) {
        if ((hexString == null) || (hexString.equals(""))) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (char2byte(hexChars[pos]) << 4 | char2byte(hexChars[(pos + 1)]));
        }

        return d;
    }

    /**
     * @param c
     * @return
     * @description: TODO (将一个char类型字符转成byte)
     * @author: 霍思博
     * @version: 2.0
     * @date: 2017 2017年11月11日 上午10:54:23
     */
    private static byte char2byte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * @param b
     * @return String
     * @description: TODO (把byte转为字符串的bit)
     * @author: 霍思博
     * @version: 2.0
     * @date: 2017 2017年11月11日 上午10:57:27
     */
    public static String byte2bitstring(byte b) {
        return new StringBuilder().append("") //
                .append((byte) (b >> 7 & 0x1)) //
                .append((byte) (b >> 6 & 0x1)) //
                .append((byte) (b >> 5 & 0x1)) //
                .append((byte) (b >> 4 & 0x1)) //
                .append((byte) (b >> 3 & 0x1)) //
                .append((byte) (b >> 2 & 0x1)) //
                .append((byte) (b >> 1 & 0x1)) //
                .append((byte) (b >> 0 & 0x1)) //
                .toString();
    }

    public static String byte2bitstring(byte[] bytes) {
        byte b = bytes[0];
        return new StringBuilder().append("") //
                .append((byte) (b >> 7 & 0x1)) //
                .append((byte) (b >> 6 & 0x1)) //
                .append((byte) (b >> 5 & 0x1)) //
                .append((byte) (b >> 4 & 0x1)) //
                .append((byte) (b >> 3 & 0x1)) //
                .append((byte) (b >> 2 & 0x1)) //
                .append((byte) (b >> 1 & 0x1)) //
                .append((byte) (b >> 0 & 0x1)) //
                .toString();
    }

    /**
     * @param b
     * @param start
     * @param size
     * @return String
     * @description: TODO (把byte[]的第几位到第几位转为字符串的bit)
     * @author: 霍思博
     * @version: 2.0
     * @date: 2017 2017年11月11日 上午10:58:16
     */
    public static String byte2bitstring(byte[] b, int start, int size) {
        b = getChildArray(b, start, size);
        StringBuffer sb = new StringBuffer();
        for (byte bt : b) {
            sb.append(byte2bitstring(bt));
        }
        return sb.toString();
    }

    public static String byte2bitsubstring(byte[] b, int start, int size) {
        StringBuffer sb = new StringBuffer();
        for (byte bt : b) {
            sb.append(byte2bitstring(bt));
        }
        return sb.toString().substring(start, start + size);
    }

    /**
     * @param byteStr
     * @return byte
     * @description: TODO (Bit转Byte)
     * @author: 霍思博
     * @version: 2.0
     * @date: 2017 2017年11月11日 上午11:00:20
     */
    public static byte bit2byte(String byteStr) {
        if (null == byteStr) {
            return 0;
        }
        int len = byteStr.length();
        if ((len != 4) && (len != 8)) {
            return 0;
        }
        int re = 0;
        if (len == 8) { // 8 bit处理
            if (byteStr.charAt(0) == '0') { // 正数
                re = Integer.parseInt(byteStr, 2);
            } else { // 负数
                re = Integer.parseInt(byteStr, 2) - 256;
            }
        } else { // 4 bit处理
            re = Integer.parseInt(byteStr, 2);
        }
        return (byte) re;
    }

    /**
     * 把一个byte类型的数据的每一位都输出
     *
     * @param b
     * @return
     */
    public static byte[] byte2bitArray(byte b) {
        byte[] array = new byte[8];
        for (int i = 7; i >= 0; i--) {
            array[i] = (byte) (b & 0x1);
            b = (byte) (b >> 1);
        }
        return array;
    }

    /**
     * 把一个字节数组中的每一位都放到字节数组中输出出来
     *
     * @param bs
     * @return
     */
    public static byte[] byte2bitArray(byte[] bs) {
        if (bs.length == 1) {
            return byte2bitArray(bs[0]);
        }
        byte[] result = byte2bitArray(bs[0]);
        for (int i = 1; i < bs.length; i++) {
            result = add(result, byte2bitArray(bs[i]));
        }
        return result;
    }

    public static String[] bitStrIndexs(String bitStr, char bit) {
        int i = bitStr.length() - 1;
        StringBuffer result = new StringBuffer();
        for (char ch : bitStr.toCharArray()) {
            if (ch == bit) {
                result.append(i);
                result.append(",");
            }
            i--;
        }
        if (result.length() == 0) {
            return new String[0];
        }
        return result.toString().split(",");
    }

    /**
     * 将int转成长度为4的字节数组
     *
     * @param i
     * @return
     */
    public static byte[] int2byteArray(int i) {
        return int2byteArray(i, 4);
    }

    /**
     * 将int转成长度为4的字节数组
     *
     * @param iSource
     * @param iArrayLen
     * @return
     */
    public static byte[] int2byteArray(int iSource, int iArrayLen) {
        byte[] bLocalArr = new byte[iArrayLen];
        for (int i = 0; (i < 4) && (i < iArrayLen); i++) {
            bLocalArr[i] = (byte) (iSource >> 8 * i & 0xFF);
        }
        byte[] newbLocalArr = new byte[iArrayLen];
        for (int i = 0; i < iArrayLen; i++) {
            newbLocalArr[i] = bLocalArr[(iArrayLen - i - 1)];
        }
        return newbLocalArr;
    }

    public static int bytes2int(byte[] bytes, int start, int length, boolean fhFlag) {
        return bytes2int(bytes, start, length, true, fhFlag);
    }

    public static int bytes2intSamll(byte[] bytes, int start, int length, boolean fhFlag) {
        return bytes2int(bytes, start, length, false, fhFlag);
    }

    /**
     * 将字节数组转化成int值
     *
     * @param bytes
     * @param start
     * @param length
     * @return
     */
    public static int bytes2int(byte[] bytes, int start, int length) {
        return bytes2int(bytes, start, length, true, false);
    }

    public static int bytes2intSamll(byte[] bytes, int start, int length) {
        return bytes2int(bytes, start, length, false, false);
    }

    /**
     * 将字节数组转化成int值
     *
     * @param bytes
     * @param start
     * @param length
     * @param isBig
     * @param fhFlag
     * @return
     */
    private static int bytes2int(byte[] bytes, int start, int length, boolean isBig,
                                 boolean fhFlag) {
        if ((1 > length) || (length > 4)) {
            throw new RuntimeException(new StringBuilder().append(" int bytes length is 1-4:")
                    .append(length).toString());
        }
        byte[] bytesSub = getChildArray(bytes, start, length);

        int temp = 0;
        int fh = 1;
        if (fhFlag) {
            byte first = bytesSub[0];
            if (first < 0) {
                fh = -1;
                int firstInt = first & 0x7F;
                byte[] bs = int2byteArray(firstInt, 1);
                bytesSub[0] = bs[0];
            }
        }

        int mask = 255;

        int n = 0;
        for (int i = 0; i < bytesSub.length; i++) {
            n <<= 8;
            if (isBig) {
                temp = bytesSub[i] & mask;
            } else {
                temp = bytesSub[(bytesSub.length - 1 - i)] & mask;
            }
            n |= temp;
        }
        return n * fh;
    }

    public static long bytes2long(byte[] bytes) {
        int mask = 255;
        int temp = 0;
        long n = 0L;
        for (int i = 0; i < bytes.length; i++) {
            n <<= 8;
            temp = bytes[i] & mask;
            n |= temp;
        }
        return n;
    }

    public static long bytes2long(byte[] bytes, int start, int length) {
        return bytes2long(getChildArray(bytes, start, length));
    }

    public static byte[] int2bytesBig(int a, int leng) {
        if ((leng > 4) && (leng < 1)) {
            throw new RuntimeException(new StringBuilder()
                    .append("int to bytes length must be 1-4,length is").append(leng).toString());
        }

        int byteLeng = 4;
        byte[] result = new byte[byteLeng];

        for (int i = 0; i < byteLeng; i++) {
            result[i] = (byte) (a >> (byteLeng - 1 - i) * 8 & 0xFF);
        }
        if (leng == 4) {
            return result;
        }
        return getChildArray(result, result.length - leng, leng);
    }

    public static byte[] long2bytesBig(long a, int leng) {
        if ((leng > 8) && (leng < 1)) {
            throw new RuntimeException(new StringBuilder()
                    .append("int to bytes length must be 1-8,length is").append(leng).toString());
        }

        int byteLeng = 8;
        byte[] result = new byte[byteLeng];
        for (int i = 0; i < byteLeng; i++) {
            result[i] = (byte) (int) (a >> (byteLeng - 1 - i) * 8 & 0xFF);
        }
        if (leng == 8) {
            return result;
        }
        return getChildArray(result, result.length - leng, leng);
    }

    public static String octetString(String str, int bytes) {
        byte[] b = null;
        try {
            b = str.getBytes("GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String n = encodeHex(b).replaceAll(" ", "");
        int len = n.length() / 2;
        if (len < bytes) {
            StringBuffer sb = new StringBuffer(n);
            while (len < bytes) {
                sb.append("00");
                len++;
            }
            return sb.toString();
        }
        return n;
    }

    public static final String encodeHex(byte[] bytes) {
        StringBuffer buff = new StringBuffer(bytes.length * 2);

        for (int i = 0; i < bytes.length; i++) {
            String b = Integer.toHexString(bytes[i]);
            buff.append(b.length() > 2 ? b.substring(6, 8) : b);
            buff.append(" ");
        }
        return buff.toString();
    }

    /**
     * 对指定字符串用零补齐指定位数的空缺位
     *
     * @param source
     * @param size
     * @return
     */
    public static String fixHexStringOffset(String source, int size) {
        return fixHexStringOffset22(source, size, true);
    }

    /**
     * 对指定字符串用零补齐指定位数的空缺位
     *
     * @param source    :要补零的字符串 <br>
     * @param size      ： 要补足的位数 <br>
     * @param addHeader :true表示在头部补零,false表示在尾部补零 <br>
     * @return
     */
    public static String fixHexStringOffset(String source, int size, boolean addHeader) {
        return fixHexStringOffset22(source, size, addHeader);
    }

    /**
     * 用零补齐16进制的空缺位
     *
     * @param source
     * @param size
     * @param addHeader
     * @return
     */
    private static String fixHexStringOffset22(String source, int size, boolean addHeader) {
        if (size % 2 != 0) {
            return source;
        }

        if (source.length() < size) {
            if (addHeader) {
                int offset = size - source.length();
                for (int i = 0; i < offset; i++) {
                    source = new StringBuilder().append("0").append(source).toString();
                }
            } else {
                for (int i = source.length(); i < size; i++) {
                    source = new StringBuilder().append(source).append("0").toString();
                }
            }
        } else if (source.length() > size) {
            int offset = source.length() - size;
            source = source.substring(offset, source.length());
        }
        return source;
    }

    public static String string2hexstring(String temId) {
        return string2hexstring("GBK", temId);
    }

    public static String string2hexstring(String encoding, String temId) {
        try {
            return bytes2hexstring(temId.getBytes(encoding));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String long2hexstring(long val, int size) {
        return fixHexStringOffset(Long.toHexString(val), size);
    }

    public static String string2hexstring(String val, int size) {
        return string2hexstring(val, size, true);
    }

    public static String string2hexstring(String val, int size, boolean addHeader) {
        return fixHexStringOffset22(string2hexstring(val), size, addHeader);
    }

    public static String hexstring2string(String val) {
        return hexstring2string(val, "GBK");
    }

    public static String hexstring2string(String val, String encoding) {
        try {
            return new String(hexstring2bytes(val), encoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String bytes2string(byte[] bytes) {
        try {
            return new String(bytes, "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String bytes2string(byte[] bytes, String encoding) {
        try {
            return new String(bytes, encoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String bytes2string(byte[] src, int start, int size) {
        return bytes2string(src, start, size, "GBK").trim();
    }

    public static String bytes2string(byte[] src, int start, int size, String encoding) {
        byte[] bytes = getChildArray(src, start, size);
        return bytes2string(bytes, encoding);
    }

    /**
     * 公厕氨气值解析
     *
     * @param byteArr
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2019年1月17日 上午10:26:09
     */
    public static BigDecimal getAmmonia(byte[] byteArr) {
        int iAmmonia = bytes2int(byteArr, 0, byteArr.length);
        BigDecimal bAmmonia = BigDecimal.valueOf(iAmmonia);
        return bAmmonia.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 将字节数组转化成int值
     *
     * @param byteArr
     * @return
     */
    public static int bytes2int(byte[] byteArr) {
        return bytes2int(byteArr, 0, byteArr.length);
    }

    public static Long hexstring2long(String hex) {
        return Long.valueOf(Long.parseLong(hex, 16));
    }

    public static int bit2int(String bits) {
        return Integer.parseInt(bits, 2);
    }

    /**
     * 合并两个字节数组，将第二个字节数组尾部追加到第一个字节数组中 <br>
     *
     * @param bts1 : 第一个字节数组 <br>
     * @param bts2 : 第二个字节数组 <br>
     * @return
     */
    public static byte[] add(byte[] bts1, byte[] bts2) {
        byte[] last = new byte[bts1.length + bts2.length];
        int i = 0;
        for (byte bt : bts1) {
            last[i] = bt;
            i++;
        }
        for (byte bt : bts2) {
            last[i] = bt;
            i++;
        }
        return last;
    }

    public static byte[] add(byte[] bts1, byte bt) {
        byte[] last = new byte[bts1.length + 1];
        int i = 0;
        for (byte bts : bts1) {
            last[i] = bts;
            i++;
        }
        last[i] = bt;
        return last;
    }

    public static int crc16(byte[] bytes) {
        int[] table = {0, 49345, 49537, 320, 49921, 960, 640, 49729, 50689, 1728, 1920, 51009,
                1280, 50625, 50305, 1088, 52225, 3264, 3456, 52545, 3840, 53185, 52865, 3648, 2560,
                51905, 52097, 2880, 51457, 2496, 2176, 51265, 55297, 6336, 6528, 55617, 6912, 56257,
                55937, 6720, 7680, 57025, 57217, 8000, 56577, 7616, 7296, 56385, 5120, 54465, 54657,
                5440, 55041, 6080, 5760, 54849, 53761, 4800, 4992, 54081, 4352, 53697, 53377, 4160,
                61441, 12480, 12672, 61761, 13056, 62401, 62081, 12864, 13824, 63169, 63361, 14144,
                62721, 13760, 13440, 62529, 15360, 64705, 64897, 15680, 65281, 16320, 16000, 65089,
                64001, 15040, 15232, 64321, 14592, 63937, 63617, 14400, 10240, 59585, 59777, 10560,
                60161, 11200, 10880, 59969, 60929, 11968, 12160, 61249, 11520, 60865, 60545, 11328,
                58369, 9408, 9600, 58689, 9984, 59329, 59009, 9792, 8704, 58049, 58241, 9024, 57601,
                8640, 8320, 57409, 40961, 24768, 24960, 41281, 25344, 41921, 41601, 25152, 26112,
                42689, 42881, 26432, 42241, 26048, 25728, 42049, 27648, 44225, 44417, 27968, 44801,
                28608, 28288, 44609, 43521, 27328, 27520, 43841, 26880, 43457, 43137, 26688, 30720,
                47297, 47489, 31040, 47873, 31680, 31360, 47681, 48641, 32448, 32640, 48961, 32000,
                48577, 48257, 31808, 46081, 29888, 30080, 46401, 30464, 47041, 46721, 30272, 29184,
                45761, 45953, 29504, 45313, 29120, 28800, 45121, 20480, 37057, 37249, 20800, 37633,
                21440, 21120, 37441, 38401, 22208, 22400, 38721, 21760, 38337, 38017, 21568, 39937,
                23744, 23936, 40257, 24320, 40897, 40577, 24128, 23040, 39617, 39809, 23360, 39169,
                22976, 22656, 38977, 34817, 18624, 18816, 35137, 19200, 35777, 35457, 19008, 19968,
                36545, 36737, 20288, 36097, 19904, 19584, 35905, 17408, 33985, 34177, 17728, 34561,
                18368, 18048, 34369, 33281, 17088, 17280, 33601, 16640, 33217, 32897, 16448};

        int crc = 0;
        for (byte b : bytes) {
            crc = crc >>> 8 ^ table[((crc ^ b) & 0xFF)];
        }

        return crc;
    }

    public int getUnsignedByte(byte data) {
        return data & 0xFF;
    }

    public int getUnsignedByte(short data) {
        return data & 0xFFFF;
    }

    public long getUnsignedIntt(int data) {
        return data & 0xFFFFFFFF;
    }

    private static int getCRC(int CRC, int length) {
        String hex = int2hexstring(CRC);
        if (hex.length() > length * 2) {
            hex = hex.substring(hex.length() - length * 2);
        }
        CRC = hexstring2int(hex).intValue();
        return CRC;
    }

    public static int[] bytes2ints(byte[] data) {
        int[] ints = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            ints[i] = bytes2int(data, i, 1);
        }
        return ints;
    }

    public static byte[] ints2bytes(int[] data) {
        byte[] bytes = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            bytes[i] = int2byteArray(data[i], 1)[0];
        }
        return bytes;
    }

    public static int CRC16Caculate(int Para, byte[] AP) {
        int[] CRCTable = {0, 4129, 8258, 12387, 16516, 20645, 24774, 28903, 33032, 37161, 41290,
                45419, 49548, 53677, 57806, 61935};

        int CRC = 65535 - Para;
        int CRC_H4 = 0;

        for (int i = 0; i < AP.length; i++) {
            byte[] wordAp = new byte[2];
            wordAp[0] = 0;
            wordAp[1] = AP[i];
            CRC_H4 = CRC >> 12;
            CRC = getCRC(CRC << 4, 2);
            CRC ^= CRCTable[(CRC_H4 ^ bytes2int(wordAp) >> 4)];
            CRC = getCRC(CRC, 2);
            CRC_H4 = CRC >> 12;
            CRC = getCRC(CRC << 4, 2);
            CRC ^= CRCTable[(CRC_H4 ^ bytes2int(wordAp) & 0xF)];
        }
        return CRC;
    }

    public static int[] decoding2(int[] DataAddr, int key) {
        int[][] S0 = {{2, 0, 1, 3}, {3, 0, 1, 2}, {2, 1, 0, 3}, {1, 2, 1, 3}};

        int[][] S1 = {{1, 0, 3, 2}, {3, 1, 0, 2}, {0, 2, 1, 3}, {3, 1, 2, 0}};

        int KEY = key & 0x3FF;

        int K2 = (KEY << 5 & 0x80) + (KEY >> 1 & 0x50) + (KEY << 1 & 0x20) + (KEY << 3 & 0x8)
                + (KEY >> 6 & 0x4) + (KEY & 0x2) + (KEY >> 9 & 0x1);

        int K1 = (KEY >> 2 & 0x90) + (KEY << 3 & 0x40) + (KEY << 4 & 0x20) + (KEY << 1 & 0xA)
                + (KEY >> 5 & 0x4) + (KEY >> 4 & 0x1);

        for (int T = 0; T < DataAddr.length; T++) {
            int A = DataAddr[T];

            int AL = (A >> 3 & 0xC) + (A >> 1 & 0x2) + (A >> 7 & 0x1);
            int AR = (A << 2 & 0xC) + (A >> 2 & 0x2) + (A >> 4 & 0x1);

            int ARK = (AR << 7 & 0x80) + (AR << 3 & 0x70) + (AR << 1 & 0xE) + (AR >> 3 & 0x1);

            ARK ^= K1;

            int Si = (ARK >> 6 & 0x2) + (ARK >> 4 & 0x1);
            int Sj = ARK >> 5 & 0x3;
            int SOut = S0[Si][Sj] * 4;
            Si = (ARK >> 2 & 0x2) + (ARK & 0x1);
            Sj = ARK >> 1 & 0x3;
            SOut += S1[Si][Sj];

            SOut = (SOut << 1 & 0x8) + (SOut << 2 & 0x4) + (SOut & 0x2) + (SOut >> 3 & 0x1);

            A = (AL ^ SOut) & 0xF;
            AL = AR;
            AR = A;

            ARK = (AR << 7 & 0x80) + (AR << 3 & 0x70) + (AR << 1 & 0xE) + (AR >> 3 & 0x1);

            ARK ^= K2;

            Si = (ARK >> 6 & 0x2) + (ARK >> 4 & 0x1);
            Sj = ARK >> 5 & 0x3;
            SOut = S0[Si][Sj] * 4;
            Si = (ARK >> 2 & 0x2) + (ARK & 0x1);
            Sj = ARK >> 1 & 0x3;
            SOut += S1[Si][Sj];

            SOut = (SOut << 1 & 0x8) + (SOut << 2 & 0x4) + (SOut & 0x2) + (SOut >> 3 & 0x1);

            A = ((AL ^ SOut) & 0xF) * 16 + AR;

            A = (A << 3 & 0x80) + (A >> 1 & 0x60) + (A << 4 & 0x10) + (A << 2 & 0x8)
                    + (A >> 3 & 0x4) + (A >> 2 & 0x3);

            DataAddr[(T++)] = A;
        }

        return DataAddr;
    }

    public static int[] decoding(int[] DataAddr, int key) {
        int[][] S0 = {{2, 0, 1, 3}, {3, 0, 1, 2}, {2, 1, 0, 3}, {1, 2, 1, 3}};

        int[][] S1 = {{1, 0, 3, 2}, {3, 1, 0, 2}, {0, 2, 1, 3}, {3, 1, 2, 0}};

        int KEY = key & 0x3FF;

        int K1 = (KEY << 5 & 0x80) + (KEY >> 1 & 0x50) + (KEY << 1 & 0x20) + (KEY << 3 & 0x8)
                + (KEY >> 6 & 0x4) + (KEY & 0x2) + (KEY >> 9 & 0x1);

        int K2 = (KEY >> 2 & 0x90) + (KEY << 3 & 0x40) + (KEY << 4 & 0x20) + (KEY << 1 & 0xA)
                + (KEY >> 5 & 0x4) + (KEY >> 4 & 0x1);

        for (int T = 0; T < DataAddr.length; T++) {
            int A = DataAddr[T];

            int AL = (A >> 3 & 0xC) + (A >> 1 & 0x2) + (A >> 7 & 0x1);
            int AR = (A << 2 & 0xC) + (A >> 2 & 0x2) + (A >> 4 & 0x1);

            int ARK = (AR << 7 & 0x80) + (AR << 3 & 0x70) + (AR << 1 & 0xE) + (AR >> 3 & 0x1);

            ARK ^= K1;

            int Si = (ARK >> 6 & 0x2) + (ARK >> 4 & 0x1);
            int Sj = ARK >> 5 & 0x3;
            int SOut = S0[Si][Sj] * 4;
            Si = (ARK >> 2 & 0x2) + (ARK & 0x1);
            Sj = ARK >> 1 & 0x3;
            SOut += S1[Si][Sj];

            SOut = (SOut << 1 & 0x8) + (SOut << 2 & 0x4) + (SOut & 0x2) + (SOut >> 3 & 0x1);

            A = (AL ^ SOut) & 0xF;
            AL = AR;
            AR = A;

            ARK = (AR << 7 & 0x80) + (AR << 3 & 0x70) + (AR << 1 & 0xE) + (AR >> 3 & 0x1);

            ARK ^= K2;

            Si = (ARK >> 6 & 0x2) + (ARK >> 4 & 0x1);
            Sj = ARK >> 5 & 0x3;
            SOut = S0[Si][Sj] * 4;
            Si = (ARK >> 2 & 0x2) + (ARK & 0x1);
            Sj = ARK >> 1 & 0x3;
            SOut += S1[Si][Sj];

            SOut = (SOut << 1 & 0x8) + (SOut << 2 & 0x4) + (SOut & 0x2) + (SOut >> 3 & 0x1);

            A = ((AL ^ SOut) & 0xF) * 16 + AR;

            A = (A << 3 & 0x80) + (A >> 1 & 0x60) + (A << 4 & 0x10) + (A << 2 & 0x8)
                    + (A >> 3 & 0x4) + (A >> 2 & 0x3);

            DataAddr[T] = A;
        }

        return DataAddr;
    }

    public static void testV2encrypt() {
        byte[] bytes = hexstring2bytes(
                "cd9edbb75dae2c4d8a3e2cdb76cc9ec92c155d0a442c442cf13a2c2c2c781727c25d26209c9c9c9c");

        byte[] tidParam = hexstring2bytes("02c10a3b");
        // System.out.println(new StringBuilder().append("加密tid:")
        // .append(bytes2hexstring(tidParam, 0, 4)).toString());

        int crcParam = 641;

        int idKey = CRC16Caculate(crcParam, tidParam);
        // System.out.println(new
        // StringBuilder().append("加密IDCRC:").append(int2hexstring(idKey, 4))
        // .toString());
        int cmd = 153;
        // System.out.println(new
        // StringBuilder().append("加密cmd:").append(int2hexstring(cmd, 4))
        // .toString());
        idKey += cmd;
        // System.out.println(new
        // StringBuilder().append("加密KEY:").append(int2hexstring(idKey, 4))
        // .toString());
        byte[] newBody = v2Encryption(bytes, idKey, 1, cmd);

        // System.out.println(bytes2hexstring(newBody));
    }

    public static void testV809encrypt() {
        byte[] DataAddr = hexstring2bytes("16dfd966661909");

        byte[] teid = hexstring2bytes("02c10b29");
        int crcParam = 641;
        int idKey = CRC16Caculate(crcParam, teid);

        int cmd = 144;

        idKey = idKey + cmd >> 4;

        byte[] result = v2Encryption(DataAddr, idKey, 1, cmd);

        int key = CRC16Caculate(crcParam, teid);
        key = key + cmd >> 4;
        byte[] result2 = v2Encryption(result, key, 0, cmd);
    }

    public static int xor(byte[] bytes) {
        int x = bytes[0];
        int length = bytes.length;
        for (int i = 1; i < length; i++) {
            x ^= bytes[i];
        }
        return x;
    }

    public static byte[] v2Encryption(byte[] DataAddr, int key, int mode, int cmd) {
        if (153 == cmd) {
            return v2Encryption1(DataAddr, key, mode, cmd);
        }
        return v2Encryption0(DataAddr, key, mode, cmd);
    }

    public static byte[] v2Encryption0(byte[] DataAddr, int key, int mode, int cmd) {
        int[][] S0 = {{3, 2, 1, 0}, {2, 0, 1, 3}, {3, 0, 1, 2}, {2, 1, 0, 3}};

        int[][] S1 = {{1, 0, 3, 2}, {0, 1, 2, 3}, {3, 1, 3, 2}, {0, 2, 1, 3}};

        int KEY = key & 0x3FF;

        int K1 = (KEY >> 2 & 0x90) + (KEY << 3 & 0x40) + (KEY << 4 & 0x20) + (KEY << 1 & 0xA)
                + (KEY >> 5 & 0x4) + (KEY >> 4 & 0x1);

        int K2 = (KEY << 5 & 0x80) + (KEY >> 1 & 0x50) + (KEY << 1 & 0x20) + (KEY << 3 & 0x8)
                + (KEY >> 6 & 0x4) + (KEY & 0x2) + (KEY >> 9 & 0x1);

        if (mode == 1) {
            int AL = K1;
            K1 = K2;
            K2 = AL;
        }

        for (int T = 0; T < DataAddr.length; T++) {
            int A = DataAddr[T];

            int AL = (A >> 3 & 0xC) + (A >> 1 & 0x2) + (A >> 7 & 0x1);
            int AR = (A << 2 & 0xC) + (A >> 2 & 0x2) + (A >> 4 & 0x1);

            int ARK = (AR << 7 & 0x80) + (AR << 3 & 0x70) + (AR << 1 & 0xE) + (AR >> 3 & 0x1);

            ARK ^= K1;

            int Si = (ARK >> 6 & 0x2) + (ARK >> 4 & 0x1);
            int Sj = ARK >> 5 & 0x3;
            int SOut = S0[Si][Sj] * 4;
            Si = (ARK >> 2 & 0x2) + (ARK & 0x1);
            Sj = ARK >> 1 & 0x3;
            SOut += S1[Si][Sj];

            SOut = (SOut << 1 & 0x8) + (SOut << 2 & 0x4) + (SOut & 0x2) + (SOut >> 3 & 0x1);

            A = (AL ^ SOut) & 0xF;
            AL = AR;
            AR = A;

            ARK = (AR << 7 & 0x80) + (AR << 3 & 0x70) + (AR << 1 & 0xE) + (AR >> 3 & 0x1);

            ARK ^= K2;

            Si = (ARK >> 6 & 0x2) + (ARK >> 4 & 0x1);
            Sj = ARK >> 5 & 0x3;
            SOut = S0[Si][Sj] * 4;
            Si = (ARK >> 2 & 0x2) + (ARK & 0x1);
            Sj = ARK >> 1 & 0x3;
            SOut += S1[Si][Sj];

            SOut = (SOut << 1 & 0x8) + (SOut << 2 & 0x4) + (SOut & 0x2) + (SOut >> 3 & 0x1);

            A = ((AL ^ SOut) & 0xF) * 16 + AR;

            A = (A << 3 & 0x80) + (A >> 1 & 0x60) + (A << 4 & 0x10) + (A << 2 & 0x8)
                    + (A >> 3 & 0x4) + (A >> 2 & 0x3);

            byte[] a = int2byteArray(A, 1);
            DataAddr[T] = a[0];
        }

        return DataAddr;
    }

    public static byte[] v2Encryption1(byte[] DataAddr, int key, int mode, int cmd) {
        int[][] S0 = {{0, 1, 2, 3}, {2, 0, 1, 3}, {1, 0, 3, 2}, {2, 1, 0, 3}};

        int[][] S1 = {{3, 2, 1, 0}, {1, 0, 2, 3}, {3, 1, 3, 2}, {0, 2, 1, 3}};

        int KEY = key & 0x3FF;

        int K1 = (KEY >> 2 & 0x90) + (KEY << 3 & 0x40) + (KEY << 4 & 0x20) + (KEY << 1 & 0xA)
                + (KEY >> 5 & 0x4) + (KEY >> 4 & 0x1);

        int K2 = (KEY << 5 & 0x80) + (KEY >> 1 & 0x50) + (KEY << 1 & 0x20) + (KEY << 3 & 0x8)
                + (KEY >> 6 & 0x4) + (KEY & 0x2) + (KEY >> 9 & 0x1);

        if (mode == 1) {
            int AL = K1;
            K1 = K2;
            K2 = AL;
        }

        for (int T = 0; T < DataAddr.length; T++) {
            int A = DataAddr[T];

            int AL = (A >> 3 & 0xC) + (A >> 1 & 0x2) + (A >> 7 & 0x1);
            int AR = (A << 2 & 0xC) + (A >> 2 & 0x2) + (A >> 4 & 0x1);

            int ARK = (AR << 7 & 0x80) + (AR << 3 & 0x70) + (AR << 1 & 0xE) + (AR >> 3 & 0x1);

            ARK ^= K1;

            int Si = (ARK >> 6 & 0x2) + (ARK >> 4 & 0x1);
            int Sj = ARK >> 5 & 0x3;
            int SOut = S0[Si][Sj] * 4;
            Si = (ARK >> 2 & 0x2) + (ARK & 0x1);
            Sj = ARK >> 1 & 0x3;
            SOut += S1[Si][Sj];

            SOut = (SOut << 1 & 0x8) + (SOut << 2 & 0x4) + (SOut & 0x2) + (SOut >> 3 & 0x1);

            A = (AL ^ SOut) & 0xF;
            AL = AR;
            AR = A;

            ARK = (AR << 7 & 0x80) + (AR << 3 & 0x70) + (AR << 1 & 0xE) + (AR >> 3 & 0x1);

            ARK ^= K2;

            Si = (ARK >> 6 & 0x2) + (ARK >> 4 & 0x1);
            Sj = ARK >> 5 & 0x3;
            SOut = S0[Si][Sj] * 4;
            Si = (ARK >> 2 & 0x2) + (ARK & 0x1);
            Sj = ARK >> 1 & 0x3;
            SOut += S1[Si][Sj];

            SOut = (SOut << 1 & 0x8) + (SOut << 2 & 0x4) + (SOut & 0x2) + (SOut >> 3 & 0x1);

            A = ((AL ^ SOut) & 0xF) * 16 + AR;

            A = (A << 3 & 0x80) + (A >> 1 & 0x60) + (A << 4 & 0x10) + (A << 2 & 0x8)
                    + (A >> 3 & 0x4) + (A >> 2 & 0x3);

            byte[] a = int2byteArray(A, 1);
            DataAddr[T] = a[0];
        }

        return DataAddr;
    }

    public static byte[] encryption(byte[] body, String temId, String msgId) {
        byte[] tidParam = hexstring2bytes(temId);

        int crcParam = 641;

        int idKey = CRC16Caculate(crcParam, tidParam);
        int cmd = hexstring2int(msgId).intValue();
        byte[] newBody = v2Encryption(body, idKey, 0, cmd);
        return newBody;
    }

    /**
     * String数字转换为BCD码
     *
     * @param str10
     * @return
     */
    public static byte[] str2Bcd(String str10) {
        int len = str10.length();
        int mod = len % 2;
        if (mod != 0) {
            str10 = "0" + str10;
            len = str10.length();
        }
        byte[] abt = new byte[len];
        if (len >= 2) {
            len /= 2;
        }
        byte[] bbt = new byte[len];
        abt = str10.getBytes();

        for (int p = 0; p < str10.length() / 2; p++) {
            int j;
            if ((abt[(2 * p)] >= 48) && (abt[(2 * p)] <= 57)) {
                j = abt[(2 * p)] - 48;
            } else {
                if ((abt[(2 * p)] >= 97) && (abt[(2 * p)] <= 122)) {
                    j = abt[(2 * p)] - 97 + 10;
                } else {
                    j = abt[(2 * p)] - 65 + 10;
                }
            }
            int k;
            if ((abt[(2 * p + 1)] >= 48) && (abt[(2 * p + 1)] <= 57)) {
                k = abt[(2 * p + 1)] - 48;
            } else {
                if ((abt[(2 * p + 1)] >= 97) && (abt[(2 * p + 1)] <= 122)) {
                    k = abt[(2 * p + 1)] - 97 + 10;
                } else {
                    k = abt[(2 * p + 1)] - 65 + 10;
                }
            }
            int a = (j << 4) + k;
            byte b = (byte) a;
            bbt[p] = b;
        }
        return bbt;
    }

    public static String byte2fomartDateString(byte[] arg) throws Exception {
        String dateString = bytes2string(arg);
        DateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        Date date = sdf.parse(dateString);
        return DateHelper.formatDate(date);
    }

    public static String simple() {
        return "simpleMethod!";
    }

    /**
     * 不要用，该方法解析存在问题。建议用ObdProtocolConvert.transformationLng_new
     *
     * @param arg
     * @return String 经度
     * @description: TODO (将一个字节数组转换为经度，用Integer类型表示，是真实经度乘以一百万之后的数，纬度转换例如：
     * 19度59.5945分,因为地图只认度，所以需要把分转成度，即59.5945/60 = 0.99324，
     * 所以最后得到19.99324度)
     * @author: 霍思博
     * @version: 2.0
     * @date: 2017 2017年11月21日 下午7:50:29
     */
    public static Double lngTransformationForOBD(byte[] arg) {
        String lngStr = bytes2hexstring(arg);
        lngStr = (String) lngStr.subSequence(0, 9);
        Double lng = ObdProtocolConvert.transformationLng(lngStr);
        return lng;

    }

    /**
     * 不要用，该方法解析存在问题。建议用ObdProtocolConvert.transformationLat_new
     *
     * @param arg
     * @return
     * @description: TODO (将一个字节数组转换为纬度，用Integer类型表示，是真实经度乘以一百万之后的数,经度转换，
     * 例如：110度18.0524分，和纬度一样的转化方式，18.0524/60 =
     * 0.3008733，最后得到110.3008733度)
     * @author:
     * @version: 2.0
     * @date: 2017 2017年11月21日 下午7:58:07
     */
    public static Double latTransformationForOBD(byte[] arg) {
        Double lat = ObdProtocolConvert.transformationLat(bytes2hexstring(arg));
        return lat;

    }

    /**
     * 获取OBD瞬时油耗
     *
     * @param arg
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: huosibo@edenep.net
     * @version: 2.0
     * @date: 2018年3月1日 下午4:35:15
     */
    public static Integer getObdDynAmicalFuel(byte[] arg) {
        int byte1 = bytes2int(arg, 0, 1);
        int byte2 = bytes2int(arg, 1, 1);
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        String num = df.format((float) (byte1 * 256 + byte2) / 10);//返回的是String类型
        double dynAmicalFuel = Double.valueOf(num);
        return (int) Math.round(dynAmicalFuel);

    }

    /**
     * @param arg
     * @return String
     * @description: TODO (将字节数组转换为开关量，比如全清扫、左清洗、右清洗)
     * @author: 霍思博
     * @version: 2.0
     * @date: 2017 2017年11月21日 下午8:17:50
     */
    public static Integer byte2WorkStartType(byte[] arg) {
        String workStartType = byte2bitstring(arg[0]) + byte2bitstring(arg[1]).toCharArray()[0];
        return workStartType.indexOf("1");
    }

    /**
     * @param arg
     * @return Integer 经度
     * @description: TODO (根据卡片机的数据转换经度)
     * @author: 霍思博
     * @version: 2.0
     * @date: 2017 2017年11月21日 下午8:26:28
     */
    public static Double lngTransformationForCard(byte[] arg) {
        String info = bytes2string(arg);
        Double lng = (Double.parseDouble(info.substring(0, 3)) + ObdProtocolConvert
                .div(Double.parseDouble(info.substring(3, info.length())), 60D, 10));
        return lng;
    }

    /**
     * @param arg
     * @return Integer 纬度
     * @description: TODO (根据卡片机的数据转换纬度)
     * @author: 霍思博
     * @version: 2.0
     * @date: 2017 2017年11月21日 下午8:28:22
     */
    public static Double latTransformationForCard(byte[] arg) {
        String info = bytes2string(arg);
        Double lat = (Double.parseDouble(info.substring(0, 3)) + ObdProtocolConvert
                .div(Double.parseDouble(info.substring(3, info.length())), 60D, 10));
        return lat;
    }

    /**
     * 得到冷却液温度
     *
     * @param arg
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: huosibo@edenep.net
     * @version: 2.0
     * @date: 2018年3月1日 上午11:41:49
     */
    public static int getCoolantTemperature(byte[] arg) {
        int coolant = bytes2int(arg);
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
        int coolant = bytes2int(arg);
        return coolant * 4;
    }

    /**
     * 得到智能盒子的里程数据
     *
     * @param arg
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: huosibo@edenep.net
     * @version: 2.0
     * @date: 2018年3月1日 下午1:59:20
     */
    public static int getSmartBoxGpsMileage(byte[] arg) {
        byte[] bytes = new byte[]{arg[3], arg[2], arg[1], arg[0]};
        int mileage = bytes2int(bytes, 0, 4);
        return mileage / 8;
    }

    /**
     * 得到发动机运行时间
     *
     * @param arg
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: huosibo@edenep.net
     * @version: 2.0
     * @date: 2018年3月1日 下午2:03:33
     */
    public static int getRunningTime(byte[] arg) {
        byte[] bytes = new byte[]{arg[3], arg[2], arg[1], arg[0]};
        int time = bytes2int(bytes, 0, 4);
        return (time / 20) * 60;
    }

    /**
     * 得到总消耗燃油
     *
     * @param arg
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: huosibo@edenep.net
     * @version: 2.0
     * @date: 2018年3月1日 下午2:05:30
     */
    public static Double getTotalBurnOff(byte[] arg) {
        byte[] bytes = new byte[]{arg[3], arg[2], arg[1], arg[0]};
        int totalBurnOff = bytes2int(bytes, 0, 4);
        return (double) totalBurnOff / 2;
    }

    /**
     * 得到OBD与智能盒子的上传时间，格林威治时间加8小时
     *
     * @param arg
     * @return
     * @throws Exception
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: huosibo@edenep.net
     * @version: 2.0
     * @date: 2018年3月1日 下午2:10:09
     */
    public static String byte2DateString(byte[] arg) throws Exception {
        String dateString = bytes2hexstring(arg);
        DateFormat sdf = new SimpleDateFormat("ddMMyyHHmmss");
        Date date = sdf.parse(dateString);
        if (null != date) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            calendar.add(Calendar.HOUR, 8);
            date = calendar.getTime();
        }
        return DateHelper.formatTime(date);
    }

    /**
     * 得到燃油消耗率(L/H)
     *
     * @param arg
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: huosibo@edenep.net
     * @version: 2.0
     * @date: 2018年3月1日 下午2:13:47
     */
    public static Double getFuelConsumptionRate(byte[] arg) {
        byte[] bytes = new byte[]{arg[1], arg[0]};
        int fuelConsumptionRate = bytes2int(bytes, 0, 2);
        return (double) fuelConsumptionRate / 20;
    }

    /**
     * 得到燃油经济性
     *
     * @param arg
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: huosibo@edenep.net
     * @version: 2.0
     * @date: 2018年3月1日 下午2:16:56
     */
    public static Double getFuelEconomy(byte[] arg) {
        byte[] bytes = new byte[]{arg[1], arg[0]};
        int fuelEconomy = bytes2int(bytes, 0, 2);
        return (double) fuelEconomy * 0.001953125;
    }

    /**
     * 得到发动机转速
     *
     * @param arg
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: huosibo@edenep.net
     * @version: 2.0
     * @date: 2018年3月1日 下午2:25:29
     */
    public static Double getRpm(byte[] arg) {
        byte[] bytes = new byte[]{arg[1], arg[0]};
        int fuelEconomy = bytes2int(bytes, 0, 2);
        return (double) fuelEconomy / 8;
    }

    /**
     * 获取OBD返回车速
     *
     * @param arg
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: huosibo@edenep.net
     * @version: 2.0
     * @date: 2018年3月1日 下午2:41:49
     */
    public static int getObdSpeed(byte[] arg) {
        byte[] bytes = new byte[]{arg[1], arg[0]};
        int obdSpeed = bytes2int(bytes, 0, 2);
        return obdSpeed / 256;
    }

    /**
     * 获取车辆蓄电池电压
     *
     * @param arg
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: huosibo@edenep.net
     * @version: 2.0
     * @date: 2018年3月1日 下午2:43:39
     */
    public static int getVoltage(byte[] arg) {
        byte[] bytes = new byte[]{arg[1], arg[0]};
        int voltage = bytes2int(bytes, 0, 2);
        return voltage / 20;
    }

    /**
     * 计算剩余油量
     *
     * @param arg
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: huosibo@edenep.net
     * @version: 2.0
     * @date: 2018年3月1日 下午2:58:00
     */
    public static int getFuel(byte[] arg) {
        int byte57 = bytes2int(arg, 0, 1);
        int byte58 = bytes2int(arg, 1, 1);
        int byte59 = bytes2int(arg, 2, 1);
        int byte60 = bytes2int(arg, 3, 1) * 20;
        int byte61 = bytes2int(arg, 4, 1) * 20;
        // 计算油量
        int a = byte57 * 256 + byte58;
        int b = Math.abs(byte60 - byte61);
        int c = Math.abs(a - byte61);
        double d = 0;
        if (b != 0) {
            d = scaleNum((double) c / b, 2);
        }
        double fuel1 = d * byte59;
        DecimalFormat df = new DecimalFormat("######0"); // 四舍五入转换成整数
        Integer fuel = Integer.parseInt(df.format(fuel1));
        return fuel;
    }

    /**
     * 保留小数位数
     *
     * @param d
     * @param scale 位数
     * @return
     */
    public static double scaleNum(double d, int scale) {
        BigDecimal b = new BigDecimal(d);
        return b.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 获取车辆工作启动类型
     *
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: huosibo@edenep.net
     * @version: 2.0
     * @date: 2018年3月1日 下午3:02:38
     */
    public static String getStartType(byte[] arg) {
        String workStartType = byte2bitstring(arg[0]) + byte2bitstring(arg[1]).toCharArray()[0];
        int startTypeNum = workStartType.indexOf("1");
        String startType = VehicleSweepingConditionEnum.getWorkStatus(startTypeNum);
        return startType;
    }

    /**
     * 获取卡片机速度
     *
     * @param arg
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: huosibo@edenep.net
     * @version: 2.0
     * @date: 2018年3月3日 上午11:59:31
     */
    public static Integer getSpeedByCard(byte[] arg) {
        String speed = bytes2string(arg);
        double sudu = Double.valueOf(speed);
        return (int) Math.round(sudu);
    }

    /**
     * 获取速度
     *
     * @param arg
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: huosibo@edenep.net
     * @version: 2.0
     * @date: 2018年3月3日 下午12:37:01
     */
    public static Integer getSpeed(byte[] arg) {
        return bytes2int(arg);
    }

    /**
     * 获取智能盒子GPS速度
     *
     * @param arg
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: huosibo@edenep.net
     * @version: 2.0
     * @date: 2018年3月3日 下午12:37:01
     */
    public static Integer getSpeedByBoxGps(byte[] arg) {
        return bytes2int(arg) / 2;
    }

    public static Integer getSpeedByGB32960(byte[] arg) {
        return (int) (bytes2int(arg) * 0.1);
    }

    /**
     * 获取方向
     *
     * @param arg
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: huosibo@edenep.net
     * @version: 2.0
     * @date: 2018年3月3日 下午12:37:48
     */
    public static Integer getDirection(byte[] arg) {
        return bytes2int(arg) / 2;
    }

    /**
     * 获取智能盒子方向
     *
     * @param arg
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: huosibo@edenep.net
     * @version: 2.0
     * @date: 2018年3月3日 下午12:37:48
     */
    public static Integer getDirectionByBox(byte[] arg) {
        return bytes2int(arg) * 2;
    }

    /**
     * 得到卡片机的上传时间
     *
     * @param arg
     * @return
     * @throws Exception
     * @description:
     * @author: huosibo@edenep.net
     * @version: 2.0
     * @date: 2018年3月1日 下午2:10:09
     */
    public static String byte2DateStringByCard(byte[] arg) throws Exception {
        String dateString = bytes2string(arg);
        DateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        Date date = sdf.parse(dateString);
        if (null != date) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            calendar.add(Calendar.HOUR, 8);
            date = calendar.getTime();
        }
        return DateHelper.formatTime(date);
    }

    /**
     * 得到APP的上传时间
     *
     * @param arg
     * @return
     * @throws Exception
     * @description:
     * @author: huosibo@edenep.net
     * @version: 2.0
     * @date: 2018年3月1日 下午2:10:09
     */
    public static String byte2DateStringByApp(byte[] arg) throws Exception {
        String dateString = bytes2string(arg);
        DateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = sdf.parse(dateString);
        return DateHelper.formatTime(date);
    }

    /**
     * 将一个字节数组转为int,先转10进制再转int
     *
     * @param arg
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: huosibo@edenep.net
     * @version: 2.0
     * @date: 2018年4月15日 下午11:10:43
     */
    public static int byte2Int(byte[] arg) {
        String str = bytes2string(arg);
        double dou = Double.valueOf(str);
        return (int) Math.round(dou);
    }

    /**
     * 字符数组截取
     *
     * @param src   要截取的数组
     * @param begin 开始位置
     * @param count 截取几位
     * @return
     */
    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        if (src.length < begin + count) {
            System.arraycopy(src, begin, bs, 0, src.length - begin);
        } else {
            System.arraycopy(src, begin, bs, 0, count);
        }
        return bs;
    }

    /**
     * 累加byte数组 a+b
     *
     * @param a 前面
     * @param b 后面
     * @return
     */
    public static byte[] AddBytes(byte[]... params) {
        int byteLength = 0;
        for (int i = 0; i < params.length; i++) {
            byteLength += params[i].length;
        }
        byte[] c = new byte[byteLength];
        int addLength = 0;
        for (int i = 0; i < params.length; i++) {
            System.arraycopy(params[i], 0, c, addLength, params[i].length);
            addLength += params[i].length;
        }
        return c;
    }

    /**
     * 依次累加byte到byte[]数组后面
     *
     * @param src
     * @param params
     * @return
     */
    public static byte[] AddBytes(byte[] src, byte... params) {
        if (params != null) {
            byte[] newByte = new byte[src.length + params.length];
            for (int i = 0; i < src.length; i++) {
                newByte[i] = src[i];
            }
            for (int i = src.length, j = 0; j < params.length; i++, j++) {
                newByte[i] = params[j];
            }
            return newByte;
        } else {
            return src;
        }
    }

    /**
     * 字节转为无符号整形 求和
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018年7月19日 上午10:51:54
     */
    public static int intSum(byte[] bytes) {
        int a = 0;
        // 累加求和
        for (int i = 0; i < bytes.length; i++) {
            a += bytes[i] & 0xff;
        }
        return a;
    }


    /**
     * 字节数组求和 校验位 占一位
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zl
     * @version: 2.0
     * @date: 2018年5月25日 下午5:48:07
     */
    public static int byteSum(byte[] bytes) {
        byte a = 0;
        // 累加求和
        for (int i = 0; i < bytes.length; i++) {
            a += bytes[i];
        }
        return a;
    }

    /**
     * @param a
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author:
     * @version: 2.0
     * @date: 2018年7月12日 下午5:01:38
     */
    public static byte[] intToByteArray(int n) {
        byte[] b = new byte[1];
        b[0] = (byte) (n & 0xff);
        return b;
    }

    public static byte[] int2Bytes(int value, int len) {
        byte[] b = new byte[len];
        for (int i = 0; i < len; i++) {
            b[len - i - 1] = (byte) ((value >> 8 * i) & 0xff);
        }
        return b;
    }

    /**
     * 将字节流转为字节数组
     *
     * @param input
     * @return
     * @throws IOException
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年7月19日 下午7:06:24
     */
    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }

    /**
     * 神州车载协议 纬度解析
     *
     * @param bytes
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zl
     * @version: 2.0
     * @date: 2018年8月11日 下午4:35:45
     */
    public static Double transformationLat(byte[] bytes) {
        Double lat = ObdProtocolConvert.div(Double.valueOf(byte2Int(bytes)), 1000000D, 10);
        return lat;
    }

    /**
     * 神州车载协议 经度解析
     *
     * @param bytes
     * @return
     * @author: zl
     * @version: 2.0
     * @date: 2018年8月11日 下午4:35:38
     */
    public static Double transformationLng(byte[] bytes) {
        Double lng = ObdProtocolConvert.div(Double.valueOf(byte2Int(bytes)), 1000000D, 10);
        return lng;
    }

    /**
     * 将字节数组转为hex字符串，然后格式化为日期字符串
     *
     * @param arg
     * @return
     * @throws Exception
     * @author: zl
     * @version: 2.0
     * @date: 2018 2018年8月11日 下午5:18:48
     */
    public static String byteFormatHexString2Date(byte[] arg) throws Exception {
        String dateString = bytes2hexstring(arg);
        DateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        Date date = sdf.parse(dateString);
        return DateHelper.formatTime(date);
    }

    /**
     * 将int转成指定长度的二进制字符串，位数不足的用0补足 <br>
     *
     * @return
     */
    public static String bytes2stringttx(byte[] bytes) {

        Integer val = byte2Int(bytes);
        StringBuffer sb = new StringBuffer();
        String result = Integer.toBinaryString(val);
        if (result.length() < 32) {
            for (int i = 0; i < 32 - result.length(); i++) {
                sb.append("0");
            }
        }
        sb.append(result);
        return sb.toString();
    }

    /**
     * 状态位1 20-23位 判断这四个开关的值
     *
     * @return
     */
    public static int ttxSwitchNumberFour(byte[] bytes) {

        String status1 = bytes2stringttx(bytes);
        StringBuffer sb = new StringBuffer(status1);
        char[] c = sb.reverse().toString().toCharArray();
        int switchCount = 0;
        for (int i = 0; i < c.length; i++) {
            if (i >= 20 && i <= 23 && c[i] == '1') {
                switchCount++;
            }
        }
        return switchCount;
    }

    /**
     * 状态位1 20-27位 判断这八个开关的值
     *
     * @return
     */
    public static int ttxSwitchNumberEight(byte[] bytes) {

        String status1 = bytes2stringttx(bytes);
        StringBuffer sb = new StringBuffer(status1);
        char[] c = sb.reverse().toString().toCharArray();
        int switchCount = 0;
        for (int i = 0; i < c.length; i++) {
            if (i >= 20 && i <= 27 && c[i] == '1') {
                switchCount++;
            }
        }
        return switchCount;
    }

    /**
     * 至少有一个开关打开,则认为开关量打开了在作业
     *
     * @return
     */
    public static int ttxFourSwitch1(byte[] bytes) {
        int switchCount = ttxSwitchNumberFour(bytes);
        if (switchCount >= 1) {
            return 1;
        }
        return 0;
    }

    /**
     * 至少两个开关打开,则认为开关量打开了在作业
     *
     * @return
     */
    public static int ttxFourSwitch2(byte[] bytes) {
        int switchCount = ttxSwitchNumberFour(bytes);
        if (switchCount >= 2) {
            return 1;
        }
        return 0;
    }

    /**
     * 至少有一个开关打开,则认为开关量打开了在作业
     *
     * @return
     */
    public static int ttxEightSwitch1(byte[] bytes) {
        int switchCount = ttxSwitchNumberEight(bytes);
        if (switchCount >= 1) {
            return 1;
        }
        return 0;
    }

    /**
     * 至少两个开关打开,则认为开关量打开了在作业
     *
     * @return
     */
    public static int ttxEightwitch2(byte[] bytes) {
        int switchCount = ttxSwitchNumberEight(bytes);
        if (switchCount >= 2) {
            return 1;
        }
        return 0;
    }

    public static Map<String, Object> vmcBaseValue(byte[] bytes) {
        String bodyStr = DefaultProtocolConvert.bytes2string(bytes);
        bodyStr = bodyStr.substring(bodyStr.indexOf("{"), bodyStr.lastIndexOf("}") + 1);
        return JSON.parseObject(bodyStr, Map.class);
    }

    public static String vmcData(byte[] bytes) {
        return JSON.toJSONString(vmcBaseValue(bytes));
    }

    public static String vmcMid(byte[] bytes) {
        return vmcBaseValue(bytes).get("Mid").toString();
    }

    public static String vmcImei(byte[] bytes) {
        String mid = vmcMid(bytes);
        return CollectRedisCacheService.getVmcImeiByMid(mid);
    }

    public static String vmcMaxSlot(byte[] bytes) {
        return vmcBaseValue(bytes).get("MaxSlot").toString();
    }

    public static String vmcSlotInfo(byte[] bytes) {

        List<Object> list = (List<Object>) vmcBaseValue(bytes).get("SlotInfo");
        return JSON.toJSONString(list);
    }

    public static String vmcTimesp(byte[] bytes) {
        long time = Long.parseLong(vmcBaseValue(bytes).get("TimeSp").toString());
        Assert.notNull(time, "time is null");
        DateTimeFormatter ftf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time * 1000L), ZoneId.systemDefault()));
    }

    public static String vmcOrderNo(byte[] bytes) {
        return vmcBaseValue(bytes).get("OrderNo").toString();
    }

    public static String vmcResult(byte[] bytes) {
        return vmcBaseValue(bytes).get("Resault").toString();
    }

    public static String vmcError(byte[] bytes) {
        return vmcBaseValue(bytes).get("Error").toString();
    }

    public static int byte2int(byte[] bytes) {
        int a = 0;
        // 累加求和
        for (int i = 0; i < bytes.length; i++) {
            a += bytes[i] & 0xff;
        }
        return a;
    }

    public static String getBluetoothScaleWeight(byte[] bytes) {
        String weight = "0";

        if (bytes.length > 30) {
            String weightHex;
            int lastIndex = bytes.length - 22 >= 9 ? 9 : bytes.length - 22;
            String sourceStr = DefaultProtocolConvert.bytes2string(bytes, 22, lastIndex, "GBK").trim();
            if (sourceStr.startsWith("+") || sourceStr.startsWith("-")) {
                weightHex = sourceStr.substring(1, sourceStr.length() - 1);
                weight = Double.parseDouble(new StringBuilder(weightHex).toString()) / 100 + "";
            }
        } else {
            String startStr = "202020";
            String twoStartStr = "2020";
            String endStr = "0d";
            String sourceStr = DefaultProtocolConvert.bytes2hexstring(bytes);

            if (sourceStr.contains(startStr) && sourceStr.contains(endStr)) {
                String weightHex = sourceStr.substring(sourceStr.indexOf(startStr) + 6, sourceStr.indexOf(endStr));
                weight = hexstring2string(weightHex);
            }
            if (sourceStr.contains(twoStartStr) && sourceStr.contains(endStr)) {
                String weightHex = sourceStr.substring(sourceStr.indexOf(twoStartStr) + 4, sourceStr.indexOf(endStr));
                weight = hexstring2string(weightHex);
            }
        }

        return weight;
    }

    public static String getBluetoothScaleImei(byte[] bytes) {
        String startStr = "3031";
        String endStr = "202020";
        String twoEndStr = "2020";
        String sourceStr = DefaultProtocolConvert.bytes2hexstring(bytes);
        String imei = "";
        if (sourceStr.contains(startStr) && sourceStr.contains(endStr)) {
            String weightHex = sourceStr.substring(sourceStr.indexOf(startStr) + 4, sourceStr.indexOf(endStr));
            imei = hexstring2string(weightHex).trim();
        }
        if (sourceStr.contains(startStr) && sourceStr.contains(twoEndStr)) {
            String weightHex = sourceStr.substring(sourceStr.indexOf(startStr) + 4, sourceStr.indexOf(twoEndStr));
            imei = hexstring2string(weightHex).trim();
        }
        return imei;
    }

    public static String getWeighbridgeImei(byte[] bytes) {

       /* String startStr = "3032";
        String endStr = "2e";
        String sourceStr = DefaultProtocolConvert.bytes2hexstring(bytes);
        String imei = "";
        if(sourceStr.contains(startStr) && sourceStr.contains(endStr)){
            String imeiHex = sourceStr.substring(sourceStr.indexOf(startStr)+4,sourceStr.indexOf(endStr));
            imei = hexstring2string(imeiHex).trim();
        }*/

        return bytes2string(bytes, 7, 15, "GBK").trim();
    }

    public static String getWeighbridgeWeightOld(byte[] bytes) {
        String startStr = "2e";
        String endStr = "3d";
        String sourceStr = DefaultProtocolConvert.bytes2hexstring(bytes);
        String weight = "0";
        if (sourceStr.contains(startStr) && sourceStr.contains(endStr)) {
            String weightHex = sourceStr.substring(sourceStr.lastIndexOf(endStr) - 16, sourceStr.lastIndexOf(endStr));
            String weightStr = hexstring2string(weightHex).trim();
            weight = Double.parseDouble(new StringBuilder(weightStr).reverse().toString()) + "";
        }
        return weight;
    }

    public static String getWeighbridgeWeight(byte[] bytes) {
        if (bytes.length <= 23) {
            return null;
        }
        int lastIndex = bytes.length - 22 >= 9 ? 9 : bytes.length - 22;
        String sourceStr = DefaultProtocolConvert.bytes2string(bytes, 22, lastIndex, "GBK").trim();
        String weightHex;
        String weight = null;
        if (sourceStr.startsWith("=")) {
            weightHex = sourceStr.substring(1, sourceStr.length());
            weight = Double.parseDouble(new StringBuilder(weightHex).reverse().toString()) + "";
        } else if (sourceStr.endsWith("=")) {
            weightHex = sourceStr.substring(0, sourceStr.length() - 1);
            weight = Double.parseDouble(new StringBuilder(weightHex).reverse().toString()) + "";
        } else if (sourceStr.startsWith("+") || sourceStr.startsWith("-")) {
            weightHex = sourceStr.substring(1, sourceStr.length() - 1);
            weight = Double.parseDouble(new StringBuilder(weightHex).toString()) + "";
        }
        return weight;
    }


    public static void main(String[] args) {
        String sourceStr = "77656967683031543633383636303436353537373733022b30303030333232313803";
//        sourceStr="776569676830325448533037393737333736303433323d3039323030303030";//weigh02THS079773760432=09200000
//        sourceStr="77656967683032544853303739373733373630343332022b30303030323530314303";
        System.out.println(getBluetoothScaleWeight(hexstring2bytes(sourceStr)));
        System.out.println(getWeighbridgeImei(hexstring2bytes(sourceStr)));
        System.out.println(getWeighbridgeWeight(hexstring2bytes(sourceStr)));
    }
}
