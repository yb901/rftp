package com.zy.common.core.idgen.conf;

import lombok.Data;

/**
 * ID 生成场景配置
 *
 * <p>用于配置单个发号场景的各项参数。
 *
 * <p>配置示例：
 * <pre>
 * order:
 *   step: 1000              # 每次预获取 ID 数量
 *   time-level: DAY        # 时间维度
 *   start-id: 0            # 起始值，0 表示不设置
 *   loading-threshold-ratio: 0.2  # 异步补充阈值
 * </pre>
 *
 * @author zzy
 * @date 2026-05-05
 * @see IdGeneratorProperties
 * @see TimeLevel
 */
@Data
public class Scene {
    private int step = 1000;
    /**
     * 时间维度，用于多时间尺度的发号器隔离
     */
    private TimeLevel timeLevel = TimeLevel.NONE;
    /**
     * 起始 ID，0 表示不设定起始值
     */
    private long startId = 0;

    /**
     * 异步补充阈值比率，剩余队列占比低于此值时触发补充。
     * 例如 0.2 表示队列剩余不足 step 的 20% 时开始补充。
     * 若设为 0，则只在队列完全取空时才同步加载。
     */
    private double loadingThresholdRatio = 0.2;
}