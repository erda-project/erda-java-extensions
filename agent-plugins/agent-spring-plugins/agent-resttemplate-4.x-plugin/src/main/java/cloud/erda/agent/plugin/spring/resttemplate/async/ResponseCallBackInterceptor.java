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

package cloud.erda.agent.plugin.spring.resttemplate.async;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.DynamicFieldEnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import cloud.erda.agent.core.tracing.Scope;
import cloud.erda.agent.core.tracing.TracerManager;

public class ResponseCallBackInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        Object[] allArguments = context.getArguments();
        if (allArguments == null || allArguments.length < 1
                || !(allArguments[0] instanceof EnhancedInstance)) {
            return;
        }
        DynamicFieldEnhancedInstance successCallBack = (DynamicFieldEnhancedInstance) allArguments[0];
        successCallBack.setDynamicField( ((DynamicFieldEnhancedInstance)context.getInstance()).getDynamicField());

        if (allArguments.length < 2 || !(allArguments[1] instanceof EnhancedInstance)) {
            return;
        }
        DynamicFieldEnhancedInstance failedCallBack = (DynamicFieldEnhancedInstance) allArguments[1];
        failedCallBack.setDynamicField( ((DynamicFieldEnhancedInstance)context.getInstance()).getDynamicField());
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        Scope scope = TracerManager.currentTracer().active();
        if (scope != null) {
            scope.close(false);
        }
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
    }
}
