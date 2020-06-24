package com.yidcloud.api.enums;


import com.lsy.base.exception.BusinessException;

import java.util.Arrays;
import java.util.List;

/**
 * 布尔类型枚举定义
 * @author chenyanfa@edenep.net
 * @version 2.0
 * @description:
 * @copyright: Copyright (c) 2017
 * @company: 易登科技
 * @since 2018-3-1 15:22
 */
public enum BooleanCharEnum {

    /**
     * '1' 是
     */
    TRUE("1", "是"),
    /**
     * '0' 否
     */
    FALSE("0", "否");


    private String value;
    private String label;
    BooleanCharEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }



    public String getValue() {
        return value;
    }


    public static BooleanCharEnum checkValue(String value) {
        List<BooleanCharEnum> list = Arrays.asList(BooleanCharEnum.values());
        for (BooleanCharEnum employeeRoleEnum : list) {
            if (employeeRoleEnum.value.equals(value)) {
                return employeeRoleEnum;
            }
        }

        throw new BusinessException(-1, "参数有误");
    }
}
