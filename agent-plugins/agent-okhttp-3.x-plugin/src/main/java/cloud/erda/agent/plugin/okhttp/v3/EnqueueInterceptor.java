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

package cloud.erda.agent.plugin.okhttp.v3;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.utils.TracerUtils;
import okhttp3.Request;

/**
 * {@link EnqueueInterceptor} create a local span and the prefix of the span operation name is start with `Async` when
 * the `enqueue` method called and also put the `ContextSnapshot` and `RealCall` instance into the
 * `SkyWalkingDynamicField`.
 *
 * @author zhangxin
 */
public class EnqueueInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        EnhancedInstance instance = context.getInstance();
        Request request = (Request) instance.getDynamicField();

        Tracer tracer = TracerManager.tracer();
        SpanContext spanContext = tracer.active() != null ? tracer.active().span().getContext() : null;
        tracer.buildSpan("Async" + request.url().uri().getPath()).childOf(spanContext).startActive();

        /*
         * Here is the process about how to trace the async function.
         *
         * 1. Storage `Request` object into `RealCall` instance when the constructor of `RealCall` called.
         * 2. Put the `RealCall` instance to `CallBack` instance
         * 3. Get the `RealCall` instance from `CallBack` and then Put the `RealCall` into `AsyncCall` instance
         *    since the constructor of `RealCall` called.
         * 5. Create the exit span by using the `RealCall` instance when `AsyncCall` method called.
         */
        EnhancedInstance callbackInstance = (EnhancedInstance) context.getArguments()[0];
        callbackInstance.setDynamicField(new EnhanceRequiredInfo(instance, tracer.capture()));
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        TracerManager.tracer().active().close(false);
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        TracerUtils.handleException(t);
    }
}
