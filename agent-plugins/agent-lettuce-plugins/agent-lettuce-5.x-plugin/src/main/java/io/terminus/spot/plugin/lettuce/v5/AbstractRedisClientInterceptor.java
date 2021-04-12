package io.terminus.spot.plugin.lettuce.v5;


import io.lettuce.core.AbstractRedisClient;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

public class AbstractRedisClientInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        EnhancedInstance clientOptions = (EnhancedInstance) context.getArguments()[0];
        if (clientOptions == null) {
            return;
        }
        AbstractRedisClient client = (AbstractRedisClient) context.getInstance();
        if (client.getOptions() == null || ((EnhancedInstance) client.getOptions()).getDynamicField() == null) {
            return;
        }
        clientOptions.setDynamicField(((EnhancedInstance) client.getOptions()).getDynamicField());
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {

    }
}
