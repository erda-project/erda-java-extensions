/*
 * Copyright (c) 2021 Terminus, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.erda.agent.plugin.app.insight.transaction;

import cloud.erda.agent.core.metric.Metric;
import cloud.erda.agent.core.tracing.TracerContext;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.DateTimeUtils;
import cloud.erda.agent.plugin.app.insight.Configs;
import cloud.erda.agent.plugin.app.insight.MetricBuilder;
import cloud.erda.agent.plugin.app.insight.StopWatch;
import org.apache.skywalking.apm.agent.core.util.Strings;

import java.util.HashMap;
import java.util.Map;

import static cloud.erda.agent.core.utils.Constants.Tags.REQUEST_ID;
import static cloud.erda.agent.core.utils.Constants.Tags.TRACE_SAMPLED;

/**
 * @author liuhaoyang 2020/3/17 12:08
 */
public class TransactionMetricBuilder implements MetricBuilder {
    
    private Map<String, String> tags;
    private Map<String, Object> fields;
    private String name;
    private long timestamp;
    private StopWatch watch;

    public TransactionMetricBuilder(String name, boolean isTarget) {
        this.name = name;
        this.tags = new HashMap<String, String>();
        this.fields = new HashMap<String, Object>();
        this.timestamp = DateTimeUtils.currentTimeNano();
        this.watch = new StopWatch(this.timestamp);

        if (isTarget) {
            recordContextSourceTag();
            recordConfigTargetTag();
        } else {
            recordConfigSourceTag();
        }
    }

    public MetricBuilder field(String key, Object value) {
        fields.put(key, value);
        return this;
    }

    public MetricBuilder tag(String key, String value) {
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
            if (entry.getValue() instanceof String && Strings.isEmpty((String) entry.getValue())) {
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
        tags.put(Constants.Metrics.SOURCE_ADDON_TYPE_METRIC, Configs.AddonConfig.getAddonType());
        tags.put(Constants.Metrics.SOURCE_ADDON_ID_METRIC, Configs.AddonConfig.getAddonId());
        tags.put(Constants.Metrics.SOURCE_ORG_ID, Configs.ServiceConfig.getOrgId());
        tags.put(Constants.Metrics.SOURCE_PROJECT_ID, Configs.ServiceConfig.getProjectId());
        tags.put(Constants.Metrics.SOURCE_PROJECT_NAME, Configs.ServiceConfig.getProjectName());
        tags.put(Constants.Metrics.SOURCE_APPLICATION_ID, Configs.ServiceConfig.getApplicationId());
        tags.put(Constants.Metrics.SOURCE_APPLICATION_NAME, Configs.ServiceConfig.getApplicationName());
        tags.put(Constants.Metrics.SOURCE_WORKSPACE, Configs.ServiceConfig.getWorkspace());
        tags.put(Constants.Metrics.SOURCE_RUNTIME_ID, Configs.ServiceConfig.getRuntimeId());
        tags.put(Constants.Metrics.SOURCE_RUNTIME_NAME, Configs.ServiceConfig.getRuntimeName());
        tags.put(Constants.Metrics.SOURCE_SERVICE_NAME, Configs.ServiceConfig.getServiceName());
        tags.put(Constants.Metrics.SOURCE_SERVICE_ID, Configs.ServiceConfig.getServiceId());
        tags.put(Constants.Metrics.SOURCE_INSTANCE_ID, Configs.ServiceConfig.getServiceInstanceId());
        tags.put(Constants.Metrics.SOURCE_TERMINUS_KEY, Configs.AgentConfig.terminusKey());
    }

    private void recordConfigTargetTag() {
        tags.put(Constants.Metrics.TARGET_ADDON_TYPE, Configs.AddonConfig.getAddonType());
        tags.put(Constants.Metrics.TARGET_ADDON_ID, Configs.AddonConfig.getAddonId());
        tags.put(Constants.Metrics.TARGET_ORG_ID, Configs.ServiceConfig.getOrgId());
        tags.put(Constants.Metrics.TARGET_PROJECT_ID, Configs.ServiceConfig.getProjectId());
        tags.put(Constants.Metrics.TARGET_PROJECT_NAME, Configs.ServiceConfig.getProjectName());
        tags.put(Constants.Metrics.TARGET_APPLICATION_ID, Configs.ServiceConfig.getApplicationId());
        tags.put(Constants.Metrics.TARGET_APPLICATION_NAME, Configs.ServiceConfig.getApplicationName());
        tags.put(Constants.Metrics.TARGET_WORKSPACE, Configs.ServiceConfig.getWorkspace());
        tags.put(Constants.Metrics.TARGET_RUNTIME_ID, Configs.ServiceConfig.getRuntimeId());
        tags.put(Constants.Metrics.TARGET_RUNTIME_NAME, Configs.ServiceConfig.getRuntimeName());
        tags.put(Constants.Metrics.TARGET_SERVICE_NAME, Configs.ServiceConfig.getServiceName());
        tags.put(Constants.Metrics.TARGET_SERVICE_ID, Configs.ServiceConfig.getServiceId());
        tags.put(Constants.Metrics.TARGET_INSTANCE_ID, Configs.ServiceConfig.getServiceInstanceId());
        tags.put(Constants.Metrics.TARGET_TERMINUS_KEY, Configs.AgentConfig.terminusKey());
    }

    private void recordRequestId() {
        TracerContext tracerContext = TracerManager.tracer().context();
        tags.put(REQUEST_ID, tracerContext.requestId());
        tags.put(TRACE_SAMPLED, String.valueOf(tracerContext.sampled()));
    }
}
