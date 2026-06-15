package com.zy.common.core.threadpool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 通用线程池命名线程工厂
 *
 * @author zzy
 * @date 2026/05/18
 */
public class ThreadPoolNamedThreadFactory implements ThreadFactory {

    /**
     * 线程名称前缀
     */
    private final String threadNamePrefix;

    /**
     * 线程序号
     */
    private final AtomicInteger threadIndex = new AtomicInteger(1);

    public ThreadPoolNamedThreadFactory(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setName(threadNamePrefix + "-" + threadIndex.getAndIncrement());
        return thread;
    }
}
