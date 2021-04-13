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

package cloud.erda.agent.plugin.log4j2.pattern.converter;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

import static cloud.erda.agent.plugin.log.pattern.PatternStrings.TAG_SEPARATOR;

/**
 * @author : liuhaoyang
 **/
public class TagsPatternConverter extends LogEventPatternConverter {

    private static final RequestIdPatternConverter REQUEST_ID_CONVERTER = RequestIdPatternConverter.newInstance(null);
    private static final SpanIdPatternConverter SPAN_ID_CONVERTER = SpanIdPatternConverter.newInstance(null);
    private static final ServicePatternConverter SERVICE_CONVERTER = ServicePatternConverter.newInstance(null);
    private static final ContextPatternConverter CONTEXT_CONVERTER = ContextPatternConverter.newInstance(null);

    /**
     * Singleton.
     */
    private static final TagsPatternConverter INSTANCE = new TagsPatternConverter();

    /**
     * Private constructor.
     */
    private TagsPatternConverter() {
        super("TerminusTag", "terminusTag");
    }

    /**
     * Obtains an instance of ThreadPatternConverter.
     *
     * @param options options, currently ignored, may be null.
     * @return instance of ThreadPatternConverter.
     */
    public static TagsPatternConverter newInstance(final String[] options) {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final StringBuilder toAppendTo) {
        StringBuilder builder = new StringBuilder();
        SERVICE_CONVERTER.format(event, builder);
        builder.append(TAG_SEPARATOR);
        REQUEST_ID_CONVERTER.format(event, builder);
        builder.append(TAG_SEPARATOR);
        SPAN_ID_CONVERTER.format(event, builder);
        CONTEXT_CONVERTER.format(event, builder);
        toAppendTo.append(builder);
    }
}
