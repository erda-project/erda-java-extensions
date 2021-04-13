package io.terminus.spot.plugin.monitor.sdk.config;

import cloud.erda.agent.core.config.loader.ConfigAccessor;
import cloud.erda.agent.core.config.ServiceConfig;
import cloud.erda.agent.core.tracing.Scope;
import cloud.erda.agent.core.tracing.TracerManager;
import io.terminus.dice.monitor.sdk.config.Config;

/**
 * @author liuhaoyang 2020/3/17 23:51
 */
public class MonitorConfig implements Config {

    private static final ServiceConfig serviceConfig = ConfigAccessor.Default.getConfig(ServiceConfig.class);

    @Override public String getRequestId() {
        String requestId = TracerManager.tracer().context().requestId();
        return requestId == null ? "" : requestId;
    }

    @Override public String getSpanId() {
        Scope scope = TracerManager.tracer().active();
        if (scope != null) {
            return scope.span().getContext().getSpanId();
        }
        return "";
    }

    @Override public String getServiceName() {
        return serviceConfig.getServiceName();
    }

    @Override public String getServiceInstanceId() {
        return serviceConfig.getServiceInstanceId();
    }

    @Override public String getApplicationId() {
        return serviceConfig.getApplicationId();
    }

    @Override public String getApplicationName() {
        return serviceConfig.getApplicationName();
    }

    @Override public String getProjectId() {
        return serviceConfig.getProjectId();
    }

    @Override public String getProjectName() {
        return serviceConfig.getProjectName();
    }
}
