package cloud.erda.agent.plugin.rocketmq.v4;

import cloud.erda.agent.core.tracing.TracerSnapshot;
import cloud.erda.agent.plugin.app.insight.AppMetricBuilder;

/**
 * @author randomnil
 */
public class MessageSendAsyncInfo {
    private TracerSnapshot tracerSnapshot;
    private AppMetricBuilder appMetricBuilder;

    public MessageSendAsyncInfo(TracerSnapshot tracerSnapshot, AppMetricBuilder appMetricBuilder) {
        this.tracerSnapshot = tracerSnapshot;
        this.appMetricBuilder = appMetricBuilder;
    }

    public TracerSnapshot getTracerSnapshot() {
        return tracerSnapshot;
    }

    public AppMetricBuilder getAppMetricBuilder() {
        return appMetricBuilder;
    }
}
