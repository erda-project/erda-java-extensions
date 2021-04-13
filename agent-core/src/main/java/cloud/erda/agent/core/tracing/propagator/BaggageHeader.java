package cloud.erda.agent.core.tracing.propagator;

import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.TracerContext;

import java.util.Map;

/**
 * @author: liuhaoyang
 * @create: 2019-01-07 17:38
 **/
public class BaggageHeader extends Header {

    private static final String Request_Bg = "terminus-request-bg-";

    public BaggageHeader(Header next) {
        super(next);
    }

    @Override
    public void inject(SpanContext context, Carrier carrier) {
        for (Map.Entry<String, String> entry : context.getTracerContext()) {
            if (TracerContext.REQUEST_ID.equals(entry.getKey()) || TracerContext.SAMPLED.equals(entry.getKey())) {
                continue;
            }
            carrier.put(Request_Bg + entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void extract(SpanContext.Builder builder, Carrier carrier) {
        for (Map.Entry<String, String> entry : carrier) {
            if (entry.getKey().startsWith(Request_Bg)) {
                String key = entry.getKey().substring((Request_Bg.length()));
                builder.getTracerContext().put(key, entry.getValue());
            }
        }
    }
}
