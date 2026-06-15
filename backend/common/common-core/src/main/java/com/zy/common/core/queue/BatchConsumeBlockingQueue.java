package com.zy.common.core.queue;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * 批量消费阻塞队列
 *
 * @param <T> 队列元素类型
 * @author zzy
 * @date 2026/06/08
 */
@Slf4j
public class BatchConsumeBlockingQueue<T> {

    /**
     * 单次消费数量
     */
    private final int batchSize;

    /**
     * 数量触发阈值
     */
    private final int maxWaitSize;

    /**
     * 最大等待毫秒数
     */
    private final int maxWaitTimeMillis;

    /**
     * 批量消费回调
     */
    private final Consumer<List<T>> batchConsumer;

    /**
     * 内部队列
     */
    private final BlockingQueue<T> queue;

    /**
     * 消费锁
     */
    private final ReentrantLock consumeLock = new ReentrantLock();

    /**
     * 消费条件
     */
    private final Condition consumeCondition = consumeLock.newCondition();

    /**
     * 最近一次消费时间
     */
    private volatile long lastConsumeTimeMillis;

    /**
     * 是否已启动
     */
    private volatile boolean started;

    /**
     * 定时唤醒线程
     */
    private final Thread scheduleThread;

    /**
     * 消费线程
     */
    private final Thread consumeThread;

    /**
     * 构造批量消费阻塞队列
     *
     * @param capacity              队列容量，非正数表示无界
     * @param batchSize             单次消费数量
     * @param maxWaitSize           数量触发阈值，非正数表示不按数量触发
     * @param maxWaitTimeMillis     最大等待毫秒数，非正数表示不按时间触发
     * @param queueName             队列名称
     * @param batchConsumer         批量消费回调
     */
    public BatchConsumeBlockingQueue(int capacity,
                                     int batchSize,
                                     int maxWaitSize,
                                     int maxWaitTimeMillis,
                                     String queueName,
                                     Consumer<List<T>> batchConsumer) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be greater than zero");
        }
        if (maxWaitSize > 0 && batchSize > maxWaitSize) {
            throw new IllegalArgumentException("batchSize must be less than or equal to maxWaitSize");
        }
        if (maxWaitSize <= 0 && maxWaitTimeMillis <= 0) {
            throw new IllegalArgumentException("maxWaitSize and maxWaitTimeMillis cannot both be less than or equal to zero");
        }
        this.queue = capacity <= 0 ? new LinkedBlockingQueue<>() : new LinkedBlockingQueue<>(capacity);
        this.batchSize = batchSize;
        this.maxWaitSize = maxWaitSize;
        this.maxWaitTimeMillis = maxWaitTimeMillis;
        this.batchConsumer = Objects.requireNonNull(batchConsumer, "batchConsumer must not be null");
        String safeName = queueName == null || queueName.isBlank() ? "batch-consume" : queueName.trim();
        this.scheduleThread = new Thread(this::schedule, safeName + "-schedule");
        this.consumeThread = new Thread(this::consumeLoop, safeName + "-consumer");
        this.scheduleThread.setDaemon(true);
        this.consumeThread.setDaemon(true);
    }

    /**
     * 启动队列
     */
    public synchronized void start() {
        if (started) {
            return;
        }
        started = true;
        lastConsumeTimeMillis = System.currentTimeMillis();
        if (maxWaitTimeMillis > 0) {
            scheduleThread.start();
        }
        consumeThread.start();
    }

    /**
     * 停止队列
     */
    public synchronized void stop() {
        if (!started) {
            return;
        }
        started = false;
        if (maxWaitTimeMillis > 0) {
            scheduleThread.interrupt();
        }
        signalConsumer();
    }

    /**
     * 阻塞添加元素
     *
     * @param item 队列元素
     * @throws InterruptedException 线程中断
     */
    public void put(T item) throws InterruptedException {
        ensureStarted();
        queue.put(Objects.requireNonNull(item, "item must not be null"));
        signalConsumer();
    }

    /**
     * 带超时时间添加元素
     *
     * @param item    队列元素
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return 是否添加成功
     * @throws InterruptedException 线程中断
     */
    public boolean offer(T item, long timeout, TimeUnit unit) throws InterruptedException {
        ensureStarted();
        boolean added = queue.offer(Objects.requireNonNull(item, "item must not be null"), timeout, unit);
        if (added) {
            signalConsumer();
        }
        return added;
    }

    /**
     * 判断队列是否为空
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    private void schedule() {
        while (started && !Thread.currentThread().isInterrupted()) {
            try {
                TimeUnit.MILLISECONDS.sleep(maxWaitTimeMillis);
                signalConsumer();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void consumeLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                List<T> items = waitAndDrain();
                if (!items.isEmpty()) {
                    batchConsumer.accept(items);
                }
                if (!started && queue.isEmpty()) {
                    return;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("批量消费队列异常", e);
            }
        }
    }

    private List<T> waitAndDrain() throws InterruptedException {
        consumeLock.lockInterruptibly();
        try {
            while (started && needWait()) {
                consumeCondition.await();
            }
            List<T> list = new ArrayList<>();
            queue.drainTo(list, batchSize);
            if (!list.isEmpty()) {
                lastConsumeTimeMillis = System.currentTimeMillis();
            }
            return list;
        } finally {
            consumeLock.unlock();
        }
    }

    private boolean needWait() {
        return sizeWait() && timeWait();
    }

    private boolean sizeWait() {
        return maxWaitSize <= 0 || queue.size() < maxWaitSize;
    }

    private boolean timeWait() {
        return maxWaitTimeMillis <= 0 || queue.isEmpty() || System.currentTimeMillis() - lastConsumeTimeMillis < maxWaitTimeMillis;
    }

    private void signalConsumer() {
        consumeLock.lock();
        try {
            consumeCondition.signal();
        } finally {
            consumeLock.unlock();
        }
    }

    private void ensureStarted() {
        if (!started) {
            throw new IllegalStateException("queue is not started");
        }
    }
}
