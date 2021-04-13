package cloud.erda.agent.core.config.loader;

import java.util.*;

@ConfigLoaderModule(priority = 0)
public class EnvironmentVariableConfigLoader extends ConfigLoader {
    @Override
    protected Map<String, String> getConfigurations(Object instance) {
        Map<String, String> configMap = new HashMap<String, String>();
        Iterator<Map.Entry<String, String>> entryIterator = System.getenv().entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, String> prop = entryIterator.next();
            configMap.put(prop.getKey(), prop.getValue());
        }
        return configMap;
    }
}
