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

package cloud.erda.agent.core.tracing.span;

import cloud.erda.agent.core.config.AgentConfig;
import cloud.erda.agent.core.config.ServiceConfig;
import cloud.erda.agent.core.metrics.Metric;
import cloud.erda.agent.core.config.loader.ConfigAccessor;

import java.util.List;
import java.util.Map;

/**
 * @author liuhaoyang
 * @since 2019-01-08 11:17
 **/
public class SpanSerializer {

    private final ServiceConfig serviceConfig = ConfigAccessor.Default.getConfig(ServiceConfig.class);
    private final AgentConfig agentConfig = ConfigAccessor.Default.getConfig(AgentConfig.class);

    public Metric[] serialize(Span span) {
        Metric metric = Metric.New("span", span.getStartTime());
        metric.addTag("trace_id", span.getContext().getTraceId());
        metric.addTag("span_id", span.getContext().getSpanId());
        metric.addTag("parent_span_id", span.getContext().getParentSpanId());
        metric.addTag("operation_name", span.getOperationName());
        for (Map.Entry<String, String> tag : span.getTags().entrySet()) {
            metric.addTag(tag.getKey().replace('.', '_'), tag.getValue());
        }
        metric.addField("start_time", span.getStartTime());
        metric.addField("end_time", span.getEndTime());
        metric.addField("duration", (Math.abs(span.getEndTime() - span.getStartTime())));
        metric.addTag("service_name", serviceConfig.getServiceName());
        metric.addTag("service_id", serviceConfig.getServiceId());
        metric.addTag("service_instance_id", serviceConfig.getServiceInstanceId());
        metric.addTag("terminus_key", agentConfig.terminusKey());
        metric.addTag("project_id", serviceConfig.getProjectId());
        metric.addTag("application_id", serviceConfig.getApplicationId());
        metric.addTag("runtime_name", serviceConfig.getRuntimeName());
        metric.addTag("runtime_id", serviceConfig.getRuntimeId());
        metric.addTag("org_id", serviceConfig.getOrgId());
        metric.addTag("project_name", serviceConfig.getProjectName());
        metric.addTag("application_name", serviceConfig.getApplicationName());
        metric.addTag("workspace", serviceConfig.getWorkspace());
        List<SpanLog> spanLogs = span.getLogs();
        Metric[] metrics = new Metric[spanLogs.size() + 1];
        metrics[0] = metric;
        for (int i = 0; i < spanLogs.size(); i++) {
            SpanLog log = spanLogs.get(i);
            Metric logMetric = Metric.New("apm_span_event", log.getTimestamp());
            for (Map.Entry<String, String> tag : log.getFields().entrySet()) {
                logMetric.addTag(tag.getKey().replace('.', '_'), tag.getValue());
            }
            logMetric.addTag("trace_id", span.getContext().getTraceId());
            logMetric.addTag("span_id", span.getContext().getSpanId());
            logMetric.addTag("terminus_key", agentConfig.terminusKey());
            logMetric.addField("field_count", log.getFields().size());
            metrics[i + 1] = logMetric;
        }
        return metrics;
    }
}
