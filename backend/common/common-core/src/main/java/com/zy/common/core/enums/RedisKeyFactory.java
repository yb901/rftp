package com.zy.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RedisKeyFactory {

    K001("合同编号生成器"),
    K002("短链-ip限制"),
    ;

    private final String desc;

    private static final String SPACE = "common";
    private static final String SEPARATOR = "_";

    /**
     * 需要追加参数作为key时使用，参数会以SEPARATOR隔开
     *
     * @param args 追加的参数
     * @return key
     */
    public String join(Object... args) {
        StringBuilder key = new StringBuilder(toString());
        for (Object arg : args) {
            key.append(SEPARATOR).append(arg);
        }
        return key.toString();
    }

    @Override
    public String toString() {
        return SPACE + SEPARATOR + super.toString();
    }
}
