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

package cloud.erda.agent.plugin.redisson.v3;

import cloud.erda.agent.core.tracing.Scope;
import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.tracing.span.SpanBuilder;
import cloud.erda.agent.core.utils.Constants;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

import java.lang.reflect.Method;

/**
 * @author liuhaoyang
 * @date 2021/11/1 21:44
 */
public class ConnectionOpInterceptor implements InstanceMethodsAroundInterceptor {

    private final static ILog logger = LogManager.getLogger(ConnectionOpInterceptor.class);

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        Tracer tracer = TracerManager.currentTracer();
        SpanContext spanContext = tracer.active() != null ? tracer.active().span().getContext() : null;
        SpanBuilder spanBuilder = tracer.buildSpan("Redisson ConnectionManager Connection");
        Scope scope = spanBuilder.childOf(spanContext).startActive();
        Span span = scope.span();
        span.tag(Constants.Tags.DB_SYSTEM, Constants.Tags.DB_TYPE_REDIS);
        span.tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_REDISSON);
        span.tag(Constants.Tags.SPAN_LAYER, Constants.Tags.SPAN_LAYER_LOCAL);
        span.tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_LOCAL);
        span.tag("connection_mode", context.getMethod().getName().equals("connectionReadOp") ? "ReadOnlyMode" : "ReadWriteMode");
        context.setAttachment(Constants.Keys.TRACE_SCOPE, scope);
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        Method method = RFutureMethods.getRFMethod(ret.getClass());
        Scope scope = context.getAttachment(Constants.Keys.TRACE_SCOPE);
        if (method == null) {
            scope.close();
        } else {
            method.invoke(ret, method.getName().equals("onComplete") ? new RFutureTraceConsumer<>(scope) : new RFutureTraceListener<>(scope));
        }
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
    }
}
