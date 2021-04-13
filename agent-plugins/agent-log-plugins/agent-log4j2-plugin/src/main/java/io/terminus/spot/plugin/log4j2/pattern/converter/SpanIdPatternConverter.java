package io.terminus.spot.plugin.log4j2.pattern.converter;

import cloud.erda.agent.core.tracing.Scope;
import cloud.erda.agent.core.tracing.TracerManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

public class SpanIdPatternConverter extends LogEventPatternConverter {
    /**
     * Singleton.
     */
    private static final SpanIdPatternConverter INSTANCE = new SpanIdPatternConverter();

    /**
     * Private constructor.
     */
    private SpanIdPatternConverter() {
        super("TerminusSpanId", "terminusSpanId");
    }

    /**
     * Obtains an instance of ThreadPatternConverter.
     *
     * @param options options, currently ignored, may be null.
     * @return instance of ThreadPatternConverter.
     */
    public static SpanIdPatternConverter newInstance(final String[] options) {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final StringBuilder toAppendTo) {
        Scope scope = TracerManager.tracer().active();
        if (scope == null || scope.span() == null || scope.span().getContext() == null) {
            return;
        }

        toAppendTo.append(scope.span().getContext().getSpanId() != null ? scope.span().getContext().getSpanId() : "");
    }
}