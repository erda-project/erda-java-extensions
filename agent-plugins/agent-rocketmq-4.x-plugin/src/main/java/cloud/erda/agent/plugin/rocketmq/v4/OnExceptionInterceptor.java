/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package cloud.erda.agent.plugin.rocketmq.v4;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.DynamicFieldEnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.utils.TracerUtils;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricBuilder;
import cloud.erda.agent.plugin.app.insight.MetricReporter;

import static cloud.erda.agent.core.utils.Constants.Tags.*;

/**
 * {@link OnExceptionInterceptor} create local span when the method
 * {@link org.apache.rocketmq.client.producer.SendCallback#onException(Throwable)}
 * execute.
 *
 * @author zhang xin
 */
public class OnExceptionInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        MessageSendAsyncInfo info = (MessageSendAsyncInfo) ((DynamicFieldEnhancedInstance) context.getInstance()).getDynamicField();
        if (info == null) {
            return;
        }

        TransactionMetricBuilder builder = info.getAppMetricBuilder();
        if (builder != null) {
            builder.tag(ERROR, ERROR_TRUE);
        }

        Span span = TracerManager.currentTracer().attach(info.getTracerSnapshot()).span();
        span.updateName("RocketMQ/OnException");
        span.tag(COMPONENT, COMPONENT_ROCKETMQ);
        span.tag(SPAN_LAYER, SPAN_LAYER_MQ);
        span.tag(ERROR, ERROR_TRUE);
        span.tag(ERROR_MESSAGE, ((Throwable) context.getArguments()[0]).getMessage());
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        MessageSendAsyncInfo info = (MessageSendAsyncInfo) ((DynamicFieldEnhancedInstance) context.getInstance()).getDynamicField();
        if (info == null) {
            return ret;
        }

        TransactionMetricBuilder builder = info.getAppMetricBuilder();
        if (builder != null) {
            MetricReporter.report(builder);
        }

        // 添加null检查，防止active()返回null导致空指针异常
        cloud.erda.agent.core.tracing.Tracer tracer = TracerManager.currentTracer();
        if (tracer != null && tracer.active() != null) {
            tracer.active().close();
        }
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        MessageSendAsyncInfo info = (MessageSendAsyncInfo) ((DynamicFieldEnhancedInstance) context.getInstance()).getDynamicField();
        if (info == null) {
            return;
        }

        TransactionMetricBuilder builder = info.getAppMetricBuilder();
        if (builder != null) {
            builder.tag(ERROR, ERROR_TRUE);
        }

        TracerUtils.handleException(t);
    }
}
