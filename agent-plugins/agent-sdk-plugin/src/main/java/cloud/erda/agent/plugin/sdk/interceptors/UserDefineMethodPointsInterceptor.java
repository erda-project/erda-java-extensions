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
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.span.LogFields;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.DateTime;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.StaticMethodsAroundInterceptor;

/**
 * @author liuhaoyang
 * @date 2021/10/20 19:23
 */
public class UserDefineMethodPointsInterceptor implements StaticMethodsAroundInterceptor, InstanceMethodsAroundInterceptor {

    public final static String INTERCEPTOR_CLASS = "cloud.erda.agent.plugin.sdk.interceptors.UserDefineMethodPointsInterceptor";
    private final static String START_TIME_KEY = "START_TIME";

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) {
        Long start = DateTime.currentTimeNano();
        context.setAttachment(START_TIME_KEY, start);
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        Long now = DateTime.currentTimeNano();
        Scope scope = TracerManager.tracer().active();
        if (scope != null) {
            Long start = context.getAttachment(START_TIME_KEY);
            scope.span().log(now).event(LogFields.Event, String.format("[%.3fms] %s:%s", (now - start) / 1000000f, context.getOriginClass().getName(), context.getMethod().getName()));
        }
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {

    }
}
