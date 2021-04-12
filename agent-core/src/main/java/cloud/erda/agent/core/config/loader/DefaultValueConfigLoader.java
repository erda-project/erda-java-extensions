package cloud.erda.agent.core.config.loader;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liuhaoyang
 */
@ConfigLoaderModule(priority = 9)
public class DefaultValueConfigLoader extends ConfigLoader {

    @Override
    protected Map<String, String> getConfigurations(Object instance) {
        Map<String, String> configMap = new HashMap<String, String>();
        Class insClass = instance.getClass();
        Field[] fields = insClass.getDeclaredFields();
        for (Field field : fields) {
            Configuration configuration = field.getAnnotation(Configuration.class);
            if (configuration != null) {
                configMap.put(configuration.name(), configuration.defaultValue());
            }
        }
        return configMap;
    }

    @Override
    protected boolean validateValue(String name, String value, Object instance, Field field) {
        return true;
    }
}