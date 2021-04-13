package io.terminus.spot.plugin.log4j2.pattern;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import io.terminus.spot.plugin.log.pattern.PatternStrings;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * @author randomnil
 */
public class PatternLayoutInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        EnhancedInstance instance = context.getInstance();
        if (!(instance instanceof PatternLayout.Builder)) {
            return;
        }
        PatternLayout.Builder builder = (PatternLayout.Builder)instance;
        builder.withPattern(PatternStrings.PATTERN);
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
    }
}
