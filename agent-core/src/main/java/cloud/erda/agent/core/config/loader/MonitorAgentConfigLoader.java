package cloud.erda.agent.core.config.loader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@ConfigLoaderModule(priority = 2)
public class MonitorAgentConfigLoader extends ConfigLoader {

    private final static String OS_NAME = System.getProperty("os.name");
    private final static String OS_VERSION = System.getProperty("os.version");
    private final static String RUNTIME_VERSION = System.getProperty("java.runtime.version");

    @Override
    protected Map<String, String> getConfigurations(Object instance) {
        Map<String, String> configMap = new HashMap<String, String>();
        configMap.put("monitor.agent.platform", "JDK " + RUNTIME_VERSION);
        configMap.put("monitor.agent.os", OS_NAME + " " + OS_VERSION);
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("java-agent.properties");
        Properties p = new Properties();
        try {
            p.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Enumeration<?> names = p.propertyNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            configMap.put(name, p.getProperty(name));
        }
        return configMap;
    }
}
