package cloud.erda.agent.core.tracing.propagator;

import cloud.erda.agent.core.tracing.SpanContext;

/**
 * @author: liuhaoyang
 * @create: 2019-01-07 13:32
 **/
public class Propagator {

    private static final Header HEADER = HeaderFactory.createHeader();

    public void inject(SpanContext spanContext, Carrier carrier) {
        Header header = HEADER;
        while (header.hasNext()) {
            header.inject(spanContext, carrier);
            header = header.getNext();
        }
    }

    public SpanContext extract(Carrier carrier) {
        SpanContext.Builder builder = new SpanContext.Builder();
        Header header = HEADER;
        while (header.hasNext()) {
            header.extract(builder, carrier);
            header = header.getNext();
        }
        return builder.build(false);
    }
}
