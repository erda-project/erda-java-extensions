package cloud.erda.agent.core.tracing.propagator;

import cloud.erda.agent.core.tracing.SpanContext;

/**
 * @author: liuhaoyang
 * @create: 2019-01-07 17:18
 **/
public class NoopHeader extends Header {


    public NoopHeader() {
        super(null);
    }

    @Override
    public void inject(SpanContext context, Carrier carrier) {
    }

    @Override
    public void extract(SpanContext.Builder builder, Carrier carrier) {
    }
}
