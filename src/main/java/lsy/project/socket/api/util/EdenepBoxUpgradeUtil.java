package lsy.project.socket.api.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Map;

import com.lsy.base.date.DateHelper;
import lsy.project.socket.api.cache.CollectRedisCacheService;
import lsy.project.socket.api.callback.YdBoxUpgradeCallBack;
import lsy.project.socket.api.convert.DefaultProtocolConvert;
import lsy.project.socket.api.dto.UpgradeDto;
import lsy.project.socket.api.model.CallbackMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;


/**
 * 易登硬件协议升级工具类
 *
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public class EdenepBoxUpgradeUtil {

    private static Logger logger = LoggerFactory.getLogger(YdBoxUpgradeCallBack.class);

    /**
     * 分包大小
     */
    private static final BigDecimal PACKAGE_SIZE = BigDecimal.valueOf(64);

    /**
     * 下发升级包
     *
     * @param filePath
     * @param imei
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年7月20日 下午5:57:58
     */
    public static void downPackage(String filePath, String imei) {

        Map<String, String> imeimap = NettyChannelMap.getImeiMap();
        if (null == imeimap || imeimap.isEmpty() || !imeimap.containsKey(imei)) {
            return;
        }

        //设备号对应的客户端通道ID
        String clientId = imeimap.get(imei);
        //客户端tcp通道
        Channel channel = NettyChannelMap.get(clientId);

        UpgradeDto boxUpgradeDto = CollectRedisCacheService.getUpgradeDto(imei);
        if (null == boxUpgradeDto) {
            logger.info(String.format(StandardConstantResp.ERROR_NOTFOUND_UPGRADECACHE.getMessage(), imei));
            return;
        }

        byte[] content = getFileContent(filePath, imei);
        if (null == content) {
            return;
        }

        //包数量
        BigDecimal packageNum = BigDecimal.valueOf(content.length).divide(PACKAGE_SIZE, 0,
                RoundingMode.CEILING);

        /**
         * 下发数据到盒子
         */
        CallbackMessage callbackMessage = createDownData(boxUpgradeDto, content, packageNum);
        channel.writeAndFlush(callbackMessage);


        //文件包总字节数
        boxUpgradeDto.setFileByteSize(content.length + "");

        //文件总字节求和
        boxUpgradeDto.setFileByteSum(DefaultProtocolConvert.intSum(content) + "");
        boxUpgradeDto.setPackageTotal(packageNum + "");

        //取文件中隐藏的版本号
        byte[] version_byte = DefaultProtocolConvert.subBytes(content, 1036, 4);

        String gversion = DefaultProtocolConvert.bytes2string(version_byte);
        boxUpgradeDto.setGversion(gversion);
        boxUpgradeDto.setCurrentPackageDownTime(DateHelper.formatDate(new Date(), DateHelper.PATTERN_TIME));
        //更新缓存
        CollectRedisCacheService.addServerPushUpgrade(boxUpgradeDto);

        /**
         * 打印升级进度日志
         */
        printUpgradeLog(boxUpgradeDto, callbackMessage);
    }

    /**
     * 组装最后一个升级包内容
     *
     * @param imei
     * @param upgradeDto
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018 2018年7月20日 下午4:15:30
     */
    public static byte[] createLastPackageContent(String imei, UpgradeDto upgradeDto) {

        //协议命定
        String sendCmd = upgradeDto.getSendLastPackageCmd() == null ? "82" : upgradeDto.getSendLastPackageCmd().toString();
        byte[] commond_byte = new byte[]{(byte) DefaultProtocolConvert.hexstring2int(sendCmd).intValue()};

        //IMEI
        byte[] imeibyte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.stringToASCII(imei, imei.length(), ""));
        if ("1".equals(upgradeDto.getImeiType())) {
            imeibyte = DefaultProtocolConvert.hexstring2bytes(imei);
        }

        //协议长度 13 为固定长度=帧头1+协议长度1+指令码1+文件大小4+文件总字节求和4+校验码1+帧尾1
        byte[] protocolLength_byte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(imeibyte.length + 13), 2));

        //文件大小
        String fileByteSize = upgradeDto.getFileByteSize();
        int iFileByteSize = Integer.parseInt(fileByteSize);
        String hexFileByteSize = DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(iFileByteSize), 8);
        byte[] fileSize_byte = DefaultProtocolConvert.hexstring2bytes(hexFileByteSize);

        //文件总字节求和
        String fileByteSum = upgradeDto.getFileByteSum();
        int iFileByteSum = Integer.parseInt(fileByteSum);
        String hexFileByteSum = DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(iFileByteSum), 8);
        byte[] fileByteSum_byte = DefaultProtocolConvert.hexstring2bytes(hexFileByteSum);

        byte[] dataByte = DefaultProtocolConvert.add(DefaultProtocolConvert.add(
                DefaultProtocolConvert.add(DefaultProtocolConvert
                        .add(protocolLength_byte, commond_byte), imeibyte),
                fileSize_byte), fileByteSum_byte);//拼接数据内容
        //协议校验码
        byte[] protocolVerify_byte = new byte[]
                {
                        (byte) DefaultProtocolConvert.byteSum(dataByte)//协议校验码
                };
        byte[] returnDataByte = DefaultProtocolConvert.add(dataByte, protocolVerify_byte);
        return returnDataByte;
    }

    /**
     * 打印升级进度日志
     *
     * @param imei
     * @param boxUpgradeDto
     * @param callbackMessage
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年7月20日 下午3:14:29
     */
    private static void printUpgradeLog(UpgradeDto boxUpgradeDto, CallbackMessage callbackMessage) {

        String imei = boxUpgradeDto.getImei();
        long current = System.currentTimeMillis();
        long lastPackageDownTime = boxUpgradeDto.getCurrentPackageDownTime() == null ? current : DateHelper.parseString(boxUpgradeDto.getCurrentPackageDownTime()).getTime();
        long interval = current - lastPackageDownTime;
        logger.info("[" + imei + "]升级分包下发,总包[" + boxUpgradeDto.getPackageTotal() + "]正在下发第[" + boxUpgradeDto.getCurrentDown() + "]个包,距上一包间隔[" + interval + "]=" + DefaultProtocolConvert.bytes2hexstring(callbackMessage.getHeaderMsg()) +
                DefaultProtocolConvert.bytes2hexstring(callbackMessage.getBodyMsg()) +
                DefaultProtocolConvert.bytes2hexstring(callbackMessage.getFooterMsg()) +
                " byte=" + callbackMessage.toString());
    }

    /**
     * 构建下发升级数据包
     *
     * @param boxUpgradeDto
     * @param content       升级文件总字节
     * @param packageNum    升级文件总包号
     * @return
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年7月20日 下午3:16:22
     */
    private static CallbackMessage createDownData(UpgradeDto boxUpgradeDto, byte[] content,
                                                  BigDecimal packageNum) {

        String imei = boxUpgradeDto.getImei();
        byte[] headByte = DefaultProtocolConvert.hexstring2bytes(boxUpgradeDto.getSendStartTag() == null ? "FD" : boxUpgradeDto.getSendStartTag());

        byte[] imeibyte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.stringToASCII(imei, imei.length(), ""));
        if ("1".equals(boxUpgradeDto.getImeiType())) {
            imeibyte = DefaultProtocolConvert.hexstring2bytes(imei);
        }

        //协议长度  9 为固定长度=帧头1+协议长度1+指令码1+校验码1+帧尾1+2包号+2数据求和校验
        byte[] protocolLength_byte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(PACKAGE_SIZE.intValue() + imeibyte.length + 9), 2));//数据长度
        //当前下发包号
        int downPackage = Integer.parseInt(boxUpgradeDto.getCurrentDown());

        //协议命定
        String sendCmd = boxUpgradeDto.getSendCmd() == null ? "80" : boxUpgradeDto.getSendCmd().toString();
        byte[] commond_byte = new byte[]{(byte) DefaultProtocolConvert.hexstring2int(sendCmd).intValue()};

        //升级包内容
        byte[] downPackageContent = getUpgradePkgContent(content, packageNum, downPackage);

        //数据求和校验码 占两位
        int byteSum = DefaultProtocolConvert.intSum(downPackageContent);
        String hexByteSum = DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(byteSum), 4);
        byte[] dataVerify_byte = DefaultProtocolConvert.hexstring2bytes(hexByteSum);

        //包号 占两位
        String hexNumber = DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(downPackage), 4);
        byte[] packageNumByte = DefaultProtocolConvert.hexstring2bytes(hexNumber);
        byte[] footerByte = DefaultProtocolConvert.hexstring2bytes(boxUpgradeDto.getSendEndTag() == null ? "DF" : boxUpgradeDto.getSendEndTag());
        byte[] dataByte = DefaultProtocolConvert
                .add(DefaultProtocolConvert
                                .add(DefaultProtocolConvert.add(
                                        DefaultProtocolConvert.add(DefaultProtocolConvert
                                                .add(protocolLength_byte, commond_byte), imeibyte),
                                        packageNumByte), downPackageContent),
                        dataVerify_byte);//从帧长到数据内容

        //协议校验码
        byte[] protocolVerify_byte = new byte[]{(byte) DefaultProtocolConvert.byteSum(dataByte)};

        byte[] returnDataByte = DefaultProtocolConvert.add(dataByte, protocolVerify_byte);

        CallbackMessage callbackMessage = new CallbackMessage();
        //协议头 取消息第一个字节
        callbackMessage.setHeaderMsg(headByte);
        //协议尾 
        callbackMessage.setFooterMsg(footerByte);
        callbackMessage.setBodyMsg(returnDataByte);
        return callbackMessage;
    }

    /**
     * 获取指令路径文件内容 以字节数组形式返回
     *
     * @param filePath 升级文件路径
     * @return
     * @description: TODO 1.文件最少字节数判断 2.校验文件是否有被篡改
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年7月18日 下午2:33:47
     */
    private static byte[] getFileContent(String filePath, String imei) {

        byte[] content = null;
        try {

            /**
             * 文件字节数判断
             */
            content = DefaultProtocolConvert.toByteArray(new FileInputStream(filePath));
            if (content.length < StandardConstantResp.EDENEP_BOX_UPGRADE_FILE_MINSIZE.getValue()) {
                logger.error(String.format(StandardConstantResp.EDENEP_BOX_UPGRADE_FILE_MINSIZE.getMessage(), StandardConstantResp.EDENEP_BOX_UPGRADE_FILE_MINSIZE.getValue()));
                return null;
            }

            /**
             * 校验服务器文件是否被篡改
             */
            byte[] verify_byte = DefaultProtocolConvert.subBytes(content, 1024, 4);
            String verifyStr = DefaultProtocolConvert.bytes2string(verify_byte);
            if (!StandardConstantResp.EDENEP_BOX_UPGRADE_FILE_SERCERT.getMessage().equals(verifyStr)) {
                logger.error(String.format(StandardConstantResp.ERROR_SERVER_UPGRADE_FILE.getMessage(), imei, filePath));
                return null;
            }
        } catch (FileNotFoundException e) {
            logger.error(String.format(StandardConstantResp.ERROR_PUSH_UPGRADE_FILE.getMessage(), imei, filePath));
        } catch (IOException e) {
            logger.error(String.format(StandardConstantResp.ERROR_IO_EXCEPTION.getMessage(), e.getMessage()));
        }

        return content;
    }

    /**
     * 获取下发升级包内容 返回字节数组
     *
     * @param content             包内容
     * @param fileTotalPackageNum 升级文件总包数
     * @param downPackage         当前下载包号
     * @return
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年7月15日 下午1:51:58
     */
    private static byte[] getUpgradePkgContent(byte[] content, BigDecimal fileTotalPackageNum, int downPackage) {

        byte[] downPackageContent = new byte[PACKAGE_SIZE.intValue()];
        //下载最后一个包时,若包长度小于协议约定的包长  则进行补零
        if (downPackage == fileTotalPackageNum.intValue()) {
            int lastPackageLength = content.length % PACKAGE_SIZE.intValue() == 0 ? PACKAGE_SIZE.intValue() : content.length % PACKAGE_SIZE.intValue();
            byte[] dpContent = DefaultProtocolConvert.subBytes(content, (downPackage - 1) * PACKAGE_SIZE.intValue(),
                    lastPackageLength);//最后一个包数据

            /**
             * 补零操作
             */
            if (dpContent.length < PACKAGE_SIZE.intValue()) {
                byte[] lastPackageByte = new byte[PACKAGE_SIZE.intValue() - dpContent.length];
                for (int i = 0; i < PACKAGE_SIZE.intValue() - dpContent.length; i++) {
                    lastPackageByte[i] = 0x00;
                }
                downPackageContent = DefaultProtocolConvert.add(dpContent, lastPackageByte);
            } else {
                downPackageContent = dpContent;
            }
            return downPackageContent;
        }

        /**
         * 非最后一个包,则直接从整包中取指定位置的包数据
         */
        downPackageContent = DefaultProtocolConvert.subBytes(content, (downPackage - 1) * PACKAGE_SIZE.intValue(),
                PACKAGE_SIZE.intValue());//下载包数据
        return downPackageContent;
    }


}
