package cloud.erda.agent.core.tracing;

import cloud.erda.agent.core.utils.UUIDGenerator;

/**
 * @author: liuhaoyang
 * @create: 2019-01-04 17:07
 **/
public class SpanContext {

    private String spanId;

    private String parentSpanId;

    private SpanContext(String spanId, String parentSpanId) {
        this.spanId = spanId;
        this.parentSpanId = parentSpanId;
    }

    public String getSpanId() {
        return spanId;
    }

    public String getParentSpanId() {
        return parentSpanId;
    }

    public TracerContext getTracerContext() {
        return TracerManager.tracer().context();
    }

    public static class Builder {

        private String parentSpanId;

        private String spanId;

        public TracerContext getTracerContext() {
            return TracerManager.tracer().context();
        }

        public void setParentSpanId(String parentSpanId) {
            this.parentSpanId = parentSpanId;
        }

        public void setSpanId(String spanId) {
            this.spanId = spanId;
        }

        public SpanContext build() {
            return build(true);
        }

        public SpanContext build(final boolean initSpanId) {
            String spanId = initSpanId ? (this.spanId != null ? this.spanId : UUIDGenerator.New()) : this.spanId;
            return new SpanContext(spanId, parentSpanId);
        }
    }
}
