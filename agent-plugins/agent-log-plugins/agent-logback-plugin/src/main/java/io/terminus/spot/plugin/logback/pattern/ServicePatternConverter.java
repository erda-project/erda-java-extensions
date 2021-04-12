package io.terminus.spot.plugin.logback.pattern;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import cloud.erda.agent.core.config.loader.ConfigAccessor;
import cloud.erda.agent.core.config.ServiceConfig;

public class ServicePatternConverter extends ClassicConverter {

    @Override
    public String convert(ILoggingEvent event) {
        ServiceConfig config = ConfigAccessor.Default.getConfig(ServiceConfig.class);
        return config.getServiceName();
    }
}
