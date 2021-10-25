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

import cloud.erda.agent.plugin.sdk.interceptors.UserDefineMethodPointsInterceptor;
import cloud.erda.agent.plugin.sdk.match.PackageMatch;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.StaticMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * @author liuhaoyang
 * @date 2021/10/20 14:06
 */
public class PackageInterceptInstrumentation extends ClassEnhancePluginDefine {

    private String packages;

    public PackageInterceptInstrumentation(String packages) {
        this.packages = packages;
    }

    @Override
    protected ClassMatch enhanceClass() {
        return PackageMatch.byPackages(packages);
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
                        return ElementMatchers.not(named("getDynamicField").or(ElementMatchers.isDefaultMethod()).or(isAbstract()).or(nameContainsIgnoreCase("BySpringCGLIB$")));
                    }

                    @Override
                    public String getMethodsInterceptor() {
                        return UserDefineMethodPointsInterceptor.INTERCEPTOR_CLASS;
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return true;
                    }
                }
        };
    }

    @Override
    protected boolean implementDynamicField() {
        return false;
    }

    @Override
    protected StaticMethodsInterceptPoint[] getStaticMethodsInterceptPoints() {
        return new StaticMethodsInterceptPoint[]{
                new StaticMethodsInterceptPoint() {
                    @Override
                    public ElementMatcher<MethodDescription> getMethodsMatcher() {

                        return not(nameContainsIgnoreCase("BySpringCGLIB$"));
                    }

                    @Override
                    public String getMethodsInterceptor() {
                        return UserDefineMethodPointsInterceptor.INTERCEPTOR_CLASS;
                    }

                    @Override
                    public boolean isOverrideArgs() {
                        return true;
                    }
                }
        };
    }
}
