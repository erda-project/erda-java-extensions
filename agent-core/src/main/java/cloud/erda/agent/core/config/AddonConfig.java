package cloud.erda.agent.core.config;

import cloud.erda.agent.core.config.loader.Config;
import cloud.erda.agent.core.config.loader.Configuration;

/**
 * @author: liuhaoyang
 * @create: 2020-02-19 14:32
 **/
public class AddonConfig implements Config {

    @Configuration(name = "ADDON_TYPE")
    private String addonType;

    @Configuration(name = "ADDON_ID")
    private String addonId;

    public String getAddonId() {
        return addonId;
    }

    public String getAddonType() {
        return addonType;
    }
}
