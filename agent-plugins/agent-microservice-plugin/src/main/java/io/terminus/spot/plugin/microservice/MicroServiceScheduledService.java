package io.terminus.spot.plugin.microservice;

import org.apache.skywalking.apm.agent.core.boot.ScheduledService;
import cloud.erda.agent.core.utils.AddonTypeManager;
import cloud.erda.plugin.app.insight.AppMetricBuilder;
import cloud.erda.plugin.app.insight.AppMetricRecorder;
import cloud.erda.plugin.app.insight.AppMetricUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author randomnil
 */
public class MicroServiceScheduledService extends ScheduledService {

    @Override
    protected void executing() {
        for (String addonType : AddonTypeManager.INSTANCE.getAddonTypeSet()) {
            AppMetricBuilder appMetricBuilder = AppMetricUtils.createMiroServiceMetric(addonType);
            AppMetricRecorder.record(appMetricBuilder);
        }
    }

    @Override
    protected long initialDelay() {
        return 1;
    }

    @Override
    protected long period() {
        return 5;
    }

    @Override
    protected TimeUnit timeUnit() {
        return TimeUnit.MINUTES;
    }
}