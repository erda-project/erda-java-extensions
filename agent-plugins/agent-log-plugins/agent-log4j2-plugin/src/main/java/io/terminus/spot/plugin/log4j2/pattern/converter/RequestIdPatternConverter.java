package io.terminus.spot.plugin.log4j2.pattern.converter;

import cloud.erda.agent.core.tracing.TracerContext;
import cloud.erda.agent.core.tracing.TracerManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

public class RequestIdPatternConverter extends LogEventPatternConverter {
    /**
     * Singleton.
     */
    private static final RequestIdPatternConverter INSTANCE = new RequestIdPatternConverter();

    /**
     * Private constructor.
     */
    private RequestIdPatternConverter() {
        super("TerminusRequestId", "terminusRequestId");
    }

    /**
     * Obtains an instance of ThreadPatternConverter.
     *
     * @param options options, currently ignored, may be null.
     * @return instance of ThreadPatternConverter.
     */
    public static RequestIdPatternConverter newInstance(final String[] options) {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final StringBuilder toAppendTo) {
        TracerContext context = TracerManager.tracer().context();
        if (context == null) {
            return;
        }

        toAppendTo.append(context.requestId() != null ? context.requestId() : "");
    }
}