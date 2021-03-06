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
        Scope scope = TracerManager.currentTracer().active();
        if (scope == null || scope.span() == null || scope.span().getContext() == null) {
            return;
        }

        toAppendTo.append(scope.span().getContext().getSpanId() != null ? scope.span().getContext().getSpanId() : "");
    }
}