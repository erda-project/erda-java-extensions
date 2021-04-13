package cloud.erda.agent.core.tracing.span;

import cloud.erda.agent.core.tracing.SpanContext;

import java.util.Map;

/**
 * @author: liuhaoyang
 * @create: 2019-01-04 16:53
 **/
public interface Span {

    SpanContext getContext();

    String getOperationName();

    long getStartTime();

    long getEndTime();

    Map<String, String> getTags();

    void tag(String key, String value);

    void finish();
}
