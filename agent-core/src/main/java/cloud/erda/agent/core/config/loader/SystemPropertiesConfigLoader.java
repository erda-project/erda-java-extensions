package cloud.erda.agent.core.config.loader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@ConfigLoaderModule(priority = 1)
public class SystemPropertiesConfigLoader extends ConfigLoader {

    @Override
    protected Map<String, String> getConfigurations(Object instance) {
        Map<String, String> configMap = new HashMap<String, String>();
        Iterator<Map.Entry<Object, Object>> entryIterator = System.getProperties().entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<Object, Object> prop = entryIterator.next();
            configMap.put(prop.getKey().toString(), prop.getValue().toString());
        }
        return configMap;
    }
}
