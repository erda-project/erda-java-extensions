package io.terminus.spot.plugin.servlet.jetty;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import io.terminus.spot.plugin.servlet.BaseServletInvokeInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author randomnil
 */
public class ServletDoHandlerInterceptor extends BaseServletInvokeInterceptor {

    @Override
    protected HttpServletRequest getRequest(IMethodInterceptContext context) {
        return (HttpServletRequest) context.getArguments()[2];
    }

    @Override
    protected HttpServletResponse getResponse(IMethodInterceptContext context) {
        return (HttpServletResponse) context.getArguments()[3];
    }
}
