package com.zy.common.core.idgen.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Map;

/**
 * ID 生成器配置属性
 *
 * <p>通过 {@code idgen.scenes} 配置项绑定，支持多场景配置。
 * 每个场景可独立设置步长、时间维度、起始值、异步补充阈值。
 *
 * <p>配置示例：
 * <pre>
 * idgen:
 *   scenes:
 *     order:
 *       step: 1000
 *       time-level: DAY
 *       start-id: 0
 *       loading-threshold-ratio: 0.2
 * </pre>
 *
 * @author zzy
 * @date 2026-05-05
 * @see Scene
 * @see com.zy.common.core.idgen.IdGeneratorClient
 */
@Data
@ConfigurationProperties("idgen")
public class IdGeneratorProperties {
    private Map<String, Scene> scenes = Collections.emptyMap();
}