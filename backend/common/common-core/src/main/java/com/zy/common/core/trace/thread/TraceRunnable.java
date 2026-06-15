package com.zy.common.core.trace.thread;

import com.zy.common.core.trace.TraceContext;

/**
 * Runnable wrapper that propagates traceId to worker threads.
 */
public class TraceRunnable implements Runnable {

    private final Runnable delegate;

    private final String traceId;

    public TraceRunnable(Runnable delegate) {
        this.delegate = delegate;
        this.traceId = TraceContext.getTraceId();
    }

    @Override
    public void run() {
        String previousTraceId = TraceContext.getTraceId();
        TraceContext.restore(traceId);
        try {
            delegate.run();
        } finally {
            TraceContext.restore(previousTraceId);
        }
    }
}
