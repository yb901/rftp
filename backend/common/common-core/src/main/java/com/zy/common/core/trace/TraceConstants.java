package com.zy.common.core.trace;

/**
 * Trace constants shared by HTTP, Dubbo, MQ and logs.
 */
public final class TraceConstants {

    public static final String TRACE_ID = "traceId";

    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    public static final String TRACE_ID_ATTACHMENT = TRACE_ID;

    private TraceConstants() {
    }
}
