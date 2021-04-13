package cloud.erda.agent.core.tracing;

import cloud.erda.agent.core.tracing.propagator.Carrier;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.tracing.span.SpanBuilder;

/**
 * @author: liuhaoyang
 * @create: 2019-01-04 17:26
 **/
public interface Tracer {

    TracerContext context();

    Scope activate(Span span);

    Scope activate(Scope scope);

    Scope active();

    Scope attach(TracerSnapshot snapshot);

    TracerSnapshot capture();

    void dispatch(Span span);

    void inject(SpanContext spanContext, Carrier carrier);

    SpanContext extract(Carrier carrier);

    SpanBuilder buildSpan(String operationName);
}
