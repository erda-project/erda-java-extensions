package io.terminus.spot.plugin.log4j2.pattern.converter;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

import java.util.Map;

import static io.terminus.spot.plugin.log.pattern.PatternStrings.KV_SEPARATOR;
import static io.terminus.spot.plugin.log.pattern.PatternStrings.TAG_SEPARATOR;

public class ContextPatternConverter extends LogEventPatternConverter {

    /**
     * Singleton.
     */
    private static final ContextPatternConverter INSTANCE = new ContextPatternConverter();

    /**
     * Private constructor.
     */
    private ContextPatternConverter() {
        super("TerminusContext", "terminusContext");
    }

    /**
     * Obtains an instance of ThreadPatternConverter.
     *
     * @param options options, currently ignored, may be null.
     * @return instance of ThreadPatternConverter.
     */
    public static ContextPatternConverter newInstance(final String[] options) {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final StringBuilder toAppendTo) {
        if (event.getContextData() == null || event.getContextData().isEmpty()) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : event.getContextData().toMap().entrySet()) {
            builder.append(TAG_SEPARATOR);
            builder.append(entry.getKey());
            builder.append(KV_SEPARATOR);
            builder.append(entry.getValue());
        }
        toAppendTo.append(builder);
    }
}
