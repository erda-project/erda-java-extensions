package io.terminus.spot.plugin.logback.appender;

import ch.qos.logback.classic.LoggerContext;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

/**
 * @author: liuhaoyang
 * @create: 2019-11-28 17:33
 **/
public class ContextInitializerInterceptor implements InstanceMethodsAroundInterceptor {

    private static final ILog log = LogManager.getLogger(ContextInitializerInterceptor.class);

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        LoggerContext lc = (LoggerContext) context.getInstance().getDynamicField();
        log.info("Replace logback config after invoke {}.{}", context.getMethod().getDeclaringClass().getName(), context.getMethod().getName());
        new AppenderProcessor().replace(lc);
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
    }
}
