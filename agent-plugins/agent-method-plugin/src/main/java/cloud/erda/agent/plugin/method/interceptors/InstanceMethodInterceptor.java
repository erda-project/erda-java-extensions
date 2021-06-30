package cloud.erda.agent.plugin.method.interceptors;


import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.tracing.span.SpanBuilder;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.TracerUtils;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

/**
 * @author liuhaoyang
 * @date 2021/5/9 18:35
 */
public class InstanceMethodInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        Tracer tracer = TracerManager.tracer();
        SpanContext spanContext = tracer.active() != null ? tracer.active().span().getContext() : null;
        SpanBuilder spanBuilder = tracer.buildSpan("Call/" + context.getOriginClass().getName() + "." + context.getMethod().getName());
        Span span = spanBuilder.childOf(spanContext).startActive().span();
        span.tag(Constants.Tags.CLASS, context.getOriginClass().getName());
        span.tag(Constants.Tags.METHOD, context.getMethod().getName());
        span.tag(Constants.Tags.SPAN_LAYER, Constants.Tags.SPAN_LAYER_LOCAL);
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        TracerManager.tracer().active().close();
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        TracerUtils.handleException(t);
    }
}
