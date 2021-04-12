package cloud.erda.agent.core.config;

import cloud.erda.agent.core.config.loader.ConfigLoader;
import cloud.erda.agent.core.config.loader.ConfigLoaderModule;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ConfigLoaderModule(priority = 2)
public class ServiceConfigLoader extends ConfigLoader {
    @Override
    protected Map<String, String> getConfigurations(Object instance) {
        Map<String, String> configMap = new HashMap<String, String>();
        configMap.put("SERVICE_INSTANCE_ID", UUID.randomUUID().toString());
        return configMap;
    }
}
