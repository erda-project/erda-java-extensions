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

package cloud.erda.agent.plugin.spring.concurrent.define;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import cloud.erda.agent.plugin.spring.concurrent.match.SuccessCallbackMatch;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * {@link SuccessCallbackInstrumentation} enhance the <code>onSuccess</code> method that class inherited
 * <code>org.springframework.util.concurrent.SuccessCallback</code> by <code>SuccessCallbackInterceptor</code>.
 *
 * @author zhangxin
 */
public class SuccessCallbackInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {

    public static final String SUCCESS_METHOD_NAME = "onSuccess";

    public static final String SUCCESS_CALLBACK_INTERCEPTOR
            = "cloud.erda.agent.plugin.spring.concurrent.SuccessCallbackInterceptor";

    @Override
    protected ClassMatch enhanceClass() {
        return SuccessCallbackMatch.successCallbackMatch();
    }

    @Override
    protected ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return null;
    }

    @Override
    protected InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[]{
                new InstanceMethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named(SUCCESS_METHOD_NAME);
                    }

                    @Override
                    public String getMethodsInterceptor() {
                        return SUCCESS_CALLBACK_INTERCEPTOR;
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return false;
                    }
                }
        };
    }

    @Override
    protected boolean implementDynamicField() {
        return true;
    }
}
