package com.zy.common.core.trace.xxljob;

import com.xxl.job.core.handler.IJobHandler;
import com.zy.common.core.trace.TraceContext;

/**
 * XXL-JOB handler wrapper that creates a traceId for each execution.
 */
public class TraceXxlJobHandler extends IJobHandler {

    private final IJobHandler delegate;

    public TraceXxlJobHandler(IJobHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute() throws Exception {
        String previousTraceId = TraceContext.getTraceId();
        TraceContext.setTraceId(TraceContext.normalizeOrCreate(null));
        try {
            delegate.execute();
        } finally {
            TraceContext.restore(previousTraceId);
        }
    }

    @Override
    public void init() throws Exception {
        delegate.init();
    }

    @Override
    public void destroy() throws Exception {
        delegate.destroy();
    }

    @Override
    public String toString() {
        return "TraceXxlJobHandler{" + delegate + '}';
    }
}
