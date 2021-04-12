package cloud.erda.agent.core.tracing.propagator;

import cloud.erda.agent.core.tracing.SpanContext;

/**
 * @author: liuhaoyang
 * @create: 2019-01-07 18:16
 **/
public class RequestContextHeader extends Header {

    private static final String Request_Context = "terminus-request-context";

    public RequestContextHeader(Header next) {
        super(next);
    }

    @Override
    public void inject(SpanContext context, Carrier carrier) {
        //todo
    }

    @Override
    public void extract(SpanContext.Builder builder, Carrier carrier) {
        //todo
    }
}
