package io.terminus.spot.plugin.log4j2.pattern;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import io.terminus.spot.plugin.log4j2.pattern.converter.RequestIdPatternConverter;
import io.terminus.spot.plugin.log4j2.pattern.converter.ServicePatternConverter;
import io.terminus.spot.plugin.log4j2.pattern.converter.SpanIdPatternConverter;
import io.terminus.spot.plugin.log4j2.pattern.converter.TagsPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.terminus.spot.plugin.log.pattern.PatternStrings.*;

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

        Map<String, Class<PatternConverter>> rules = (Map)args[5];
        Map<String, Class<PatternConverter>> allRules = new LinkedHashMap<>(rules);
        if (!allRules.containsKey(REQUEST_ID)) {
            PatternConverter converter = RequestIdPatternConverter.newInstance(null);
            allRules.put(REQUEST_ID, (Class<PatternConverter>)converter.getClass());
        }
        if (!allRules.containsKey(SERVICE)) {
            PatternConverter converter = ServicePatternConverter.newInstance(null);
            allRules.put(SERVICE, (Class<PatternConverter>)converter.getClass());
        }
        if (!allRules.containsKey(SPAN_ID)) {
            PatternConverter converter = SpanIdPatternConverter.newInstance(null);
            allRules.put(SPAN_ID, (Class<PatternConverter>)converter.getClass());
        }
        if (!allRules.containsKey(TAGS)) {
            PatternConverter converter = TagsPatternConverter.newInstance(null);
            allRules.put(TAGS, (Class<PatternConverter>)converter.getClass());
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
