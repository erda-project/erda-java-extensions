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

package cloud.erda.agent.plugin.lettuce.v5;

import io.lettuce.core.protocol.AsyncCommand;
import cloud.erda.agent.core.utils.Constants;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.TracerSnapshot;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.tracing.span.SpanBuilder;
import cloud.erda.agent.core.utils.TracerUtils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AsyncCommandMethodInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        AsyncCommand asyncCommand = (AsyncCommand) context.getInstance();
        String operationName = "Lettuce/" + asyncCommand.getType().name();
        Tracer tracer = TracerManager.currentTracer();
        SpanContext spanContext = tracer.active() != null ? tracer.active().span().getContext() : null;
        SpanBuilder spanBuilder = tracer.buildSpan(operationName + "/onComplete");
        Span span = spanBuilder.childOf(spanContext).startActive().span();
        span.tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_LETTUCE);
        span.tag(Constants.Tags.DB_TYPE, Constants.Tags.DB_TYPE_REDIS);
        span.tag(Constants.Tags.SPAN_LAYER, Constants.Tags.SPAN_LAYER_CACHE);
        TracerSnapshot snapshot = TracerManager.currentTracer().capture();
        if (context.getArguments()[0] instanceof Consumer) {
            context.getArguments()[0] = new SpotConsumer((Consumer) context.getArguments()[0], snapshot, operationName);
        } else {
            context.getArguments()[0] = new SpotBiConsumer((BiConsumer) context.getArguments()[0], snapshot, operationName);
        }
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        TracerManager.currentTracer().active().close();
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        TracerUtils.handleException(t);
    }
}
