package io.terminus.spot.plugin.spring;

import cloud.erda.agent.core.tracing.TracerSnapshot;
import cloud.erda.plugin.app.insight.AppMetricBuilder;

import java.util.Map;

/**
 * @author randomnil
 */
public class EnhanceCommonInfo {

    private TracerSnapshot snapshot;

    private Map<String, String> context;

    private AppMetricBuilder appMetricBuilder;

    public TracerSnapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(TracerSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public Map<String, String> getContext() {
        return context;
    }

    public void setContext(Map<String, String> context) {
        this.context = context;
    }

    public AppMetricBuilder getAppMetricBuilder() {
        return appMetricBuilder;
    }

    public void setAppMetricBuilder(AppMetricBuilder appMetricBuilder) {
        this.appMetricBuilder = appMetricBuilder;
    }
}
