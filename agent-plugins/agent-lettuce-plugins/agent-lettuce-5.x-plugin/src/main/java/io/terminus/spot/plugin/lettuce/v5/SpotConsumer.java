package io.terminus.spot.plugin.lettuce.v5;

import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.TracerSnapshot;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.tracing.span.SpanBuilder;
import cloud.erda.agent.core.utils.TracerUtils;

import java.util.function.Consumer;

public class SpotConsumer<T> implements Consumer<T> {

    private TracerSnapshot snapshot;
    private Consumer<T> consumer;
    private String operationName;

    SpotConsumer(Consumer<T> consumer, TracerSnapshot snapshot, String operationName) {
        this.consumer = consumer;
        this.operationName = operationName;
        this.snapshot = snapshot;
    }

    @Override
    public void accept(T t) {
        Tracer tracer = TracerManager.tracer();
        tracer.context().attach(snapshot.getTracerContext());
        SpanContext spanContext = snapshot.getSpan() != null ? snapshot.getSpan().getContext() : null;
        SpanBuilder spanBuilder = tracer.buildSpan(operationName + "/accept");
        Span span = spanBuilder.childOf(spanContext).startActive().span();
        span.tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_REDIS);
        span.tag(Constants.Tags.DB_TYPE, Constants.Tags.COMPONENT_REDIS);
        span.tag(Constants.Tags.SPAN_LAYER, Constants.Tags.SPAN_LAYER_CACHE);
        try {
            consumer.accept(t);
        } catch (Throwable th) {
            TracerUtils.handleException(th);
        } finally {
            TracerManager.tracer().active().close();
        }
    }
}
