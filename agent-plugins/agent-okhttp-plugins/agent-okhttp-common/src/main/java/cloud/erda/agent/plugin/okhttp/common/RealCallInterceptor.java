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

import cloud.erda.agent.core.tracing.Scope;
import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.TracerUtils;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricBuilder;
import cloud.erda.agent.plugin.app.insight.MetricReporter;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*;

/**
 * {@link RealCallInterceptor} intercept the synchronous http calls by the discovery of okhttp.
 *
 * @author peng-yongsheng
 */
public class RealCallInterceptor implements InstanceMethodsAroundInterceptor, InstanceConstructorInterceptor {

    private static ILog log = LogManager.getLogger(RealCallInterceptor.class);

    @Override
    public void onConstruct(Object objInst, Object[] allArguments) {
        ((DynamicFieldEnhancedInstance)objInst).setDynamicField(allArguments[1]);
    }

    /**
     * Get the {@link Request} from {@link EnhancedInstance}, then create {@link Span} and set host, port, kind,
     * component, url from {@link Request}. Through the reflection of the way, set the http header of context
     * data into {@link Request#headers}.
     *
     * @param context
     * @param result  change this result, if you want to truncate the method.
     * @throws Throwable
     */
    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        Request request = (Request) ((DynamicFieldEnhancedInstance) context.getInstance()).getDynamicField();

        Tracer tracer = TracerManager.currentTracer();
        SpanContext spanContext = tracer.active() != null ? tracer.active().span().getContext() : null;
        Span span = tracer.buildSpan("HTTP " + request.method()).childOf(spanContext).startActive().span();

        CallInterceptorUtils.wrapRequestSpan(span, request);

        // 修改：获取新的带 header 的 request
        Request newRequest = CallInterceptorUtils.injectRequestHeader(request, span);

        // 替换原始 request（必须设置回 context 实例的 dynamic field，否则后续仍旧使用旧 request）
        ((DynamicFieldEnhancedInstance) context.getInstance()).setDynamicField(newRequest);

        // Metric 构建
        TransactionMetricBuilder transactionMetricBuilder = CallInterceptorUtils.createRequestAppMetric(newRequest);
        context.setAttachment(Constants.Keys.METRIC_BUILDER, transactionMetricBuilder);
    }


    /**
     * Get the status code from {@link Response}, when status code greater than 400, it means there was some errors in
     * the server. Finish the {@link Span}.
     *
     * @param context
     * @param ret     the method's original return value.
     * @return
     * @throws Throwable
     */
    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        Response response = (Response) ret;

        TransactionMetricBuilder transactionMetricBuilder = context.getAttachment(Constants.Keys.METRIC_BUILDER);
        if (transactionMetricBuilder != null) {
            transactionMetricBuilder = CallInterceptorUtils.wrapResponseAppMetric(transactionMetricBuilder, response);
            MetricReporter.report(transactionMetricBuilder);
        }

        Scope scope = TracerManager.currentTracer().active();
        CallInterceptorUtils.wrapResponseSpan(scope.span(), response);
        scope.close();
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        TracerUtils.handleException(t);
    }
}
