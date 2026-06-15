package com.zy.common.core.idgen;

import com.zy.common.core.idgen.bean.IdGeneratorKey;
import com.zy.common.core.idgen.conf.IdGeneratorProperties;
import com.zy.common.core.idgen.conf.Scene;
import com.zy.common.core.idgen.service.IdGeneratorCache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalListener;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * ID 生成器客户端
 *
 * <p>提供统一的 ID 生成入口，基于 Caffeine 本地缓存 + Redis 分布式发号实现高性能 ID 生成。
 * 支持多场景隔离，每个场景可配置不同的时间维度、步长、起始值。
 *
 * <p>启动时自动初始化所有已配置场景的缓存，支持定时清理过期缓存。
 *
 * @author zzy
 * @date 2026-05-05
 * @see IdGeneratorCache
 * @see IdGeneratorProperties
 */
@Slf4j
public class IdGeneratorClient implements SmartInitializingSingleton {

    @Resource
    private IdGeneratorProperties idGeneratorProperties;
    private final StringRedisTemplate stringRedisTemplate;

    public IdGeneratorClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private final LoadingCache<IdGeneratorKey, IdGeneratorCache> cache = Caffeine.newBuilder()
            .removalListener((RemovalListener<IdGeneratorKey, IdGeneratorCache>) (key, value, cause) -> {
                if (value != null) {
                    value.destroy();
                }
            })
            .build(new CacheLoader<>() {
                @Override
                public @NonNull IdGeneratorCache load(@NonNull IdGeneratorKey key) {
                    IdGeneratorCache cache = new IdGeneratorCache(idGeneratorProperties, stringRedisTemplate);
                    cache.setScene(idGeneratorProperties.getScenes().get(key.getSceneKey()));
                    cache.setKey(key);
                    cache.init();
                    return cache;
                }
            });

    /**
     * 生成 ID
     *
     * @param sceneKey 场景标识，对应配置中的场景名称
     * @return 生成的 ID
     * @throws RuntimeException 如果场景未配置
     */
    public Long idGenerator(String sceneKey) {
        return generator(sceneKey, new Date());
    }

    /**
     * 生成 ID（指定时间）
     *
     * @param sceneKey 场景标识
     * @param time     时间戳，用于时间维度隔离
     * @return 生成的 ID
     */
    private Long generator(String sceneKey, Date time) {
        Scene scene = idGeneratorProperties.getScenes().get(sceneKey);
        IdGeneratorCache idGeneratorCache = getCache(sceneKey, scene, time);
        return Objects.requireNonNull(idGeneratorCache).get();
    }

    /**
     * 获取缓存实例
     *
     * <p>根据场景键和时间格式化串获取缓存，若缓存不存在或已失效则创建新的缓存。
     * 同时清理同场景下其他已过期的时间缓存。
     *
     * @param sceneKey 场景标识
     * @param scene    场景配置
     * @param time     时间戳
     * @return 缓存实例
     * @throws RuntimeException 如果场景未配置
     */
    private IdGeneratorCache getCache(String sceneKey, Scene scene, Date time) {
        if (Objects.isNull(scene)) {
            throw new RuntimeException("发号场景[" + sceneKey + "]未配置");
        }
        String formatTime = scene.getTimeLevel().formatTime(time);
        IdGeneratorKey key = new IdGeneratorKey();
        key.setSceneKey(sceneKey);
        key.setTime(formatTime);

        IdGeneratorCache cached = cache.getIfPresent(key);
        if (cached != null && !cached.isInvalidate()) {
            return cached;
        }

        for (Map.Entry<IdGeneratorKey, IdGeneratorCache> entry : cache.asMap().entrySet()) {
            IdGeneratorKey existKey = entry.getKey();
            if (existKey.getSceneKey().equals(sceneKey) && !existKey.getTime().equals(formatTime)) {
                cache.invalidate(existKey);
                log.info("IdGenerator removed expired cache: scene={}, oldTime={}", sceneKey, existKey.getTime());
            }
        }
        return cache.get(key);
    }

    /**
     * 定时扫描并清理失效缓存
     *
     * <p>每小时执行一次，清理所有已过期的缓存实例。
     * 缓存失效条件：时间维度不为 NONE 且当前时间已超出缓存对应的时间窗口。
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void scanCache() {
        for (Map.Entry<IdGeneratorKey, IdGeneratorCache> entry : cache.asMap().entrySet()) {
            if (entry.getValue().isInvalidate()) {
                cache.invalidate(entry.getKey());
            }
        }
    }

    /**
     * 初始化所有场景缓存
     *
     * <p>在所有单例 Bean 初始化完成后执行，主动预热所有已配置场景的缓存。
     * 若初始化失败，记录警告日志，缓存将在首次请求时延迟加载。
     */
    @Override
    public void afterSingletonsInstantiated() {
        try {
            Map<String, Scene> scenes = idGeneratorProperties.getScenes();
            if (scenes == null || scenes.isEmpty()) return;
            for (Map.Entry<String, Scene> entry : scenes.entrySet()) {
                getCache(entry.getKey(), entry.getValue(), new Date());
                log.info("IdGenerator, init key={}", entry.getKey());
            }
        } catch (Exception e) {
            log.warn("IdGenerator init error, will lazy-load on first request", e);
        }
    }
}