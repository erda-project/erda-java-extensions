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
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.TracerSnapshot;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.TracerUtils;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricBuilder;
import cloud.erda.agent.plugin.app.insight.MetricReporter;
import okhttp3.Request;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*;

/**
 * {@link AsyncCallInterceptor} get the `EnhanceRequiredInfo` instance from `SkyWalkingDynamicField` and then put it
 * into `AsyncCall` instance when the `AsyncCall` constructor called.
 * <p>
 * {@link AsyncCallInterceptor} also create an exit span by using the `EnhanceRequiredInfo` when the `execute` method
 * called.
 *
 * @author zhangxin
 */
public class AsyncCallInterceptor implements InstanceConstructorInterceptor, InstanceMethodsAroundInterceptor {

    private static ILog log = LogManager.getLogger(AsyncCallInterceptor.class);

    @Override
    public void onConstruct(EnhancedInstance objInst, Object[] allArguments) {
        /*
         * The first argument of constructor is not the `real` parameter when the enhance class is an inner class. This
         * is the JDK compiler mechanism.
         */
        DynamicFieldEnhancedInstance realCallInstance = (DynamicFieldEnhancedInstance) allArguments[1];
        Object enhanceRequireInfo = realCallInstance.getDynamicField();

        ((DynamicFieldEnhancedInstance)objInst).setDynamicField(enhanceRequireInfo);
    }

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        EnhanceRequiredInfo enhanceRequiredInfo = (EnhanceRequiredInfo) ((DynamicFieldEnhancedInstance) context.getInstance()).getDynamicField();
        Request request = (Request) ((DynamicFieldEnhancedInstance) enhanceRequiredInfo.getRealCallEnhance()).getDynamicField();

        Tracer tracer = TracerManager.currentTracer();
        TracerSnapshot snapshot = enhanceRequiredInfo.getTracerSnapshot();
        Span span = tracer.attach(snapshot).span();

        CallInterceptorUtils.wrapRequestSpan(span, request);
        CallInterceptorUtils.injectRequestHeader(request, span);

        TransactionMetricBuilder transactionMetricBuilder = CallInterceptorUtils.createRequestAppMetric(request);
        if (transactionMetricBuilder != null) {
            tracer.context().setAttachment(Constants.Keys.METRIC_BUILDER, transactionMetricBuilder);
        }
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        TransactionMetricBuilder builder = TracerManager.currentTracer().context().getAttachment(Constants.Keys.METRIC_BUILDER);
        if (builder != null) {
            MetricReporter.report(builder);
        }

        Scope scope = TracerManager.currentTracer().active();
        if (scope != null) {
            scope.close();
        }
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        TracerUtils.handleException(t);
    }
}
