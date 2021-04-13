package io.terminus.spot.plugin.logback.pattern;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import cloud.erda.agent.core.tracing.TracerManager;

public class RequestIdPatternConverter extends ClassicConverter {

    static final RequestIdPatternConverter INSTANCE = new RequestIdPatternConverter();

    @Override
    public String convert(ILoggingEvent event) {
        String requestId = TracerManager.tracer().context().requestId();
        return requestId == null ? "" : requestId;
    }
}
