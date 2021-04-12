package io.terminus.spot.plugin.logback.appender;

import ch.qos.logback.classic.LoggerContext;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceConstructorInterceptor;

public class ContextInitializerConstructorInterceptor implements InstanceConstructorInterceptor {

    private static final ILog log = LogManager.getLogger(ContextInitializerConstructorInterceptor.class);

    @Override
    public void onConstruct(EnhancedInstance objInst, Object[] allArguments) {
        LoggerContext lc = (LoggerContext) allArguments[0];
        objInst.setDynamicField(lc);
    }
}
