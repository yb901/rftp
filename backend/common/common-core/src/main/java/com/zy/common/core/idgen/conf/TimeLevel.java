package com.zy.common.core.idgen.conf;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

/**
 * 时间维度枚举
 *
 * <p>用于 ID 生成器的时间隔离策略，决定 Redis 键的时间粒度。
 * 不同时间维度适用于不同的业务场景：
 * <ul>
 *   <li>{@link #NONE} - 无时间维度，全局单调递增，适合不需要重置的场景</li>
 *   <li>{@link #DAY} - 每天重置，适合日表隔离或按天分库分表</li>
 *   <li>{@link #MONTH} - 每月重置，适合月度统计</li>
 *   <li>{@link #YEAR} - 每年重置，适合年度统计</li>
 * </ul>
 *
 * @author zzy
 * @date 2026-05-05
 * @see Scene#getTimeLevel()
 */
@Getter
public enum TimeLevel {
    NONE(""),
    DAY("yyyy-MM-dd"),
    MONTH("yyyy-MM"),
    YEAR("yyyy");

    private final SimpleDateFormat format;
    /**
     * 时间格式码，如 "yyyy-MM-dd"
     */
    private final String code;

    TimeLevel(String code) {
        this.code = code;
        if (StringUtils.isBlank(code)) {
            this.format = null;
        } else {
            this.format = new SimpleDateFormat(code);
            this.format.setTimeZone(TimeZone.getTimeZone("UTC")); // 强制 UTC
        }
    }

    public String formatTime(Date time) {
        if (Objects.isNull(format)) {
            return "";
        }
        synchronized (format) {
            return format.format(time);
        }
    }
}