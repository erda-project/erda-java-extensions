package io.terminus.spot.plugin.logback.pattern;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import io.terminus.spot.plugin.log.pattern.PatternStrings;

import java.util.Map;

/**
 * @author : liuhaoyang
 **/
public class TagsPatternConverter extends ClassicConverter {

    private static final RequestIdPatternConverter requestIdConverter = new RequestIdPatternConverter();
    private static final SpanIdPatternConverter spanIdConverter = new SpanIdPatternConverter();
    private static final ServicePatternConverter serviceConverter = new ServicePatternConverter();

    @Override public String convert(ILoggingEvent event) {
        StringBuilder builder = new StringBuilder();
        builder.append(serviceConverter.convert(event)).append(PatternStrings.TAG_SEPARATOR);
        builder.append(requestIdConverter.convert(event)).append(PatternStrings.TAG_SEPARATOR);
        builder.append(spanIdConverter.convert(event));
        Map<String, String> mdc = event.getMDCPropertyMap();
        if (mdc.isEmpty()) {
            return builder.toString();
        }
        for (Map.Entry<String, String> entry : mdc.entrySet()) {
            builder.append(PatternStrings.TAG_SEPARATOR);
            builder.append(entry.getKey());
            builder.append(PatternStrings.KV_SEPARATOR);
            builder.append(entry.getValue());
        }
        return builder.toString();
    }
}
