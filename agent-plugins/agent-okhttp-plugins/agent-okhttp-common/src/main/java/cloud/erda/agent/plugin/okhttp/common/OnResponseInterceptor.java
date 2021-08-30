/*
 * Copyright (c) 2021 Terminus, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.erda.agent.plugin.okhttp.common;

import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.TracerUtils;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricBuilder;
import okhttp3.Response;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

/**
 * {@link OnResponseInterceptor} validate the response code if it is great equal than 400. if so. the transaction status
 * chang to `error`, or do nothing.
 *
 * @author zhangxin
 */
public class OnResponseInterceptor implements InstanceMethodsAroundInterceptor {

    private static ILog log = LogManager.getLogger(OnResponseInterceptor.class);

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        Response response = (Response) context.getArguments()[1];
        log.info("FFFF======  " + Thread.currentThread().getId());
        Span span = TracerManager.tracer().active().span();
        CallInterceptorUtils.wrapResponseSpan(span, response);

        TransactionMetricBuilder transactionMetricBuilder =
                TracerManager.tracer().context().getAttachment(Constants.Keys.METRIC_BUILDER);
        if (transactionMetricBuilder != null) {
            transactionMetricBuilder = CallInterceptorUtils.wrapResponseAppMetric(transactionMetricBuilder, response);
            TracerManager.tracer().context().setAttachment(Constants.Keys.METRIC_BUILDER, transactionMetricBuilder);
        }
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        TracerUtils.handleException(t);
    }
}
