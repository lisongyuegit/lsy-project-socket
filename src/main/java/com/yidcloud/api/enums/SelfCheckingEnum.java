package com.yidcloud.api.enums;


import com.lsy.base.exception.BusinessException;

import java.util.Arrays;
import java.util.List;

/**
 * @description: TODO (简单说明如何使用，以及其它有助于快速、正确使用它的有关信息)
 * @copyright: Copyright (c) 2018
 * @company: 易登科技
 * @author: zhouliang@edenep.net
 * @version: 2.0
 * @date: 2018年9月20日 下午9:30:45
 */
public enum SelfCheckingEnum {

    /**
     * '1'， 默认保存一个小时 ， 心跳自检-记录最近心跳时间
     */
    HEART_SELFCHECKING(1, 1 * 1 * 60 * 60 ,"心跳自检-记录最近心跳时间"),
    
    /**
     * '2'， 默认保存七天 ，记录最近上传有效数据时间
     */
    REPORTDATA_SELFCHECKING(2, 7 * 24 * 60 * 60 , "记录最近上传有效数据时间");

    private int value;
    private int expire;
    private String label;
    SelfCheckingEnum(int value, int expire,String label) {
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
            if (employeeRoleEnum.value==value) {
                return employeeRoleEnum;
            }
        }
        throw new BusinessException(-1, "参数有误");
    }
}
