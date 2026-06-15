package com.zy.common.core.queue;

import org.springframework.beans.factory.DisposableBean;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 批量消费队列抽象服务
 *
 * @param <T> 队列元素类型
 * @author zzy
 * @date 2026/06/08
 */
public abstract class AbstractQueueService<T> implements QueueConsumeService<T>, DisposableBean {

    /**
     * 批量消费阻塞队列
     */
    private final BatchConsumeBlockingQueue<T> queue;

    /**
     * 构造批量消费队列服务
     *
     * @param capacity          队列容量，非正数表示无界
     * @param batchSize         单次消费数量
     * @param maxWaitSize       数量触发阈值
     * @param maxWaitTimeMillis 最大等待毫秒数
     */
    protected AbstractQueueService(int capacity, int batchSize, int maxWaitSize, int maxWaitTimeMillis) {
        this.queue = new BatchConsumeBlockingQueue<>(
                capacity,
                batchSize,
                maxWaitSize,
                maxWaitTimeMillis,
                getClass().getSimpleName(),
                this::batchConsume
        );
        this.queue.start();
    }

    /**
     * 阻塞添加到队列
     *
     * @param item 队列元素
     */
    @Override
    public void addQueue(T item) {
        try {
            queue.put(item);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("添加队列被中断", e);
        }
    }

    /**
     * 带超时时间添加到队列
     *
     * @param item    队列元素
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return 是否添加成功
     */
    public boolean addQueue(T item, long timeout, TimeUnit unit) {
        try {
            return queue.offer(item, timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("添加队列被中断", e);
        }
    }

    /**
     * 批量消费队列消息
     *
     * @param list 队列元素列表
     */
    protected void batchConsume(List<T> list) {
        batchConsumeMsg(list);
    }

    /**
     * 批量消费队列消息
     *
     * @param list 队列元素列表
     */
    protected abstract void batchConsumeMsg(List<T> list);

    /**
     * 销毁队列服务
     */
    @Override
    public void destroy() throws Exception {
        queue.stop();
        for (int i = 0; i < 3 && !queue.isEmpty(); i++) {
            TimeUnit.SECONDS.sleep(2);
        }
    }
}
