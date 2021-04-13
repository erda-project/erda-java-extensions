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

package cloud.erda.agent.plugin.log4j2.pattern;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import cloud.erda.agent.plugin.log4j2.pattern.converter.RequestIdPatternConverter;
import cloud.erda.agent.plugin.log4j2.pattern.converter.ServicePatternConverter;
import cloud.erda.agent.plugin.log4j2.pattern.converter.SpanIdPatternConverter;
import cloud.erda.agent.plugin.log4j2.pattern.converter.TagsPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;

import java.util.LinkedHashMap;
import java.util.Map;

import static cloud.erda.agent.plugin.log.pattern.PatternStrings.*;

/**
 * @author randomnil
 */
public class PatternParserInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        Object[] args = context.getArguments();
        if (args == null || args.length < 6) {
            return;
        }
        if (!(args[5] instanceof Map)) {
            return;
        }

        Map<String, Class<PatternConverter>> rules = (Map) args[5];
        Map<String, Class<PatternConverter>> allRules = new LinkedHashMap<>(rules);
        if (!allRules.containsKey(REQUEST_ID)) {
            PatternConverter converter = RequestIdPatternConverter.newInstance(null);
            allRules.put(REQUEST_ID, (Class<PatternConverter>) converter.getClass());
        }
        if (!allRules.containsKey(SERVICE)) {
            PatternConverter converter = ServicePatternConverter.newInstance(null);
            allRules.put(SERVICE, (Class<PatternConverter>) converter.getClass());
        }
        if (!allRules.containsKey(SPAN_ID)) {
            PatternConverter converter = SpanIdPatternConverter.newInstance(null);
            allRules.put(SPAN_ID, (Class<PatternConverter>) converter.getClass());
        }
        if (!allRules.containsKey(TAGS)) {
            PatternConverter converter = TagsPatternConverter.newInstance(null);
            allRules.put(TAGS, (Class<PatternConverter>) converter.getClass());
        }
        args[5] = allRules;
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
    }
}
