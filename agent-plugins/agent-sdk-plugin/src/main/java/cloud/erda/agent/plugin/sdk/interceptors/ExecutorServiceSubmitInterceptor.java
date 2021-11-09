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

package cloud.erda.agent.plugin.sdk.interceptors;

import cloud.erda.agent.core.tracing.Scope;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.TracerSnapshot;
import cloud.erda.agent.core.tracing.span.SpanBuilder;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.plugin.sdk.concurrent.CallableWrapper;
import cloud.erda.agent.plugin.sdk.concurrent.FutureWrapper;
import cloud.erda.agent.plugin.sdk.concurrent.RunnableWrapper;
import cloud.erda.msp.monitor.concurrent.ExecutorServiceAccessor;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * @author liuhaoyang
 * @date 2021/10/27 13:32
 */
public class ExecutorServiceSubmitInterceptor implements InstanceMethodsAroundInterceptor {

    private final static ILog logger = LogManager.getLogger(ExecutorServiceSubmitInterceptor.class);
    private final static String SCOPE_KEY = "SCOPE";
    private final static String SNAPSHOT_KEY = "SNAPSHOT";

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        Tracer tracer = TracerManager.currentTracer();
        Scope parent = tracer.active();
        SpanBuilder spanBuilder = tracer.buildSpan("ExecutorService Submit");
        if (parent != null) {
            spanBuilder.childOf(parent.span().getContext());
        }
        spanBuilder.tag(Constants.Tags.SPAN_LAYER, Constants.Tags.SPAN_LAYER_LOCAL);
        spanBuilder.tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_LOCAL);
        spanBuilder.tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_THREAD_POOL);
        if (context.getInstance() instanceof ExecutorServiceAccessor) {
            spanBuilder.tag("thread_pool_type", ((ExecutorServiceAccessor) context.getInstance()).getExecutorService().getClass().getName());
        }
        Scope scope = spanBuilder.startActive();
        TracerSnapshot tracerSnapshot = TracerManager.currentTracer().capture(scope);
        Object task = context.getArguments()[0];
        if (task instanceof Callable<?>) {
            context.getArguments()[0] = new CallableWrapper<>((Callable<?>) task, tracerSnapshot);
        } else if (task instanceof Runnable) {
            context.getArguments()[0] = new RunnableWrapper((Runnable) task, tracerSnapshot);
        }
        context.setAttachment(SCOPE_KEY, scope);
        context.setAttachment(SNAPSHOT_KEY, tracerSnapshot);
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        Scope scope = context.getAttachment(SCOPE_KEY);
//        TracerSnapshot tracerSnapshot = context.getAttachment(SNAPSHOT_KEY);
        try {
            return ret;
//            return new FutureWrapper<>((Future<?>) ret, tracerSnapshot);
        } finally {
            scope.close();
        }
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        Scope scope = context.getAttachment(SCOPE_KEY);
        scope.span().tag(Constants.Tags.ERROR, Constants.Tags.ERROR_TRUE);
    }
}
