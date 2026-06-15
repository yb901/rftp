package com.zy.common.core.order;

import org.apache.commons.lang3.StringUtils;

/**
 * 分片后缀解析器
 *
 * <p>将三位分片键转换为分表后缀。
 * 例如：000 -> ""，001 -> "_001"。</p>
 *
 * @author zzy
 * @date 2026/06/01
 */
public final class ShardSuffixResolver {

    /**
     * 私有构造方法
     */
    private ShardSuffixResolver() {
    }

    /**
     * 默认不分表分片键
     */
    private static final String NO_SHARD_KEY = "000";

    /**
     * 解析分表后缀
     *
     * @param shardKey 分片键
     * @return 分表后缀
     */
    public static String resolve(String shardKey) {
        if (!isValidShardKey(shardKey)) {
            throw new IllegalArgumentException("分片键格式不合法");
        }
        if (NO_SHARD_KEY.equals(shardKey)) {
            return "";
        }
        return "_" + shardKey;
    }

    /**
     * 校验分片键格式
     *
     * @param shardKey 分片键
     * @return 校验结果
     */
    public static boolean isValidShardKey(String shardKey) {
        return StringUtils.isNotBlank(shardKey) && shardKey.matches("\\d{3}");
    }
}
