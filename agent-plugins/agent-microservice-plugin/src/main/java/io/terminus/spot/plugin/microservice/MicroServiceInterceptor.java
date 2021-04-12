package io.terminus.spot.plugin.microservice;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import cloud.erda.agent.core.utils.AddonTypeManager;

/**
 * @author randomnil
 */
public class MicroServiceInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        String method = context.getMethod().getName();

        if ("publish".equals(method) || "consumer".equals(method)) {
            AddonTypeManager.INSTANCE.addRegisterCenter();
        } else if ("configCenterClient".equals(method) || "configClient".equals(method)) {
            AddonTypeManager.INSTANCE.addConfigCenter();
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
