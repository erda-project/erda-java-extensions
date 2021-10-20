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

package cloud.erda.agent.plugin.app.insight.invoke;

import cloud.erda.agent.core.metrics.Metric;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.DateTime;
import cloud.erda.agent.plugin.app.insight.Configs;
import cloud.erda.agent.plugin.app.insight.MetricBuilder;
import cloud.erda.agent.plugin.app.insight.StopWatch;

/**
 * @author liuhaoyang
 * @date 2021/8/3 11:54
 */
public class InvokeMetricBuilder implements MetricBuilder {

    private Metric metric;
    private StopWatch watch;

    InvokeMetricBuilder(long start) {
        this.metric = Metric.New(Constants.Metrics.APPLICATION_INVOKE, start);
        this.watch = new StopWatch(start);
    }

    @Override
    public MetricBuilder tag(String key, String value) {
        metric.addTag(key, value);
        return this;
    }

    @Override
    public MetricBuilder field(String key, Object value) {
        metric.addField(key, value);
        return this;
    }

    @Override
    public Metric build() {
        watch.stop();
        metric.addField(Constants.Metrics.ELAPSED, watch.elapsed());
        metric.addTag("terminus_key", Configs.AgentConfig.terminusKey()).
                addTag("service_instance_id", Configs.ServiceConfig.getServiceInstanceId()).
                addTag("service_id", Configs.ServiceConfig.getServiceId()).
                addTag("service_ip", Configs.ServiceConfig.getServiceIp()).
                addTag("service_name", Configs.ServiceConfig.getServiceName()).
                addTag("runtime_name", Configs.ServiceConfig.getRuntimeName()).
                addTag("application_name", Configs.ServiceConfig.getApplicationName()).
                addTag("project_name", Configs.ServiceConfig.getProjectName()).
                addTag("workspace", Configs.ServiceConfig.getWorkspace()).
                addTag("org_name", Configs.ServiceConfig.getOrgName()).
                addTag("org_id", Configs.ServiceConfig.getOrgId());
        return metric;
    }

    public static MetricBuilder New() {
        return new InvokeMetricBuilder(DateTime.currentTimeNano());
    }
}
