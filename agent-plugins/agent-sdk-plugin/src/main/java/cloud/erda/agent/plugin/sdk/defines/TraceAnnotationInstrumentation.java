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

package cloud.erda.agent.plugin.sdk.defines;

import cloud.erda.msp.monitor.tracing.Trace;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.StaticMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import org.apache.skywalking.apm.agent.core.plugin.match.MethodAnnotationMatch;

import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;

/**
 * @author liuhaoyang
 * @date 2021/5/26 17:51
 */
public class TraceAnnotationInstrumentation extends ClassEnhancePluginDefine {

    private static final String TRACE_ANNOTATION = "cloud.erda.msp.monitor.tracing.Trace";
    private static final String TRACE_ANNOTATION_METHOD_INTERCEPTOR = "cloud.erda.agent.plugin.sdk.interceptors.TraceAnnotationInterceptor";

    @Override
    protected ClassMatch enhanceClass() {
        return MethodAnnotationMatch.byMethodAnnotationMatch(new String[]{TRACE_ANNOTATION});
    }

    @Override
    protected ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[0];
    }

    @Override
    protected InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[] {
                new InstanceMethodsInterceptPoint(){

                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return isAnnotatedWith(Trace.class);
                    }

                    @Override
                    public String getMethodsInterceptor() {
                        return TRACE_ANNOTATION_METHOD_INTERCEPTOR;
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return false;
                    }
                }
        };
    }

    @Override
    protected StaticMethodsInterceptPoint[] getStaticMethodsInterceptPoints() {
        return new StaticMethodsInterceptPoint[]{
                new StaticMethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {
                        return isAnnotatedWith(Trace.class);
                    }

                    @Override
                    public String getMethodsInterceptor() {
                        return TRACE_ANNOTATION_METHOD_INTERCEPTOR;
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return false;
                    }
                }
        };
    }
}
