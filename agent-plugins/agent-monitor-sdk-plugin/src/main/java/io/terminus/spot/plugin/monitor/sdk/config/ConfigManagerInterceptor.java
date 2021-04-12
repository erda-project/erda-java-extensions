package io.terminus.spot.plugin.monitor.sdk.config;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.StaticMethodsAroundInterceptor;

/**
 * @author liuhaoyang 2020/3/18 00:01
 */
public class ConfigManagerInterceptor implements StaticMethodsAroundInterceptor {

    private final MonitorConfig config = new MonitorConfig();

    @Override public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) {
        result.defineReturnValue(config);
    }

    @Override public Object afterMethod(IMethodInterceptContext context, Object ret) {
        return ret;
    }

    @Override public void handleMethodException(IMethodInterceptContext context, Throwable t) throws Throwable {

    }
}
