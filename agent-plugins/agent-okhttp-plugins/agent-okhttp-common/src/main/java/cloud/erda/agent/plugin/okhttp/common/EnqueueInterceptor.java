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

import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.utils.TracerUtils;
import okhttp3.Request;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*;

/**
 * {@link EnqueueInterceptor} create a local span and the prefix of the span operation name is start with `Async` when
 * the `enqueue` method called and also put the `ContextSnapshot` and `RealCall` instance into the
 * `SkyWalkingDynamicField`.
 *
 * @author zhangxin
 */
public class EnqueueInterceptor implements InstanceMethodsAroundInterceptor,InstanceConstructorInterceptor {

    private static ILog log = LogManager.getLogger(EnqueueInterceptor.class);

    /*
     * Here is the process about how to trace the async function.
     *
     * 1. Storage `Request` object into `RealCall` instance when the constructor of `RealCall` called.
     * 2. Put the `RealCall` instance to `CallBack` instance
     * 3. Get the `RealCall` instance from `CallBack` and then Put the `RealCall` into `AsyncCall` instance
     *    since the constructor of `RealCall` called.
     * 5. Create the exit span by using the `RealCall` instance when `AsyncCall` method called.
     */
    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {

        DynamicFieldEnhancedInstance callbackInstance = (DynamicFieldEnhancedInstance) context.getArguments()[0];
        Request request = (Request)  ((DynamicFieldEnhancedInstance)context.getInstance()).getDynamicField();

        Tracer tracer = TracerManager.tracer();
        SpanContext spanContext = tracer.active() != null ? tracer.active().span().getContext() : null;
        tracer.buildSpan("Async" + request.url().uri().getPath()).childOf(spanContext).startActive();

//        TransactionMetricBuilder transactionMetricBuilder = CallInterceptorUtils.createRequestAppMetric(request);
//        if (transactionMetricBuilder != null) {
//            tracer.context().setAttachment(Constants.Keys.METRIC_BUILDER, transactionMetricBuilder);
//        }

        callbackInstance.setDynamicField(new EnhanceRequiredInfo(context.getInstance(), tracer.capture()));
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

    @Override
    public void onConstruct(EnhancedInstance objInst, Object[] allArguments) {
        ((DynamicFieldEnhancedInstance)objInst).setDynamicField(allArguments[1]);
    }
}
