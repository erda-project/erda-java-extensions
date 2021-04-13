package cloud.erda.agent.core.tracing;

import cloud.erda.agent.core.tracing.propagator.Propagator;
import org.apache.skywalking.apm.agent.core.boot.ServiceManager;
import cloud.erda.agent.core.tracing.propagator.Carrier;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.tracing.span.SpanBuilder;
import cloud.erda.agent.core.tracing.span.SpanSerializer;
import cloud.erda.agent.core.reporter.TelegrafReporter;

/**
 * @author: liuhaoyang
 * @create: 2019-01-04 17:36
 **/
public class ScopeTracer implements Tracer {

    private static final Propagator propagator = new Propagator();
    private Scope activeScope;
    private TracerContext context = new TracerContext();
    private SpanSerializer spanSerializer = new SpanSerializer();
    private TelegrafReporter transporter = ServiceManager.INSTANCE.findService(TelegrafReporter.class);
    private Sampler sampler = ServiceManager.INSTANCE.findService(Sampler.class);

    @Override
    public TracerContext context() {
        return context;
    }

    @Override
    public Scope active() {
        return activeScope;
    }

    @Override
    public Scope attach(TracerSnapshot snapshot) {
        if (snapshot == null) {
            return active();
        }
        context.attach(snapshot.getTracerContext());
        if (snapshot.getSpan() == null) {
            return active();
        }
        return activate(snapshot.getSpan());
    }

    @Override
    public TracerSnapshot capture() {
        Span span = active() != null ? active().span() : null;
        return new TracerSnapshot(context.capture(), span);
    }

    @Override
    public void dispatch(Span span) {
        transporter.send(spanSerializer.serialize(span));
    }

    @Override
    public void inject(SpanContext spanContext, Carrier carrier) {
        propagator.inject(spanContext, carrier);
    }

    @Override
    public SpanContext extract(Carrier carrier) {
        return propagator.extract(carrier);
    }

    @Override
    public Scope activate(Span span) {
        return new Scope(this, span);
    }

    @Override
    public Scope activate(Scope scope) {
        return this.activeScope = scope;
    }

    @Override
    public SpanBuilder buildSpan(String operationName) {
        return new SpanBuilder(operationName, this, sampler);
    }
}
