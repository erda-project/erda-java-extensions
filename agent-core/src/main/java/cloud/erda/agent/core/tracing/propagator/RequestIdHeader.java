package cloud.erda.agent.core.tracing.propagator;

import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.utils.UUIDGenerator;
import cloud.erda.agent.core.tracing.TracerContext;

/**
 * @author: liuhaoyang
 * @create: 2019-01-07 14:59
 **/
public class RequestIdHeader extends Header {

    private static final String Request_Id = "terminus-request-id";

    public RequestIdHeader(Header next) {
        super(next);
    }

    @Override
    public void inject(SpanContext context, Carrier carrier) {
        String requestId = context.getTracerContext().requestId();
        carrier.put(Request_Id, requestId);
    }

    @Override
    public void extract(SpanContext.Builder builder, Carrier carrier) {
        String requestId = builder.getTracerContext().requestId();
        if (requestId == null) {
            requestId = carrier.get(Request_Id);
        }
        if (requestId == null) {
            requestId = UUIDGenerator.New();
        }
        builder.getTracerContext().put(TracerContext.REQUEST_ID, requestId);
    }
}
