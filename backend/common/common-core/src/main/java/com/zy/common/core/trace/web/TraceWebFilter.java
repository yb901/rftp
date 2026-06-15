package com.zy.common.core.trace.web;

import com.zy.common.core.trace.TraceConstants;
import com.zy.common.core.trace.TraceContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Creates or propagates traceId for incoming HTTP requests.
 */
public class TraceWebFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String previousTraceId = TraceContext.getTraceId();
        String traceId = resolveTraceId(request);
        TraceContext.setTraceId(traceId);
        if (response instanceof HttpServletResponse httpServletResponse) {
            httpServletResponse.setHeader(TraceConstants.TRACE_ID_HEADER, traceId);
        }
        try {
            chain.doFilter(request, response);
        } finally {
            TraceContext.restore(previousTraceId);
        }
    }

    private String resolveTraceId(ServletRequest request) {
        if (request instanceof HttpServletRequest httpServletRequest) {
            String traceId = httpServletRequest.getHeader(TraceConstants.TRACE_ID_HEADER);
            if (TraceContext.isValidTraceId(traceId)) {
                return traceId;
            }
        }
        return TraceContext.normalizeOrCreate(null);
    }
}
