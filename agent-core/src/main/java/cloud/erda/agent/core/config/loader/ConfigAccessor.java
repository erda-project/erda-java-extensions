package cloud.erda.agent.core.config.loader;

import java.util.*;

public class ConfigAccessor {
    public static ConfigAccessor Default = new ConfigAccessor();

    private Map<Class<?>, Config> configMapping;

    public ConfigAccessor() {
        this(null);
    }

    public ConfigAccessor(ClassLoader classLoader) {
        initialize(classLoader);
    }

    private void initialize(ClassLoader classLoader) {
        ConfigLoader[] configLoaders = getConfigLoaders(classLoader);
        Arrays.sort(configLoaders, new Comparator<ConfigLoader>() {
            @Override
            public int compare(ConfigLoader o1, ConfigLoader o2) {
                ConfigLoaderModule m1 = o1.getClass().getAnnotation(ConfigLoaderModule.class);
                ConfigLoaderModule m2 = o2.getClass().getAnnotation(ConfigLoaderModule.class);
                return m2.priority() - m1.priority();
            }
        });

        configMapping = new HashMap<Class<?>, Config>();

        Config[] configs = getConfigs(classLoader);
        for (ConfigLoader loader : configLoaders) {
            for (Config config : configs) {
                loader.load(config);
            }
        }
        for (Config config : configs) {
            configMapping.put(config.getClass(), config);
        }
    }

    private ConfigLoader[] getConfigLoaders(ClassLoader classLoader) {
        List<ConfigLoader> result = new ArrayList<ConfigLoader>();
        ServiceLoader<ConfigLoader> serviceLoader = ServiceLoader.load(ConfigLoader.class, classLoader);
        for (ConfigLoader configLoader : serviceLoader) {
            result.add(configLoader);
        }
        return result.toArray(new ConfigLoader[0]);
    }

    private Config[] getConfigs(ClassLoader classLoader) {
        List<Config> result = new ArrayList<Config>();
        ServiceLoader<Config> serviceLoader = ServiceLoader.load(Config.class, classLoader);
        for (Config config : serviceLoader) {
            result.add(config);
        }
        return result.toArray(new Config[0]);
    }

    public <T> T getConfig(Class<T> tClass) {
        return (T) configMapping.get(tClass);
    }
}