package cloud.erda.agent.core.tracing.propagator;

import cloud.erda.agent.core.tracing.SpanContext;

/**
 * @author: liuhaoyang
 * @create: 2019-01-07 16:01
 **/
public class SpanIdHeader extends Header {

    private static final String Request_Span_Id = "terminus-request-spanid";

    public SpanIdHeader(Header next) {
        super(next);
    }

    @Override
    public void inject(SpanContext context, Carrier carrier) {
        String spanId = context.getSpanId();
        carrier.put(Request_Span_Id, spanId);
    }

    @Override
    public void extract(SpanContext.Builder builder, Carrier carrier) {
        String parentSpanId = carrier.get(Request_Span_Id);
        builder.setSpanId(parentSpanId);
    }
}
