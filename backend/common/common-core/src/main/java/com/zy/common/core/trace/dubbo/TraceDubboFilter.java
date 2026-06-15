package com.zy.common.core.trace.dubbo;

import com.zy.common.core.trace.TraceConstants;
import com.zy.common.core.trace.TraceContext;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;

/**
 * Propagates traceId through Dubbo attachments.
 */
@Activate(group = {CommonConstants.CONSUMER, CommonConstants.PROVIDER})
public class TraceDubboFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String previousTraceId = TraceContext.getTraceId();
        String traceId = invocation.getAttachment(TraceConstants.TRACE_ID_ATTACHMENT);
        if (!TraceContext.isValidTraceId(traceId)) {
            traceId = TraceContext.getOrCreateTraceId();
        }
        invocation.setAttachment(TraceConstants.TRACE_ID_ATTACHMENT, traceId);
        TraceContext.setTraceId(traceId);
        try {
            return invoker.invoke(invocation);
        } finally {
            TraceContext.restore(previousTraceId);
        }
    }
}
