package cloud.erda.agent.core.tracing;

import cloud.erda.agent.core.tracing.span.Span;

/**
 * @author: liuhaoyang
 * @create: 2019-01-09 15:25
 **/
public class TracerSnapshot {

    private TracerContext tracerContext;
    private Span span;

    public TracerSnapshot(TracerContext tracerContext, Span span) {
        this.tracerContext = tracerContext;
        this.span = span;
    }

    public TracerContext getTracerContext() {
        return tracerContext;
    }

    public Span getSpan() {
        return span;
    }
}
