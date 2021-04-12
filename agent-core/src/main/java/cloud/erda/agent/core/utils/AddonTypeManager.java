package cloud.erda.agent.core.utils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author randomnil
 */
public enum AddonTypeManager {
    /**
     * 单例
     */
    INSTANCE;

    private static final String CONFIG_CENTER_ADDON_TYPE = "configCenter";
    private static final String REGISTER_CENTER_ADDON_TYPE = "registerCenter";

    private final Map<String, String> ADDON_TYPE_SET = new ConcurrentHashMap<String, String>();

    public Set<String> getAddonTypeSet() {
        return ADDON_TYPE_SET.keySet();
    }

    public void addConfigCenter() {
        ADDON_TYPE_SET.put(CONFIG_CENTER_ADDON_TYPE, "");
    }

    public void addRegisterCenter() {
        ADDON_TYPE_SET.put(REGISTER_CENTER_ADDON_TYPE, "");
    }

}
