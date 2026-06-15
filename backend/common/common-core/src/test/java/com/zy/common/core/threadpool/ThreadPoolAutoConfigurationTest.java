package com.zy.common.core.threadpool;

import com.zy.common.core.trace.TraceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThreadPoolAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ThreadPoolAutoConfiguration.class));

    @Test
    void shouldCreateExecutorServiceByThreadPoolName() {
        contextRunner
                .withPropertyValues(
                        "threadpool.order-push.enabled=true",
                        "threadpool.order-push.core-size=2",
                        "threadpool.order-push.max-size=4",
                        "threadpool.order-push.queue-size=16",
                        "threadpool.order-push.keep-alive-seconds=30")
                .run(context -> {
                    ExecutorService executorService = context.getBean("orderPushExecutorService", ExecutorService.class);
                    assertThat(executorService).isInstanceOf(ThreadPoolExecutor.class);

                    ThreadPoolExecutor executor = (ThreadPoolExecutor) executorService;
                    assertThat(executor.getCorePoolSize()).isEqualTo(2);
                    assertThat(executor.getMaximumPoolSize()).isEqualTo(4);
                    assertThat(executor.getKeepAliveTime(java.util.concurrent.TimeUnit.SECONDS)).isEqualTo(30);
                    assertThat(executor.getQueue()).isInstanceOf(LinkedBlockingQueue.class);
                    assertThat(executor.getThreadFactory().newThread(() -> {
                    }).getName()).startsWith("orderPushExecutorService-");
                });
    }

    @Test
    void shouldUseCoreSizeWhenMaxSizeIsSmaller() {
        contextRunner
                .withPropertyValues(
                        "threadpool.async.enabled=true",
                        "threadpool.async.core-size=3",
                        "threadpool.async.max-size=1",
                        "threadpool.async.queue-size=0",
                        "threadpool.async.keep-alive-seconds=10")
                .run(context -> {
                    ThreadPoolExecutor executor = context.getBean("asyncExecutorService", ThreadPoolExecutor.class);
                    assertThat(executor.getCorePoolSize()).isEqualTo(3);
                    assertThat(executor.getMaximumPoolSize()).isEqualTo(3);
                    assertThat(executor.getQueue()).isInstanceOf(SynchronousQueue.class);
                });
    }

    @Test
    void shouldPropagateTraceIdToExecutorServiceTask() {
        contextRunner
                .withPropertyValues(
                        "threadpool.trace.enabled=true",
                        "threadpool.trace.core-size=1",
                        "threadpool.trace.max-size=1",
                        "threadpool.trace.queue-size=1",
                        "threadpool.trace.keep-alive-seconds=10")
                .run(context -> {
                    ExecutorService executorService = context.getBean("traceExecutorService", ExecutorService.class);
                    CountDownLatch latch = new CountDownLatch(1);
                    AtomicReference<String> traceId = new AtomicReference<>();
                    TraceContext.setTraceId("20260603153022123a7k9x2M2");
                    try {
                        executorService.execute(() -> {
                            traceId.set(TraceContext.getTraceId());
                            latch.countDown();
                        });
                        assertThat(await(latch)).isTrue();
                        assertThat(traceId.get()).isEqualTo("20260603153022123a7k9x2M2");
                    } finally {
                        TraceContext.clear();
                    }
                });
    }

    @Test
    void shouldIgnoreDisabledExecutorService() {
        contextRunner
                .withPropertyValues(
                        "threadpool.report.enabled=false",
                        "threadpool.report.core-size=1",
                        "threadpool.report.max-size=1",
                        "threadpool.report.queue-size=1",
                        "threadpool.report.keep-alive-seconds=1")
                .run(context -> assertThat(context).doesNotHaveBean("reportExecutorService"));
    }

    @Test
    void shouldThrowRejectedExecutionExceptionWithPoolMetrics() {
        ThreadPoolRejectedExecutionHandler handler = new ThreadPoolRejectedExecutionHandler("testExecutorService");
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1,
                1,
                0L,
                java.util.concurrent.TimeUnit.SECONDS,
                new SynchronousQueue<>());

        assertThatThrownBy(() -> handler.rejectedExecution(() -> {
        }, executor))
                .isInstanceOf(RejectedExecutionException.class)
                .hasMessageContaining("Thread pool : testExecutorService is EXHAUSTED!")
                .hasMessageContaining("当前线程数(PoolSize)")
                .hasMessageContaining("已完成任务数(CompletedTaskCount)");

        executor.shutdown();
    }

    private boolean await(CountDownLatch latch) {
        try {
            return latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
