package lsy.project.socket.api.enums;


import com.lsy.base.exception.BusinessException;

import java.util.Arrays;
import java.util.List;

/**
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public enum SelfCheckingEnum {

    /**
     * '1'， 默认保存一个小时 ， 心跳自检-记录最近心跳时间
     */
    HEART_SELFCHECKING(1, 1 * 1 * 60 * 60, "心跳自检-记录最近心跳时间"),

    /**
     * '2'， 默认保存七天 ，记录最近上传有效数据时间
     */
    REPORTDATA_SELFCHECKING(2, 7 * 24 * 60 * 60, "记录最近上传有效数据时间");

    private int value;
    private int expire;
    private String label;

    SelfCheckingEnum(int value, int expire, String label) {
        this.value = value;
        this.expire = expire;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public int getExpire() {
        return expire;
    }

    public static SelfCheckingEnum checkValue(int value) {
        List<SelfCheckingEnum> list = Arrays.asList(SelfCheckingEnum.values());
        for (SelfCheckingEnum employeeRoleEnum : list) {
            if (employeeRoleEnum.value == value) {
                return employeeRoleEnum;
            }
        }
        throw new BusinessException(-1, "参数有误");
    }
}
