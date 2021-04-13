package io.terminus.spot.plugin.log4j2.pattern.converter;

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