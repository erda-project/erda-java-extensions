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

package cloud.erda.agent.plugin.rocketmq.v4.define;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import org.apache.skywalking.apm.agent.core.plugin.match.HierarchyMatch;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

public class SendCallbackInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {

    private static final String ENHANCE_CLASS = "org.apache.rocketmq.client.producer.SendCallback";

    private static final String ON_SUCCESS_ENHANCE_METHOD = "onSuccess";
    private static final String ON_EXCEPTION_METHOD = "onException";

    private static final String ON_SUCCESS_FIRST_ARG_CLASS = "org.apache.rocketmq.client.producer.SendResult";
    private static final String ON_EXCEPTION_FIRST_AGR_CLASS = "java.lang.Throwable";

    private static final String ON_SUCCESS_INTERCEPTOR = "cloud.erda.agent.plugin.rocketmq.v4.OnSuccessInterceptor";
    private static final String ON_EXCEPTION_INTERCEPTOR = "cloud.erda.agent.plugin.rocketmq.v4.OnExceptionInterceptor";

    @Override
    protected ClassMatch enhanceClass() {
        return HierarchyMatch.byHierarchyMatch(new String[]{ENHANCE_CLASS});
    }

    @Override
    protected ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[0];
    }

    @Override
    protected InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[]{
                new InstanceMethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named(ON_SUCCESS_ENHANCE_METHOD)
                                .and(takesArgument(0, named(ON_SUCCESS_FIRST_ARG_CLASS)));
                    }

                    @Override
                    public String getMethodsInterceptor() {
                        return ON_SUCCESS_INTERCEPTOR;
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return false;
                    }
                },
                new InstanceMethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return named(ON_EXCEPTION_METHOD).and(takesArgument(0, named(ON_EXCEPTION_FIRST_AGR_CLASS)));
                    }

                    @Override
                    public String getMethodsInterceptor() {
                        return ON_EXCEPTION_INTERCEPTOR;
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
