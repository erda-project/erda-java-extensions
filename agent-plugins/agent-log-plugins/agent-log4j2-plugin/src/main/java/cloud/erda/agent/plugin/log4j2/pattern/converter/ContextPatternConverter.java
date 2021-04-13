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

import java.util.Map;

import static cloud.erda.agent.plugin.log.pattern.PatternStrings.KV_SEPARATOR;
import static cloud.erda.agent.plugin.log.pattern.PatternStrings.TAG_SEPARATOR;

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
