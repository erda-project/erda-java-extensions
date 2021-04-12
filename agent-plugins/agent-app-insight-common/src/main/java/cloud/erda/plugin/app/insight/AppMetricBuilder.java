package cloud.erda.plugin.app.insight;

import cloud.erda.agent.core.config.AddonConfig;
import cloud.erda.agent.core.config.AgentConfig;
import cloud.erda.agent.core.config.loader.ConfigAccessor;
import cloud.erda.agent.core.config.ServiceConfig;
import cloud.erda.agent.core.metric.Metric;
import cloud.erda.agent.core.tracing.TracerContext;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.DateTimeUtils;
import org.apache.skywalking.apm.agent.core.util.Strings;

import java.util.HashMap;
import java.util.Map;

import static cloud.erda.agent.core.utils.Constants.Tags.REQUEST_ID;
import static cloud.erda.agent.core.utils.Constants.Tags.TRACE_SAMPLED;

/**
 * @author liuhaoyang 2020/3/17 12:08
 */
public class AppMetricBuilder {

    private static final ServiceConfig serviceConfig = ConfigAccessor.Default.getConfig(ServiceConfig.class);
    private static final AddonConfig addonConfig = ConfigAccessor.Default.getConfig(AddonConfig.class);
    private static final AgentConfig agentConfig = ConfigAccessor.Default.getConfig(AgentConfig.class);

    private Map<String, String> tags;
    private Map<String, Object> fields;
    private String name;
    private long timestamp;
    private AppMetricWatch watch;

    public AppMetricBuilder(String name, boolean isTarget) {
        this.name = name;
        this.tags = new HashMap<String, String>();
        this.fields = new HashMap<String, Object>();
        this.timestamp = DateTimeUtils.currentTimeNano();
        this.watch = new AppMetricWatch(this.timestamp);

        if (isTarget) {
            recordContextSourceTag();
            recordConfigTargetTag();
        } else {
            recordConfigSourceTag();
        }
    }

    public AppMetricBuilder field(String key, Object value) {
        fields.put(key, value);
        return this;
    }

    public AppMetricBuilder tag(String key, String value) {
        tags.put(key, value);
        return this;
    }

