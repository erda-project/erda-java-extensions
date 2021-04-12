package cloud.erda.agent.core.tracing.span;

import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.utils.DateTimeUtils;
import cloud.erda.agent.core.tracing.Tracer;

import java.util.Map;

/**
 * @author: liuhaoyang
 * @create: 2019-01-07 19:20
 **/
public class SpanImpl implements Span {

    private SpanContext spanContext;
    private Long startTime;
    private Long endTime;
    private String operationName;
    private Map<String, String> tags;
    private Tracer tracer;

    public SpanImpl(String operationName, Map<String, String> tags, SpanContext spanContext, Tracer tracer) {
        this.operationName = operationName;
        this.spanContext = spanContext;
        this.tags = tags;
        this.startTime = DateTimeUtils.currentTimeNano();
        this.tracer = tracer;
    }

    @Override
    public SpanContext getContext() {
        return spanContext;
    }

    @Override
    public String getOperationName() {
        return operationName;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public long getEndTime() {
        if (endTime == null) {
            return DateTimeUtils.currentTimeNano();
        }
        return endTime;
    }

    @Override
    public Map<String, String> getTags() {
        return tags;
    }

    @Override
    public void tag(String key, String value) {
        tags.put(key, value);
    }

    @Override
    public void finish() {
        if (endTime == null) {
            endTime = DateTimeUtils.currentTimeNano();
            tracer.dispatch(this);
        }
    }
}
