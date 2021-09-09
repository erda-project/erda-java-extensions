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

package cloud.erda.agent.plugin.logback.pattern;

import cloud.erda.agent.core.config.loader.ConfigAccessor;
import cloud.erda.agent.plugin.log.config.LogConfig;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import cloud.erda.agent.plugin.log.pattern.PatternStrings;

public class PatternInterceptor implements InstanceMethodsAroundInterceptor {

    private final static ConfigAccessor logConfigAccessor = new ConfigAccessor(LogConfig.class.getClassLoader());

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        LogConfig logConfig = logConfigAccessor.getConfig(LogConfig.class);
        if(logConfig.getForceFormat()){
            Object[] allArguments = context.getArguments();
            allArguments[0] = PatternStrings.PATTERN;
        }
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {

    }
}
