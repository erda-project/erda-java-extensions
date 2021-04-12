package cloud.erda.agent.core.tracing.propagator;

import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.TracerContext;

/**
 * @author: liuhaoyang
 * @create: 2019-01-07 16:18
 **/
public class SampledHeader extends Header {

    private static final String Request_Sampled = "terminus-request-sampled";

    public SampledHeader(Header next) {
        super(next);
    }

    @Override
    public void inject(SpanContext context, Carrier carrier) {
        String sampled = String.valueOf(context.getTracerContext().sampled());
        carrier.put(Request_Sampled, sampled);
    }

    @Override
    public void extract(SpanContext.Builder builder, Carrier carrier) {
        Boolean hasSampled = builder.getTracerContext().sampled();
        if (hasSampled == null) {
            String sampled = carrier.get(Request_Sampled);
            builder.getTracerContext().put(TracerContext.SAMPLED, sampled);
        }
    }
}
