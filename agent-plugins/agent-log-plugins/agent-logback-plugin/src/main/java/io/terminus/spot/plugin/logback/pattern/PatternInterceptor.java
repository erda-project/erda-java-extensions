package io.terminus.spot.plugin.logback.pattern;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import io.terminus.spot.plugin.log.pattern.PatternStrings;

public class PatternInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        Object[] allArguments = context.getArguments();
//        String pattern = (String) allArguments[0];
//        if (pattern.indexOf(PatternStrings.LOG_ID_PATTERN) > -1) {
//            return;
//        }
//        if (pattern.equalsIgnoreCase("common") ||
//                pattern.equalsIgnoreCase("clf") ||
//                pattern.equalsIgnoreCase("combined")) {
//            return;
//        }
//
//        Integer i = pattern.indexOf("%n");
//        if (i > -1) {
//            pattern = pattern.replaceFirst("%n", PatternStrings.LOG_ID_PATTERN + " %n");
//        } else {
//            pattern = pattern + PatternStrings.LOG_ID_PATTERN;
//        }
        allArguments[0] = PatternStrings.PATTERN;
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {

    }
}
