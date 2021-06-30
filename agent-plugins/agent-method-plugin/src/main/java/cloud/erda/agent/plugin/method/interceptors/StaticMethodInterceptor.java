package cloud.erda.agent.plugin.method.interceptors;

import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.tracing.span.SpanBuilder;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.TracerUtils;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.StaticMethodsAroundInterceptor;

/**
 * @author liuhaoyang
 * @date 2021/5/9 18:36
 */
public class StaticMethodInterceptor implements StaticMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) {
        Tracer tracer = TracerManager.tracer();
        SpanContext spanContext = tracer.active() != null ? tracer.active().span().getContext() : null;
        SpanBuilder spanBuilder = tracer.buildSpan("Call/" + context.getOriginClass().getName() + "." + context.getMethod().getName());
        Span span = spanBuilder.childOf(spanContext).startActive().span();
        span.tag(Constants.Tags.CLASS, context.getOriginClass().getName());
        span.tag(Constants.Tags.METHOD, context.getMethod().getName());
        span.tag(Constants.Tags.SPAN_LAYER, Constants.Tags.SPAN_LAYER_LOCAL);
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) {
        TracerManager.tracer().active().close();
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) throws Throwable {
        TracerUtils.handleException(t);
    }
}
