package com.zy.common.core.trace.xxljob;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;

import java.lang.reflect.Method;

/**
 * XXL-JOB Spring executor that wraps registered handlers with traceId propagation.
 */
public class TraceXxlJobSpringExecutor extends XxlJobSpringExecutor {

    @Override
    protected void registryJobHandler(XxlJob xxlJob, Object bean, Method executeMethod) {
        super.registryJobHandler(xxlJob, bean, executeMethod);
        if (xxlJob == null || xxlJob.value().trim().isEmpty()) {
            return;
        }
        String name = xxlJob.value();
        IJobHandler jobHandler = loadJobHandler(name);
        if (jobHandler == null || jobHandler instanceof TraceXxlJobHandler) {
            return;
        }
        registryJobHandler(name, new TraceXxlJobHandler(jobHandler));
    }
}
