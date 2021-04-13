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

import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import cloud.erda.agent.plugin.log.pattern.PatternStrings;

import java.util.Map;

public class PatternLayoutInterceptor implements InstanceMethodsAroundInterceptor {

    private static final ILog log = LogManager.getLogger(PatternLayoutInterceptor.class);

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        if (ret != null) {
            Map<String, String> defaultConverterMap = (Map<String, String>) ret;
            if (!defaultConverterMap.containsKey(PatternStrings.REQUEST_ID)) {
                defaultConverterMap.put(PatternStrings.REQUEST_ID, RequestIdPatternConverter.class.getName());
            }
            if (!defaultConverterMap.containsKey(PatternStrings.SERVICE)) {
                defaultConverterMap.put(PatternStrings.SERVICE, ServicePatternConverter.class.getName());
            }
            if (!defaultConverterMap.containsKey(PatternStrings.SPAN_ID)) {
                defaultConverterMap.put(PatternStrings.SPAN_ID, SpanIdPatternConverter.class.getName());
            }
            if (!defaultConverterMap.containsKey(PatternStrings.TAGS)) {
                defaultConverterMap.put(PatternStrings.TAGS, TagsPatternConverter.class.getName());
            }
        }
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
    }
}
