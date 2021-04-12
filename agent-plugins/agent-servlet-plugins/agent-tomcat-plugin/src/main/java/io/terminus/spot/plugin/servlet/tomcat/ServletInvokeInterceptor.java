package io.terminus.spot.plugin.servlet.tomcat;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import io.terminus.spot.plugin.servlet.BaseServletInvokeInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author randomnil
 */
public class ServletInvokeInterceptor extends BaseServletInvokeInterceptor {

    @Override
    protected HttpServletRequest getRequest(IMethodInterceptContext context) {
        return (HttpServletRequest)context.getArguments()[0];
    }

    @Override
    protected HttpServletResponse getResponse(IMethodInterceptContext context) {
        return (HttpServletResponse)context.getArguments()[1];
    }
}
