package lsy.project.socket.api.model;

import java.util.Arrays;


/**
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public class CallbackMessage {

    /**
     * 消息头
     */
    private byte[] headerMsg;

    /**
     * 消息体
     */
    private byte[] bodyMsg;
    /**
     * 消息尾
     */
    private byte[] footerMsg;

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
