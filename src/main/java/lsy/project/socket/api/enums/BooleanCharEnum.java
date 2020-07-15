package lsy.project.socket.api.enums;


import com.lsy.base.exception.BusinessException;

import java.util.Arrays;
import java.util.List;


/**
 * 布尔类型枚举定义
 *
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
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
