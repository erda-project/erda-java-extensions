package io.terminus.spot.plugin.log.config;

import cloud.erda.agent.core.config.loader.Config;
import cloud.erda.agent.core.config.loader.Configuration;

/**
 * @author liuhaoyang
 * @date 2020/11/17 10:54
 */
public class LogConfig implements Config {

    @Configuration(name = "TERMINUS_LOG_FORCE_STDOUT", defaultValue = "true")
    private boolean forceStdout;

    public Boolean getForceStdout() {
        return forceStdout;
    }
}
