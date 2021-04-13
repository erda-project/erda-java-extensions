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

import cloud.erda.agent.core.config.loader.ConfigAccessor;
import cloud.erda.agent.core.config.ServiceConfig;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

public class ServicePatternConverter extends LogEventPatternConverter {
    /**
     * Singleton.
     */
    private static final ServicePatternConverter INSTANCE = new ServicePatternConverter();

    /**
     * Private constructor.
     */
    private ServicePatternConverter() {
        super("TerminusService", "terminusService");
    }

    /**
     * Obtains an instance of ThreadPatternConverter.
     *
     * @param options options, currently ignored, may be null.
     * @return instance of ThreadPatternConverter.
     */
    public static ServicePatternConverter newInstance(final String[] options) {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final StringBuilder toAppendTo) {
        ServiceConfig config = ConfigAccessor.Default.getConfig(ServiceConfig.class);
        if (config == null) {
            return;
        }

        toAppendTo.append(config.getServiceName() != null ? config.getServiceName() : "");
    }
}