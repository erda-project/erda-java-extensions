/*
 * Copyright (c) 2021 Terminus, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.erda.agent.plugin.logback.pattern;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import cloud.erda.agent.plugin.log.pattern.PatternStrings;

import java.util.Map;

/**
 * @author : liuhaoyang
 **/
public class TagsPatternConverter extends ClassicConverter {

    private static final RequestIdPatternConverter requestIdConverter = new RequestIdPatternConverter();
    private static final SpanIdPatternConverter spanIdConverter = new SpanIdPatternConverter();
    private static final ServicePatternConverter serviceConverter = new ServicePatternConverter();

    @Override
    public String convert(ILoggingEvent event) {
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
