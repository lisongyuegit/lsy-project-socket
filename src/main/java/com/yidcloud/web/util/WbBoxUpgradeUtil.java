package com.yidcloud.web.util;

import com.lsy.base.date.DateHelper;
import com.yidcloud.api.dto.UpgradeDto;
import com.yidcloud.web.cache.CollectRedisCacheService;
import com.yidcloud.web.callback.YdBoxUpgradeCallBack;
import com.yidcloud.web.convert.DefaultProtocolConvert;
import com.yidcloud.web.model.CallbackMessage;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Map;
import java.util.zip.CRC32;

/**
 * 外包盒子协议升级工具类
 * @copyright: Copyright (c) 2018
 * @company: 易登科技
 * @author: zhouliang@edenep.net
 * @version: 2.0
 * @date: 2019年12月13日10:46:08
 */
public class WbBoxUpgradeUtil {
	
    private static Logger logger = LoggerFactory.getLogger(YdBoxUpgradeCallBack.class);

    /**
     * 分包大小
     */
    private static final BigDecimal PACKAGE_SIZE = BigDecimal.valueOf(512);

    /**
     * 下发升级包
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年7月20日 下午5:57:58
     * @param filePath
     * @param imei
     */
    public static void downPackage(String filePath,String imei) {
    	
    	Map<String, String> imeimap = NettyChannelMap.getImeiMap();
        if (null == imeimap || imeimap.isEmpty() || !imeimap.containsKey(imei)) {
            return;
        }

        //设备号对应的客户端通道ID
        String clientId = imeimap.get(imei);
        //客户端tcp通道
        Channel channel = NettyChannelMap.get(clientId);
        
    	UpgradeDto boxUpgradeDto = CollectRedisCacheService.getUpgradeDto(imei);
        if(null == boxUpgradeDto) {
        	logger.info(String.format(StandardConstantResp.ERROR_NOTFOUND_UPGRADECACHE.getMessage(), imei));
        	return;
        }
        
        byte[] content = getFileContent(filePath,imei);
        if(null == content) {
        	return;
        }
        
        //包数量
        BigDecimal packageNum = BigDecimal.valueOf(content.length).divide(PACKAGE_SIZE, 0,
        		RoundingMode.CEILING);

		logger.info("升级包号-----------------"+packageNum);
		packageNum = packageNum.subtract(new BigDecimal("1"));
        /**
         * 下发数据到盒子
         */
        CallbackMessage callbackMessage = createDownData(boxUpgradeDto, content, packageNum);
        channel.writeAndFlush(callbackMessage);
        

		//文件包总字节数
        boxUpgradeDto.setFileByteSize(content.length+"");

		//文件总字节求和
        boxUpgradeDto.setFileByteSum(DefaultProtocolConvert.intSum(content)+"");
        boxUpgradeDto.setPackageTotal(packageNum+"");

        //取文件中隐藏的版本号
        byte[] version_byte = DefaultProtocolConvert.subBytes(content, 1036, 4);

        String gversion = DefaultProtocolConvert.bytes2string(version_byte);
        boxUpgradeDto.setGversion(gversion);
        boxUpgradeDto.setCurrentPackageDownTime(DateHelper.formatDate(new Date(),DateHelper.PATTERN_TIME));
		//更新缓存
        CollectRedisCacheService.addServerPushUpgrade(boxUpgradeDto);

        /**
         * 打印升级进度日志
         */
        printUpgradeLog(boxUpgradeDto, callbackMessage);
    }
    
    /**
     * 组装最后一个升级包内容
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018 2018年7月20日 下午4:15:30
     * @param imei
     * @param upgradeDto
     * @return
     */
	public static CallbackMessage createLastPackageContent(String imei, UpgradeDto upgradeDto) {

		byte[] headByte = DefaultProtocolConvert.hexstring2bytes(upgradeDto.getSendStartTag());

		//协议命定
		byte[] cmdByte = new byte[] {(byte) DefaultProtocolConvert.hexstring2int(upgradeDto.getSendLastPackageCmd()).intValue()};

		byte[] ydCmdByte = new byte[] {(byte) DefaultProtocolConvert.hexstring2int("FE").intValue()};

		byte[] imeibyte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.stringToASCII(imei, imei.length(), ""));

		byte[] encryptionByte = new byte[] {(byte) DefaultProtocolConvert.hexstring2int("01").intValue()};

