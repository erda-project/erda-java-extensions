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

package cloud.erda.agent.plugin.jedis.v2;

import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.tracing.span.SpanBuilder;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.TracerUtils;
import cloud.erda.agent.plugin.app.insight.MetricReporter;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricBuilder;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricUtils;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.DynamicFieldEnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

import java.lang.reflect.Method;

public class JedisMethodInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        Object objInst = context.getInstance();
        Method method = context.getMethod();
        Object[] allArguments = context.getArguments();

        String peer = String.valueOf(((DynamicFieldEnhancedInstance)objInst).getDynamicField());

        String key = "";
        // 添加参数检查，防止空指针异常
        if (allArguments != null && allArguments.length > 0 && allArguments[0] != null) {
            if (allArguments[0] instanceof String) {
                key = (String) allArguments[0];
            } else if (allArguments[0] instanceof byte[]) {
                key = new String((byte[]) allArguments[0]);
            } else if (allArguments[0] instanceof Integer) {
                key = String.valueOf(allArguments[0]);
            }
        }
        String statement = (method.getName() + " " + key).replace("\n", "");

        Tracer tracer = TracerManager.currentTracer();
        SpanContext spanContext = tracer.active() != null ? tracer.active().span().getContext() : null;
        SpanBuilder spanBuilder = tracer.buildSpan("Jedis/" + method.getName());
        Span span = spanBuilder.childOf(spanContext).startActive().span();
        span.tag(Constants.Tags.DB_SYSTEM, Constants.Tags.DB_TYPE_REDIS);
        span.tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_JEDIS);
        span.tag(Constants.Tags.PEER_SERVICE, peer);
        span.tag(Constants.Tags.SPAN_LAYER, Constants.Tags.SPAN_LAYER_CACHE);
        span.tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_CLIENT);
        span.tag(Constants.Tags.DB_HOST, peer);
        span.tag(Constants.Tags.DB_STATEMENT, statement);
        span.tag(Constants.Tags.PEER_ADDRESS, peer);
        span.tag(Constants.Tags.PEER_HOSTNAME, peer);

        TransactionMetricBuilder transactionMetricBuilder = new TransactionMetricBuilder(Constants.Metrics.APPLICATION_CACHE, false);
        context.setAttachment(Constants.Keys.METRIC_BUILDER, transactionMetricBuilder);
        transactionMetricBuilder
                .tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_JEDIS)
                .tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_CLIENT)
                .tag(Constants.Tags.PEER_SERVICE, peer)
                .tag(Constants.Tags.PEER_ADDRESS, peer)
                .tag(Constants.Tags.PEER_HOSTNAME, peer)
                .tag(Constants.Tags.DB_HOST, peer)
                .tag(Constants.Tags.DB_STATEMENT, statement)
                .tag(Constants.Tags.DB_SYSTEM, Constants.Tags.DB_TYPE_REDIS);
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        TransactionMetricBuilder transactionMetricBuilder = context.getAttachment(Constants.Keys.METRIC_BUILDER);
        if (transactionMetricBuilder != null) {
            MetricReporter.report(transactionMetricBuilder);
        }
        // 添加null检查，防止active()返回null导致空指针异常
        Tracer tracer = TracerManager.currentTracer();
        if (tracer != null && tracer.active() != null) {
            tracer.active().close();
        }
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        TransactionMetricUtils.handleException(context);
        TracerUtils.handleException(t);
    }
}