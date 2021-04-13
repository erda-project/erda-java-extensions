package cloud.erda.agent.core.tracing.propagator;

import cloud.erda.agent.core.tracing.SpanContext;

/**
 * @author: liuhaoyang
 * @create: 2019-01-07 13:55
 **/
public abstract class Header {

    private Header next;

    protected Header(Header next) {
        this.next = next;
    }

    public Header getNext() {
        return next;
    }

    public boolean hasNext() {
        return next != null;
    }

    public abstract void inject(SpanContext context, Carrier carrier);

    public abstract void extract(SpanContext.Builder builder, Carrier carrier);
}
