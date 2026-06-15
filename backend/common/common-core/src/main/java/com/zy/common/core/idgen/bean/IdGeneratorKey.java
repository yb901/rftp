package com.zy.common.core.idgen.bean;

import lombok.Data;

import java.util.Objects;

/**
 * ID 生成器缓存键
 *
 * <p>用于标识不同场景和时间维度的缓存实例。
 * 同一个场景在不同时区/时间窗口下会对应不同的缓存键。
 *
 * @author zzy
 * @date 2026-05-05
 * @see com.zy.common.core.idgen.conf.TimeLevel
 */
@Data
public class IdGeneratorKey {
    private String sceneKey;
    private String time = "";

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdGeneratorKey that)) return false;
        return Objects.equals(sceneKey, that.sceneKey) && Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sceneKey, time);
    }
}