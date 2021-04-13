package io.terminus.spot.plugin.logback.spring.boot;

import ch.qos.logback.classic.LoggerContext;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import io.terminus.spot.plugin.logback.appender.AppenderProcessor;
import org.slf4j.impl.StaticLoggerBinder;

public class LogbackLoggingSystemInterceptor implements InstanceMethodsAroundInterceptor {

    private static final ILog log = LogManager.getLogger(LogbackLoggingSystemInterceptor.class);

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        LoggerContext loggerContext = (LoggerContext) StaticLoggerBinder.getSingleton().getLoggerFactory();
        log.info("Replace logback config after invoke {}.{}", context.getMethod().getDeclaringClass().getName(), context.getMethod().getName());
        new AppenderProcessor().replace(loggerContext);
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
    }
}
