package com.zy.common.core.queue;

/**
 * 队列消费服务
 *
 * @param <T> 队列元素类型
 * @author zzy
 * @date 2026/06/08
 */
public interface QueueConsumeService<T> {

    /**
     * 阻塞添加到队列
     *
     * @param item 队列元素
     */
    void addQueue(T item);
}
