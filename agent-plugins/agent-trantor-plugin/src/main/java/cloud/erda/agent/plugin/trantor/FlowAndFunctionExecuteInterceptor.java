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

package cloud.erda.agent.plugin.trantor;

import cloud.erda.agent.core.tracing.Scope;
import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.span.SpanBuilder;
import cloud.erda.agent.core.utils.Constants;
import io.terminus.trantorframework.api.annotation.FlowImpl;
import io.terminus.trantorframework.api.annotation.FunctionImpl;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

import java.lang.annotation.Annotation;

/**
 * @author liuhaoyang
 * @date 2021/10/29 15:16
 */
public class FlowAndFunctionExecuteInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        Tracer tracer = TracerManager.tracer();
        SpanContext parent = tracer.active() != null ? tracer.active().span().getContext() : null;
        SpanBuilder spanBuilder = getSpanBuilder(context, tracer);
        Scope scope = spanBuilder.childOf(parent).startActive();
        context.setAttachment(Constants.Keys.TRACE_SCOPE, scope);
    }

    private SpanBuilder getSpanBuilder(IMethodInterceptContext context, Tracer tracer) {
        String invokeType = "";
        String invokeName = "";
        for (Annotation annotation : context.getOriginClass().getDeclaredAnnotations()) {
            if (annotation instanceof FlowImpl) {
                invokeType = "Flow";
                invokeName = ((FlowImpl) annotation).name();
                continue;
            }
            if (annotation instanceof FunctionImpl) {
                invokeType = "Function";
                invokeName = ((FunctionImpl) annotation).name();
                continue;
            }
        }
        SpanBuilder builder = tracer.buildSpan("Trantor " + invokeType);
        builder.tag(Constants.Tags.COMPONENT, Constants.Tags.TRANTOR);
        builder.tag(Constants.Tags.CLASS, context.getOriginClass().getName());
        builder.tag(Constants.Tags.METHOD, context.getMethod().getName());
        builder.tag(invokeType.toLowerCase() + "_name", invokeName);
        builder.tag(Constants.Tags.SPAN_LAYER, Constants.Tags.SPAN_LAYER_LOCAL);
        builder.tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_LOCAL);
        return builder;
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        Scope scope = context.getAttachment(Constants.Keys.TRACE_SCOPE);
        try {
            return ret;
        } finally {
            if (scope != null) {
                scope.close();
            }
        }
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        Scope scope = context.getAttachment(Constants.Keys.TRACE_SCOPE);
        if (scope != null) {
            scope.span().tag(Constants.Tags.ERROR, Constants.Tags.ERROR_TRUE);
        }
    }
}
