package com.zy.common.core.threadpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 通用线程池拒绝策略
 *
 * @author zzy
 * @date 2026/05/18
 */
public class ThreadPoolRejectedExecutionHandler implements RejectedExecutionHandler {

    private static final Logger log = LoggerFactory.getLogger(ThreadPoolRejectedExecutionHandler.class);

    /**
     * 线程池 Bean 名称
     */
    private final String executorServiceName;

    public ThreadPoolRejectedExecutionHandler(String executorServiceName) {
        this.executorServiceName = executorServiceName;
    }

    @Override
    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
        String msg = buildRejectedMessage(executor);
        log.error(msg);
        throw new RejectedExecutionException(msg);
    }

    /**
     * 构造拒绝执行异常信息
     *
     * @param executor 线程池执行器
     * @return 异常信息
     */
    private String buildRejectedMessage(ThreadPoolExecutor executor) {
        if (executor == null) {
            return "Thread pool : " + executorServiceName + " is EXHAUSTED!";
        }
        int queueSize = executor.getQueue() == null ? 0 : executor.getQueue().size();
        return "Thread pool : " + executorServiceName + " is EXHAUSTED!"
                + " 当前线程池状态："
                + " 当前线程数(PoolSize)=" + executor.getPoolSize()
                + "，活跃线程数(ActiveCount)=" + executor.getActiveCount()
                + "，核心线程数(CorePoolSize)=" + executor.getCorePoolSize()
                + "，最大线程数(MaximumPoolSize)=" + executor.getMaximumPoolSize()
                + "，历史最大线程数(LargestPoolSize)=" + executor.getLargestPoolSize()
                + "，队列任务数(QueueSize)=" + queueSize
                + "，已提交任务数(TaskCount)=" + executor.getTaskCount()
                + "，已完成任务数(CompletedTaskCount)=" + executor.getCompletedTaskCount();
    }
}