		//数据内容长度 8
		byte[] contentLengthbyte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(8),4));

		//文件大小
		String fileByteSize = upgradeDto.getFileByteSize();
		int iFileByteSize = Integer.parseInt(fileByteSize);
		String hexFileByteSize = DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(iFileByteSize),8);
		byte[] fileSize_byte =DefaultProtocolConvert.hexstring2bytes(hexFileByteSize);

		//文件总字节求和
		CRC32 crc32 = new CRC32();
		crc32.update(getFileContent(upgradeDto.getFilePath(),imei));
		String hexFileByteSum = DefaultProtocolConvert.fixHexStringOffset(Long.toHexString(crc32.getValue()),8);
		byte[] crc32_byte =DefaultProtocolConvert.hexstring2bytes(hexFileByteSum);

		byte [] dataByte = DefaultProtocolConvert.add(fileSize_byte, crc32_byte);
		//从帧长到数据内容
		byte[] totalDataByte = DefaultProtocolConvert.add(DefaultProtocolConvert.add(DefaultProtocolConvert.add(
				DefaultProtocolConvert.add(DefaultProtocolConvert
						.add(cmdByte, ydCmdByte), imeibyte),encryptionByte),contentLengthbyte),dataByte);

		//协议校验码
		byte[] yhbyte = new byte[] {(byte) DefaultProtocolConvert.byteSum(totalDataByte)};

		CallbackMessage result = new CallbackMessage();
		//协议头
		result.setHeaderMsg(headByte);
		result.setBodyMsg(totalDataByte);
		result.setFooterMsg(yhbyte);
		return result;
	}
	
	/**
     * 打印升级进度日志
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年7月20日 下午3:14:29
     * @param boxUpgradeDto
     * @param callbackMessage
     */
	private static void printUpgradeLog(UpgradeDto boxUpgradeDto, CallbackMessage callbackMessage) {
		
		String imei = boxUpgradeDto.getImei();
		long current = System.currentTimeMillis();
        long lastPackageDownTime = boxUpgradeDto.getCurrentPackageDownTime()==null?current:DateHelper.parseString(boxUpgradeDto.getCurrentPackageDownTime()).getTime();
        long interval = current-lastPackageDownTime;
        logger.info("["+imei+"]升级分包下发,总包["+boxUpgradeDto.getPackageTotal()+"]正在下发第["+boxUpgradeDto.getCurrentDown()+"]个包,距上一包间隔["+interval+"]="+DefaultProtocolConvert.bytes2hexstring(callbackMessage.getHeaderMsg())+
        		DefaultProtocolConvert.bytes2hexstring(callbackMessage.getBodyMsg())+
        		DefaultProtocolConvert.bytes2hexstring(callbackMessage.getFooterMsg())+
        		" byte="+callbackMessage.toString());
	}

	/**
	 * 构建下发升级数据包
	 * @author: zhouliang@edenep.net
	 * @version: 2.0
	 * @date: 2018年7月20日 下午3:16:22
	 * @param boxUpgradeDto 
	 * @param content 升级文件总字节
	 * @param packageNum 升级文件总包号
	 * @return
	 */
	private static CallbackMessage createDownData(UpgradeDto boxUpgradeDto, byte[] content,
			BigDecimal packageNum) {
		
		String imei = boxUpgradeDto.getImei();

		byte[] headByte = DefaultProtocolConvert.hexstring2bytes(boxUpgradeDto.getSendStartTag());

		//协议命定
		byte[] cmdByte = new byte[] {(byte) DefaultProtocolConvert.hexstring2int(boxUpgradeDto.getSendCmd()).intValue()};

		byte[] ydCmdByte = new byte[] {(byte) DefaultProtocolConvert.hexstring2int("FE").intValue()};

		byte[] imeibyte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.stringToASCII(imei, imei.length(), ""));

		byte[] encryptionByte = new byte[] {(byte) DefaultProtocolConvert.hexstring2int("01").intValue()};

        //协议长度 260
        byte[] contentLengthbyte = DefaultProtocolConvert.hexstring2bytes(DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(PACKAGE_SIZE.intValue()+4),4));

        //当前下发包号
        int downPackage = Integer.parseInt(boxUpgradeDto.getCurrentDown());
		logger.info("升级包号-----------------"+downPackage);
        //数据求和校验码 占两位
        int pkgContentLength = getUpgradePkgContentLength(content, packageNum, downPackage);
        String hexByteSum = DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(pkgContentLength),4);
		byte[] pkgContentLengthByte = DefaultProtocolConvert.hexstring2bytes(hexByteSum);
        
        //包号 占两位
        String hexNumber = DefaultProtocolConvert.fixHexStringOffset(Integer.toHexString(downPackage),4);
        byte[] packageNumByte =DefaultProtocolConvert.hexstring2bytes(hexNumber);

		//升级包内容
		byte[] downPackageContent = getUpgradePkgContent(content, packageNum, downPackage);

		for (Byte b:downPackageContent) {
			System.out.println(downPackage+ "byte: "+b);
		}

		byte [] dataByte = DefaultProtocolConvert.add(DefaultProtocolConvert.add(packageNumByte, pkgContentLengthByte), downPackageContent);
		//从帧长到数据内容
		byte[] totalDataByte = DefaultProtocolConvert.add(DefaultProtocolConvert.add(DefaultProtocolConvert.add(
				DefaultProtocolConvert.add(DefaultProtocolConvert
						.add(cmdByte, ydCmdByte), imeibyte),encryptionByte),contentLengthbyte),dataByte);

		//协议校验码
		byte[] yhbyte = new byte[] {(byte) DefaultProtocolConvert.byteSum(totalDataByte)};

		CallbackMessage result = new CallbackMessage();
		//协议头
		result.setHeaderMsg(headByte);
		result.setBodyMsg(totalDataByte);
		result.setFooterMsg(yhbyte);
        return result;
	}

    /**
     * 获取指令路径文件内容 以字节数组形式返回
     * @description: TODO 1.文件最少字节数判断 2.校验文件是否有被篡改
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年7月18日 下午2:33:47
     * @param filePath 升级文件路径
     * @return
     */
	private static byte[] getFileContent(String filePath,String imei) {
		
		byte[] content = null;
		try {
			
            /**
             * 文件字节数判断
             */
			content = DefaultProtocolConvert.toByteArray(new FileInputStream(filePath));
            if(content.length<StandardConstantResp.EDENEP_BOX_UPGRADE_FILE_MINSIZE.getValue()) {
                logger.error(String.format(StandardConstantResp.EDENEP_BOX_UPGRADE_FILE_MINSIZE.getMessage(), StandardConstantResp.EDENEP_BOX_UPGRADE_FILE_MINSIZE.getValue()));
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
     * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
     * @author: zhouliang@edenep.net
     * @version: 2.0
     * @date: 2018年7月15日 下午1:51:58
     * @param content 包内容
     * @param fileTotalPackageNum 升级文件总包数
     * @param downPackage 当前下载包号
     * @return
     */
	private static byte[] getUpgradePkgContent(byte[] content, BigDecimal fileTotalPackageNum, int downPackage) {
		
		byte[] downPackageContent = new byte[PACKAGE_SIZE.intValue()];
		//下载最后一个包时,若包长度小于协议约定的包长  则进行补零
		if(downPackage==fileTotalPackageNum.intValue()) {
		    int lastPackageLength = content.length%PACKAGE_SIZE.intValue()==0?PACKAGE_SIZE.intValue():content.length%PACKAGE_SIZE.intValue();

			//最后一个包数据
		    byte [] dpContent = DefaultProtocolConvert.subBytes(content, (downPackage) * PACKAGE_SIZE.intValue(),
		            lastPackageLength);
		    
		    /**
		     * 补零操作
		     */
		    if(dpContent.length<PACKAGE_SIZE.intValue()) {
		        byte [] lastPackageByte = new byte[PACKAGE_SIZE.intValue()-dpContent.length];
		        for(int i=0;i<PACKAGE_SIZE.intValue()-dpContent.length;i++) {
		            lastPackageByte[i]=0x00;
		        }
		        downPackageContent = DefaultProtocolConvert.add(dpContent, lastPackageByte);
		    }else {
		    	downPackageContent = dpContent;
		    }
		    return downPackageContent;
		}
		
		/**
		 * 非最后一个包,则直接从整包中取指定位置的包数据
		 */
		downPackageContent = DefaultProtocolConvert.subBytes(content, (downPackage) * PACKAGE_SIZE.intValue(),
				PACKAGE_SIZE.intValue());
		return downPackageContent;
	}


	private static int getUpgradePkgContentLength(byte[] content, BigDecimal fileTotalPackageNum, int downPackage) {

		int pkgContentLength = PACKAGE_SIZE.intValue();
		//下载最后一个包时
		if(downPackage==fileTotalPackageNum.intValue()) {
			int lastPackageLength = content.length%PACKAGE_SIZE.intValue()==0?PACKAGE_SIZE.intValue():content.length%PACKAGE_SIZE.intValue();
			//最后一个包数据
			byte [] dpContent = DefaultProtocolConvert.subBytes(content, (downPackage-1) * PACKAGE_SIZE.intValue(),
					lastPackageLength);
			pkgContentLength = dpContent.length;
		}
		return pkgContentLength;
	}

	public static void main(String[] args) {

		CRC32 crc32 = new CRC32();
		crc32.update(getFileContent("F:/app.bin","1234567890ABCDEFG"));
		System.out.println(crc32.getValue());
	}
}
