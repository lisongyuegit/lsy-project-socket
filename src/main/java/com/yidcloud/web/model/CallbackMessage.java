package com.yidcloud.web.model;

import java.util.Arrays;

/**
 * (一句话描述该类做什么)
 *
 * @author 胡洪瑜 huhongyu@edenep.net
 * @version 2.0
 * @company 易登科技
 * @since 2017/11/15 10:47
 */
public class CallbackMessage {

    /**
     * 消息头
     */
    private byte [] headerMsg;

    /**
     * 消息体
     */
    private byte [] bodyMsg;
    /**
     * 消息尾
     */
    private byte [] footerMsg;

    public byte[] getHeaderMsg() {
        return headerMsg;
    }

    public void setHeaderMsg(byte[] headerMsg) {
        this.headerMsg = headerMsg;
    }

    public byte[] getBodyMsg() {
        return bodyMsg;
    }

    public void setBodyMsg(byte[] bodyMsg) {
        this.bodyMsg = bodyMsg;
    }

    public byte[] getFooterMsg() {
        return footerMsg;
    }

    public void setFooterMsg(byte[] footerMsg) {
        this.footerMsg = footerMsg;
    }

    @Override
    public String toString() {
        return "CallbackMessage{" +
                "headerMsg=" + Arrays.toString(headerMsg) +
                ", bodyMsg=" + Arrays.toString(bodyMsg) +
                ", footerMsg=" + Arrays.toString(footerMsg) +
                '}';
    }
}
