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

import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.tracing.span.SpanBuilder;
import cloud.erda.agent.core.utils.Constants;
import org.apache.skywalking.apm.agent.core.util.Strings;
import cloud.erda.agent.core.utils.TracerUtils;
import cloud.erda.agent.plugin.app.insight.AppMetricBuilder;
import cloud.erda.agent.plugin.app.insight.AppMetricRecorder;
import cloud.erda.agent.plugin.app.insight.AppMetricUtils;

import java.lang.reflect.Method;

public class JedisMethodInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        EnhancedInstance objInst = context.getInstance();
        Method method = context.getMethod();
        Object[] allArguments = context.getArguments();

        String peer = String.valueOf(objInst.getDynamicField());

        Tracer tracer = TracerManager.tracer();
        SpanContext spanContext = tracer.active() != null ? tracer.active().span().getContext() : null;
        SpanBuilder spanBuilder = tracer.buildSpan("Jedis/" + method.getName());
        Span span = spanBuilder.childOf(spanContext).startActive().span();
        span.tag(Constants.Tags.DB_TYPE, Constants.Tags.COMPONENT_REDIS);
        span.tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_REDIS);
        span.tag(Constants.Tags.PEER_SERVICE, peer);
        span.tag(Constants.Tags.SPAN_LAYER, Constants.Tags.SPAN_LAYER_CACHE);
        span.tag(Constants.Tags.HOST, peer);
        if (allArguments.length == 0) {
            return;
        }

        String key;
        if (allArguments[0] instanceof String) {
            key = (String)allArguments[0];
        } else if (allArguments[0] instanceof byte[]) {
            key = new String((byte[])allArguments[0]);
        } else {
            return;
        }
        String statement = (method.getName() + " " + key).replace("\n", "");
        span.tag(Constants.Tags.DB_STATEMENT, statement);

        if (Strings.isEmpty(statement)) {
            return;
        }
        AppMetricBuilder appMetricBuilder = new AppMetricBuilder(Constants.Metrics.APPLICATION_CACHE, false);
        context.setAttachment(Constants.Keys.METRIC_BUILDER, appMetricBuilder);
        appMetricBuilder.tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_REDIS)
            .tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_CLIENT)
            .tag(Constants.Tags.PEER_SERVICE, peer)
            .tag(Constants.Tags.HOST, peer)
            .tag(Constants.Tags.DB_STATEMENT, statement)
            .tag(Constants.Tags.DB_TYPE, Constants.Tags.COMPONENT_REDIS);
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        AppMetricBuilder appMetricBuilder = context.getAttachment(Constants.Keys.METRIC_BUILDER);
        if (appMetricBuilder != null) {
            AppMetricRecorder.record(appMetricBuilder);
        }
        TracerManager.tracer().active().close();
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        AppMetricUtils.handleException(context);
        TracerUtils.handleException(t);
    }
}