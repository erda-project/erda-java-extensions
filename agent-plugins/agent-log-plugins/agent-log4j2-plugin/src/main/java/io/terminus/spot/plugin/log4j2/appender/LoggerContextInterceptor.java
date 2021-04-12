package io.terminus.spot.plugin.log4j2.appender;

import cloud.erda.agent.core.config.loader.ConfigAccessor;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import io.terminus.spot.plugin.log.config.LogConfig;
import io.terminus.spot.plugin.log.error.ErrorConsts;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.util.Map;

/**
 * @author randomnil
 */
public class LoggerContextInterceptor implements InstanceMethodsAroundInterceptor {

    private final static ILog log = LogManager.getLogger(LoggerContextInterceptor.class);

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {

        if (!(ret instanceof LoggerContext)) {
            return ret;
        }
        LoggerContext lc = (LoggerContext) ret;

        Configuration config = lc.getConfiguration();
        if (config == null) {
            return ret;
        }
        LoggerConfig logger = config.getRootLogger();
        if (logger == null) {
            return ret;
        }
        LogConfig logConfig = ConfigAccessor.Default.getConfig(LogConfig.class);
        boolean hasError = false;
        for (Map.Entry<String, Appender> entry : logger.getAppenders().entrySet()) {
            Appender appender = entry.getValue();
            String key = entry.getKey();
            log.info("Find [{}] appender {}.", key, appender.getClass().getName());
            if (appender instanceof ConsoleAppender) {
                continue;
            } else if (appender instanceof ErrorInsightAppender) {
                hasError = true;
                continue;
            }
            if (logConfig.getForceStdout()) {
                appender.stop();
                logger.removeAppender(key);
                log.info("Detach [{}] appender {}.", key, appender.getClass().getName());
            }
        }
        if (!hasError) {
            Appender appender = new ErrorInsightAppender(ErrorConsts.ERROR_INSIGHT, null, null, true);
            appender.start();
            logger.addAppender(appender, Level.ERROR, null);
            log.info("Add [{}] appender {}.", ErrorConsts.ERROR_INSIGHT, appender.getClass().getName());
        }
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
    }
}
