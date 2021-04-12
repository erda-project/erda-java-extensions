package cloud.erda.agent.core.tracing.span;

import cloud.erda.agent.core.tracing.SpanContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: liuhaoyang
 * @create: 2019-01-07 21:30
 **/
public class SpanNoop implements Span {

    private static Map<String, String> noopTags = new HashMap<String, String>(0);

    private SpanContext spanContext;

    public SpanNoop(SpanContext spanContext) {
        this.spanContext = spanContext;
    }

    @Override
    public SpanContext getContext() {
        return spanContext;
    }

    @Override
    public String getOperationName() {
        return null;
    }

    @Override
    public long getStartTime() {
        return 0;
    }

    @Override
    public long getEndTime() {
        return 0;
    }

    @Override
    public Map<String, String> getTags() {
        return noopTags;
    }

    @Override
    public void tag(String key, String value) {
    }

    @Override
    public void finish() {
    }
}
