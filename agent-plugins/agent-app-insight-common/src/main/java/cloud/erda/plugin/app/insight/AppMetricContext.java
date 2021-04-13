package cloud.erda.plugin.app.insight;

import cloud.erda.agent.core.config.AddonConfig;
import cloud.erda.agent.core.config.AgentConfig;
import cloud.erda.agent.core.config.loader.ConfigAccessor;
import cloud.erda.agent.core.config.ServiceConfig;
import cloud.erda.agent.core.tracing.TracerContext;
import cloud.erda.agent.core.utils.Constants;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author: liuhaoyang
 * @create: 2019-01-21 18:27
 **/
public class AppMetricContext implements TracerContext.ContextIterator {
    public static final TracerContext.ContextIterator instance = new AppMetricContext();

    private final Map<String, String> map = new HashMap<String, String>();

    public AppMetricContext() {
        ServiceConfig serviceConfig = ConfigAccessor.Default.getConfig(ServiceConfig.class);
        AddonConfig addonConfig = ConfigAccessor.Default.getConfig(AddonConfig.class);
        AgentConfig agentConfig = ConfigAccessor.Default.getConfig(AgentConfig.class);
        map.put(Constants.Metrics.SOURCE_ADDON_TYPE_ATTACH, addonConfig.getAddonType());
        map.put(Constants.Metrics.SOURCE_ADDON_ID_ATTACH, addonConfig.getAddonId());
        map.put(Constants.Metrics.SOURCE_ORG_ID, serviceConfig.getOrgId());
        map.put(Constants.Metrics.SOURCE_PROJECT_ID, serviceConfig.getProjectId());
        map.put(Constants.Metrics.SOURCE_PROJECT_NAME, serviceConfig.getProjectName());
        map.put(Constants.Metrics.SOURCE_APPLICATION_ID, serviceConfig.getApplicationId());
        map.put(Constants.Metrics.SOURCE_APPLICATION_NAME, serviceConfig.getApplicationName());
        map.put(Constants.Metrics.SOURCE_RUNTIME_ID, serviceConfig.getRuntimeId());
        map.put(Constants.Metrics.SOURCE_RUNTIME_NAME, serviceConfig.getRuntimeName());
        map.put(Constants.Metrics.SOURCE_SERVICE_NAME, serviceConfig.getServiceName());
        map.put(Constants.Metrics.SOURCE_SERVICE_ID, serviceConfig.getServiceId());
        map.put(Constants.Metrics.SOURCE_TERMINUS_KEY, agentConfig.terminusKey());
        map.put(Constants.Metrics.SOURCE_WORKSPACE, serviceConfig.getWorkspace());
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return map.entrySet().iterator();
    }
}
