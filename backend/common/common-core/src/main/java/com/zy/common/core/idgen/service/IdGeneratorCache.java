package com.zy.common.core.idgen.service;

import com.zy.common.core.idgen.bean.IdGeneratorKey;
import com.zy.common.core.idgen.conf.IdGeneratorProperties;
import com.zy.common.core.idgen.conf.Scene;
import com.zy.common.core.idgen.conf.TimeLevel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ID 生成器缓存单元
 *
 * <p>管理单个场景+时间维度的 ID 队列缓存。
 * 内部维护一个本地队列 {@link #idQueue}，通过异步/同步方式从 Redis 批量获取 ID。
 *
 * <p>核心机制：
 * <ul>
 *   <li>预加载：初始化时从 Redis 批量获取 step 个 ID 存入本地队列</li>
 *   <li>异步补充：当队列剩余量低于阈值时，触发异步补充</li>
 *   <li>单例模式：每个场景+时间维度对应一个缓存实例</li>
 * </ul>
 *
 * @author zzy
 * @date 2026-05-05
 */
@Slf4j
@Data
public class IdGeneratorCache {

    // 全局虚拟线程池，所有场景共享
    private final ExecutorService VIRTUAL_THREAD_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private final IdGeneratorProperties idGeneratorProperties;
    private final StringRedisTemplate stringRedisTemplate;

    private Scene scene;
    private IdGeneratorKey key;
    private final LinkedBlockingQueue<Long> idQueue = new LinkedBlockingQueue<>();
    private final ReentrantLock loadLock = new ReentrantLock();
    private int loadingThreshold;

    // Lua 脚本：原子化初始化起始 ID
    private static final DefaultRedisScript<Long> INIT_START_SCRIPT;
    // Lua 脚本：原子化 INCRBY + EXPIRE
    private static final DefaultRedisScript<Long> INCRBY_WITH_EXPIRE_SCRIPT;

    static {
        INIT_START_SCRIPT = new DefaultRedisScript<>();
        INIT_START_SCRIPT.setScriptText(
                "local cur = redis.call('GET', KEYS[1]) " +
                        "if cur == false then cur = 0 else cur = tonumber(cur) end " +
                        "local start = tonumber(ARGV[1]) " +
                        "local ttl = tonumber(ARGV[2]) " +
                        "if cur < start then " +
                        "  local delta = start - cur " +
                        "  local newval = redis.call('INCRBY', KEYS[1], delta) " +
                        "  redis.call('EXPIRE', KEYS[1], ttl) " +
                        "  return newval " +
                        "else " +
                        "  redis.call('EXPIRE', KEYS[1], ttl) " +
                        "  return cur " +
                        "end"
        );
        INIT_START_SCRIPT.setResultType(Long.class);

        INCRBY_WITH_EXPIRE_SCRIPT = new DefaultRedisScript<>();
        INCRBY_WITH_EXPIRE_SCRIPT.setScriptText(
                "local val = redis.call('INCRBY', KEYS[1], ARGV[1]) " +
                        "redis.call('EXPIRE', KEYS[1], ARGV[2]) " +
                        "return val"
        );
        INCRBY_WITH_EXPIRE_SCRIPT.setResultType(Long.class);
    }

    public IdGeneratorCache(IdGeneratorProperties idGeneratorProperties,
                            StringRedisTemplate stringRedisTemplate) {
        this.idGeneratorProperties = idGeneratorProperties;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 初始化缓存
     *
     * <p>计算异步补充阈值，初始化起始 ID，然后预加载 ID 队列。
     */
    public void init() {
        this.loadingThreshold = (int) (scene.getStep() * scene.getLoadingThresholdRatio());
        if (loadingThreshold <= 0) {
            loadingThreshold = 0;
        }
        initStart();
        loadId();
    }

    /**
     * 判断缓存是否已失效
     *
     * <p>对于有时间维度的场景（如 DAY），当当前时间已超出缓存对应的时间窗口时失效。
     * 无时间维度（NONE）的缓存永远不过期。
     *
     * @return true if 缓存已失效，需要重新创建
     */
    public boolean isInvalidate() {
        if (scene.getTimeLevel() == TimeLevel.NONE) {
            return false;
        }
        String now = scene.getTimeLevel().formatTime(new Date());
        return !now.equals(key.getTime());
    }

    /**
     * 销毁缓存
     *
     * <p>清理资源，用于缓存移除时的回调。
     */
    public void destroy() {
        log.info("IdGeneratorCache destroyed for key={}", key);
    }

    /**
     * 获取一个 ID
     *
     * <p>优先从本地队列获取，若队列为空则同步加载。
     * 当队列剩余量低于阈值时，触发异步补充。
     *
     * @return 生成的 ID
     * @throws RuntimeException 若获取超时或被中断
     */
    public Long get() {
        if (Objects.equals(1, scene.getStep())) {
            return incrBy(getRedisKey(), 1);
        }

        if (idQueue.isEmpty()) {
            loadId();
        } else if (loadingThreshold > 0 && idQueue.size() < loadingThreshold) {
            VIRTUAL_THREAD_EXECUTOR.execute(this::safeLoadId);
        }

        try {
            Long id = idQueue.poll(1, TimeUnit.SECONDS);
            if (id == null) {
                throw new RuntimeException("发号超时，队列无法获取 ID");
            }
            return id;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取 ID 被中断");
        }
    }

    /**
     * 安全加载 ID
     *
     * <p>捕获异常，避免异步任务失败影响主流程。
     */
    private void safeLoadId() {
        try {
            loadId();
        } catch (Exception e) {
            log.error("IdGenerator async loadId error, key={}", key, e);
        }
    }

    /**
     * 同步加载 ID
     *
     * <p>从 Redis 通过 INCRBY 批量获取 ID，存入本地队列。
     * 使用分布式锁避免多线程同时加载。
     */
    private void loadId() {
        if (Objects.equals(1L, scene.getStep())) {
            return;
        }
        if (!loadLock.tryLock()) {
            return;
        }
        try {
            if (idQueue.size() >= scene.getStep()) {
                return;
            }
            long delta = scene.getStep();
            long result = incrBy(getRedisKey(), delta);
            if (result < delta) {
                log.error("IdGenerator incr error, key={}, delta={}, result={}", key.getSceneKey(), delta, result);
                return;
            }
            for (long i = result - delta; i < result; i++) {
                idQueue.add(i + 1);
            }
        } finally {
            loadLock.unlock();
        }
    }

    /**
     * 初始化起始 ID
     *
     * <p>使用 Lua 脚本原子化地将 Redis 中的值调整为指定的起始值。
     * 仅在 startId > 0 时执行。
     */
    private void initStart() {
        if (scene.getStartId() <= 0) {
            return;
        }
        String redisKey = getRedisKey();
        long ttlSeconds = getTimeoutHours() * 3600L;
        try {
            Long result = stringRedisTemplate.execute(
                    INIT_START_SCRIPT,
                    Collections.singletonList(redisKey),
                    String.valueOf(scene.getStartId()),
                    String.valueOf(ttlSeconds)
            );
            if (result < scene.getStartId()) {
                throw new RuntimeException("发号场景[" + key.getSceneKey() + "]初始化起始值失败");
            }
            log.info("IdGenerator init success, startId={}, redisKey={}, result={}",
                    scene.getStartId(), redisKey, result);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("IdGenerator initStart error, key={}", key.getSceneKey(), e);
            throw new RuntimeException("发号场景[" + key.getSceneKey() + "]初始化失败", e);
        }
    }

    /**
     * 执行 Redis INCRBY + EXPIRE 操作
     *
     * @param redisKey Redis 键
     * @param delta    增量
     * @return 递增后的值
     */
    private Long incrBy(String redisKey, long delta) {
        long ttlSeconds = (getTimeoutHours() + 1) * 3600L; // 原逻辑多 1 小时缓冲
        return stringRedisTemplate.execute(
                INCRBY_WITH_EXPIRE_SCRIPT,
                Collections.singletonList(redisKey),
                String.valueOf(delta),
                String.valueOf(ttlSeconds)
        );
    }

    /**
     * 获取超时时间（小时）
     *
     * <p>根据时间维度返回不同的 TTL：
     * <ul>
     *   <li>NONE: 878400 小时（100年）</li>
     *   <li>YEAR: 8784 小时（1年）</li>
     *   <li>MONTH: 744 小时（31天）</li>
     *   <li>DAY: 24 小时（1天）</li>
     * </ul>
     */
    private long getTimeoutHours() {
        TimeLevel timeLevel = scene.getTimeLevel();
        return switch (timeLevel) {
            case NONE -> 878400;
            case YEAR -> 8784;
            case MONTH -> 744;
            default -> 24;
        };
    }

    /**
     * 获取 Redis 键
     *
     * <p>格式：{@code sceneKey_time}，如 {@code order_2026-05-05}
     */
    private String getRedisKey() {
        return key.getSceneKey() + "_" + key.getTime();
    }
}