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

import ch.qos.logback.core.pattern.parser.SimpleKeywordNode;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import cloud.erda.agent.plugin.log.pattern.PatternStrings;

public class CompilerInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        SimpleKeywordNode node = (SimpleKeywordNode) context.getArguments()[0];
        if (PatternStrings.REQUEST_ID.equals(node.getValue())) {
            result.defineReturnValue(new RequestIdPatternConverter());
        }
        if (PatternStrings.SERVICE.equals(node.getValue())) {
            result.defineReturnValue(new ServicePatternConverter());
        }
        if (PatternStrings.SPAN_ID.equals(node.getValue())) {
            result.defineReturnValue(new SpanIdPatternConverter());
        }
        if (PatternStrings.TAGS.equals(node.getValue())) {
            result.defineReturnValue(new TagsPatternConverter());
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
