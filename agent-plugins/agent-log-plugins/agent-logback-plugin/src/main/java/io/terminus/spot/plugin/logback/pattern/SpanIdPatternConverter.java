package io.terminus.spot.plugin.logback.pattern;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import cloud.erda.agent.core.tracing.Scope;
import cloud.erda.agent.core.tracing.TracerManager;

public class SpanIdPatternConverter extends ClassicConverter {
    @Override
    public String convert(ILoggingEvent event) {
        Scope scope = TracerManager.tracer().active();
        if (scope != null) {
            return scope.span().getContext().getSpanId();
        }
        return "";
    }
}
