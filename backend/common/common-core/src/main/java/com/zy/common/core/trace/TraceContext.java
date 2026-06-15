package com.zy.common.core.trace;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.util.regex.Pattern;

/**
 * Trace context backed by SLF4J MDC.
 */
public final class TraceContext {

    private static final int MAX_TRACE_ID_LENGTH = 64;

    private static final Pattern TRACE_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9]{1,64}$");

    private TraceContext() {
    }

    public static String getTraceId() {
        return MDC.get(TraceConstants.TRACE_ID);
    }

    public static String getOrCreateTraceId() {
        String traceId = getTraceId();
        if (isValidTraceId(traceId)) {
            return traceId;
        }
        traceId = TraceIdGenerator.generate();
        setTraceId(traceId);
        return traceId;
    }

    public static String normalizeOrCreate(String traceId) {
        if (isValidTraceId(traceId)) {
            return traceId;
        }
        return TraceIdGenerator.generate();
    }

    public static void setTraceId(String traceId) {
        if (StringUtils.isBlank(traceId)) {
            clear();
            return;
        }
        MDC.put(TraceConstants.TRACE_ID, traceId);
    }

    public static void restore(String previousTraceId) {
        if (StringUtils.isBlank(previousTraceId)) {
            clear();
            return;
        }
        setTraceId(previousTraceId);
    }

    public static void clear() {
        MDC.remove(TraceConstants.TRACE_ID);
    }

    public static boolean isValidTraceId(String traceId) {
        if (StringUtils.isBlank(traceId) || traceId.length() > MAX_TRACE_ID_LENGTH) {
            return false;
        }
        return TRACE_ID_PATTERN.matcher(traceId).matches();
    }
}