    public Metric build() {
        watch.stop();
        recordElapsed(watch.elapsed());
        recordRequestId();
        Metric metric = Metric.New(name, timestamp);
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            if (!Strings.isEmpty(entry.getValue())) {
                metric.getTags().put(entry.getKey(), entry.getValue());
            }
        }
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            if (entry.getValue() instanceof String && Strings.isEmpty((String)entry.getValue())) {
                continue;
            }
            metric.getFields().put(entry.getKey(), entry.getValue());
        }
        return metric;
    }

    private void recordElapsed(long elapsed) {
        fields.put(Constants.Metrics.ELAPSED, elapsed);
    }

    private void recordContextSourceTag() {
        TracerContext tracerContext = TracerManager.tracer().context();
        String sourceAddonType = tracerContext.get(Constants.Metrics.SOURCE_ADDON_TYPE_ATTACH);
        String sourceAddonId = tracerContext.get(Constants.Metrics.SOURCE_ADDON_ID_ATTACH);
        String sourceOrgId = tracerContext.get(Constants.Metrics.SOURCE_ORG_ID);
        String sourceProjectId = tracerContext.get(Constants.Metrics.SOURCE_PROJECT_ID);
        String sourceProjectName = tracerContext.get(Constants.Metrics.SOURCE_PROJECT_NAME);
        String sourceApplicationId = tracerContext.get(Constants.Metrics.SOURCE_APPLICATION_ID);
        String sourceApplicationName = tracerContext.get(Constants.Metrics.SOURCE_APPLICATION_NAME);
        String sourceWorkspace = tracerContext.get(Constants.Metrics.SOURCE_WORKSPACE);
        String sourceRuntimeId = tracerContext.get(Constants.Metrics.SOURCE_RUNTIME_ID);
        String sourceRuntimeName = tracerContext.get(Constants.Metrics.SOURCE_RUNTIME_NAME);
        String sourceServiceName = tracerContext.get(Constants.Metrics.SOURCE_SERVICE_NAME);
        String sourceInstanceId = tracerContext.get(Constants.Metrics.SOURCE_INSTANCE_ID);
        String sourceTerminusKey = tracerContext.get(Constants.Metrics.SOURCE_TERMINUS_KEY);
        String sourceServiceId = tracerContext.get(Constants.Metrics.SOURCE_SERVICE_ID);

        tags.put(Constants.Metrics.SOURCE_ADDON_TYPE_METRIC, sourceAddonType);
        tags.put(Constants.Metrics.SOURCE_ADDON_ID_METRIC, sourceAddonId);
        tags.put(Constants.Metrics.SOURCE_ORG_ID, sourceOrgId);
        tags.put(Constants.Metrics.SOURCE_PROJECT_ID, sourceProjectId);
        tags.put(Constants.Metrics.SOURCE_PROJECT_NAME, sourceProjectName);
        tags.put(Constants.Metrics.SOURCE_APPLICATION_ID, sourceApplicationId);
        tags.put(Constants.Metrics.SOURCE_APPLICATION_NAME, sourceApplicationName);
        tags.put(Constants.Metrics.SOURCE_WORKSPACE, sourceWorkspace);
        tags.put(Constants.Metrics.SOURCE_RUNTIME_ID, sourceRuntimeId);
        tags.put(Constants.Metrics.SOURCE_RUNTIME_NAME, sourceRuntimeName);
        tags.put(Constants.Metrics.SOURCE_SERVICE_NAME, sourceServiceName);
        tags.put(Constants.Metrics.SOURCE_INSTANCE_ID, sourceInstanceId);
        tags.put(Constants.Metrics.SOURCE_TERMINUS_KEY, sourceTerminusKey);
        tags.put(Constants.Metrics.SOURCE_SERVICE_ID, sourceServiceId);
    }

    private void recordConfigSourceTag() {
        tags.put(Constants.Metrics.SOURCE_ADDON_TYPE_METRIC, addonConfig.getAddonType());
        tags.put(Constants.Metrics.SOURCE_ADDON_ID_METRIC, addonConfig.getAddonId());
        tags.put(Constants.Metrics.SOURCE_ORG_ID, serviceConfig.getOrgId());
        tags.put(Constants.Metrics.SOURCE_PROJECT_ID, serviceConfig.getProjectId());
        tags.put(Constants.Metrics.SOURCE_PROJECT_NAME, serviceConfig.getProjectName());
        tags.put(Constants.Metrics.SOURCE_APPLICATION_ID, serviceConfig.getApplicationId());
        tags.put(Constants.Metrics.SOURCE_APPLICATION_NAME, serviceConfig.getApplicationName());
        tags.put(Constants.Metrics.SOURCE_WORKSPACE, serviceConfig.getWorkspace());
        tags.put(Constants.Metrics.SOURCE_RUNTIME_ID, serviceConfig.getRuntimeId());
        tags.put(Constants.Metrics.SOURCE_RUNTIME_NAME, serviceConfig.getRuntimeName());
        tags.put(Constants.Metrics.SOURCE_SERVICE_NAME, serviceConfig.getServiceName());
        tags.put(Constants.Metrics.SOURCE_SERVICE_ID, serviceConfig.getServiceId());
        tags.put(Constants.Metrics.SOURCE_INSTANCE_ID, serviceConfig.getServiceInstanceId());
        tags.put(Constants.Metrics.SOURCE_TERMINUS_KEY, agentConfig.terminusKey());
    }

    private void recordConfigTargetTag() {
        tags.put(Constants.Metrics.TARGET_ADDON_TYPE, addonConfig.getAddonType());
        tags.put(Constants.Metrics.TARGET_ADDON_ID, addonConfig.getAddonId());
        tags.put(Constants.Metrics.TARGET_ORG_ID, serviceConfig.getOrgId());
        tags.put(Constants.Metrics.TARGET_PROJECT_ID, serviceConfig.getProjectId());
        tags.put(Constants.Metrics.TARGET_PROJECT_NAME, serviceConfig.getProjectName());
        tags.put(Constants.Metrics.TARGET_APPLICATION_ID, serviceConfig.getApplicationId());
        tags.put(Constants.Metrics.TARGET_APPLICATION_NAME, serviceConfig.getApplicationName());
        tags.put(Constants.Metrics.TARGET_WORKSPACE, serviceConfig.getWorkspace());
        tags.put(Constants.Metrics.TARGET_RUNTIME_ID, serviceConfig.getRuntimeId());
        tags.put(Constants.Metrics.TARGET_RUNTIME_NAME, serviceConfig.getRuntimeName());
        tags.put(Constants.Metrics.TARGET_SERVICE_NAME, serviceConfig.getServiceName());
        tags.put(Constants.Metrics.TARGET_SERVICE_ID, serviceConfig.getServiceId());
        tags.put(Constants.Metrics.TARGET_INSTANCE_ID, serviceConfig.getServiceInstanceId());
        tags.put(Constants.Metrics.TARGET_TERMINUS_KEY, agentConfig.terminusKey());
    }

    private void recordRequestId() {
        TracerContext tracerContext = TracerManager.tracer().context();
        tags.put(REQUEST_ID, tracerContext.requestId());
        tags.put(TRACE_SAMPLED, String.valueOf(tracerContext.sampled()));
    }
}
