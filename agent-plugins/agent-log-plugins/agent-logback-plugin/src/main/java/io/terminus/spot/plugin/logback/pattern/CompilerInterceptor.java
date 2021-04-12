package io.terminus.spot.plugin.logback.pattern;

import ch.qos.logback.core.pattern.parser.SimpleKeywordNode;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import io.terminus.spot.plugin.log.pattern.PatternStrings;

public class CompilerInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        SimpleKeywordNode node = (SimpleKeywordNode)context.getArguments()[0];
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
