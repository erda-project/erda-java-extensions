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

import cloud.erda.agent.plugin.sdk.interceptPoint.InterceptPoint;
import cloud.erda.agent.plugin.sdk.interceptors.UserDefineMethodPointsInterceptor;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.StaticMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassStaticMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.apache.skywalking.apm.agent.core.plugin.match.NameMatch.byName;

/**
 * @author liuhaoyang
 * @date 2021/5/10 15:37
 */
public class StaticMethodInterceptInstrumentation extends ClassStaticMethodsEnhancePluginDefine {

    private InterceptPoint interceptPoint;

    public StaticMethodInterceptInstrumentation(InterceptPoint interceptPoint) {
        this.interceptPoint = interceptPoint;
    }

    @Override
    protected ClassMatch enhanceClass() {
        return byName(interceptPoint.getClassName());
    }

    @Override
    protected StaticMethodsInterceptPoint[] getStaticMethodsInterceptPoints() {
        String[] methodNames = interceptPoint.getMethodNames();
        StaticMethodsInterceptPoint[] staticMethodsInterceptPoints = new StaticMethodsInterceptPoint[methodNames.length];
        for (int i = 0; i < methodNames.length; i++) {
            int index = i;
            staticMethodsInterceptPoints[i] = new StaticMethodsInterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return named(methodNames[index]);
                }

                @Override
                public String getMethodsInterceptor() {
                    return UserDefineMethodPointsInterceptor.INTERCEPTOR_CLASS;
                }

                @Override
                public boolean isOverrideArgs() {
                    return true;
                }
            };
        }
        return staticMethodsInterceptPoints;
    }

    @Override
    protected boolean implementDynamicField() {
        return false;
    }
}
