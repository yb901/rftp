package com.zy.common.core.es;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Locale;

/**
 * ES 索引 settings 环境覆盖配置。
 *
 * <p>默认只允许 test profile 生效，避免生产环境误配置影响所有索引。</p>
 *
 * @author zzy
 * @date 2026/05/21
 */
@Data
@ConfigurationProperties(prefix = "zy.elasticsearch.index.settings-override")
public class EsIndexSettingsOverrideProperties {

    private static final String TEST_PROFILE = "test";

    /**
     * 是否启用 settings 覆盖。
     */
    private boolean enabled = false;

    /**
     * 主分片数。
     */
    private Integer numberOfShards;

    /**
     * 副本数。
     */
    private Integer numberOfReplicas;

    /**
     * 刷新间隔。
     */
    private String refreshInterval;

    /**
     * 判断当前 profile 是否允许覆盖 settings。
     *
     * @param currentProfiles 当前生效 profile
     * @return 判断结果
     */
    public boolean isEnabledFor(String[] currentProfiles) {
        if (!enabled || currentProfiles == null || currentProfiles.length == 0) {
            return false;
        }
        for (String currentProfile : currentProfiles) {
            if (currentProfile != null && TEST_PROFILE.equals(currentProfile.trim().toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否存在具体覆盖项。
     *
     * @return 判断结果
     */
    public boolean hasOverrides() {
        return numberOfShards != null || numberOfReplicas != null || isNotBlank(refreshInterval);
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
