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

package cloud.erda.agent.plugin.jvm;

import org.apache.skywalking.apm.agent.core.boot.ServiceManager;
import cloud.erda.agent.core.config.AgentConfig;
import cloud.erda.agent.core.config.loader.ConfigAccessor;
import cloud.erda.agent.core.config.ServiceConfig;
import cloud.erda.agent.core.config.ServiceMeshConfig;
import cloud.erda.agent.core.metrics.Metric;
import cloud.erda.agent.core.metrics.MetricDispatcher;

import java.util.ArrayList;
import java.util.List;

public class JVMStatsCollector {

    private final ClassLoaderStatsProvider classLoaderStatsProvider;
    private final GCStatsProvider gcStatsProvider;
    private final MemoryStatsProvider memoryStatsProvider;
    private final ThreadStatsProvider threadStatsProvider;
    private final AgentServiceNodeProvider agentServiceNodeProvider;

    public JVMStatsCollector() {
        this.classLoaderStatsProvider = new ClassLoaderStatsProvider();
        this.gcStatsProvider = new GCStatsProvider();
        this.memoryStatsProvider = new MemoryStatsProvider();
        this.threadStatsProvider = new ThreadStatsProvider();
        this.agentServiceNodeProvider = new AgentServiceNodeProvider();
    }

    public void collect() {
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.addAll(agentServiceNodeProvider.get());
        metrics.addAll(classLoaderStatsProvider.get());
        metrics.addAll(gcStatsProvider.get());
        metrics.addAll(memoryStatsProvider.get());
        metrics.addAll(threadStatsProvider.get());
        AgentConfig agentConfig = ConfigAccessor.Default.getConfig(AgentConfig.class);
        ServiceConfig serviceConfig = ConfigAccessor.Default.getConfig(ServiceConfig.class);
        ServiceMeshConfig serviceMeshConfig = ConfigAccessor.Default.getConfig(ServiceMeshConfig.class);
        for (Metric metric : metrics) {
            metric.addTag("terminus_key", agentConfig.terminusKey()).
                    addTag("instance_id", serviceConfig.getServiceInstanceId()).
                    addTag("service_instance_id", serviceConfig.getServiceInstanceId()).
                    addTag("service_id", serviceConfig.getServiceId()).
                    addTag("service_ip", serviceConfig.getServiceIp()).
                    addTag("service_name", serviceConfig.getServiceName()).
                    addTag("project_id", serviceConfig.getProjectId()).
                    addTag("runtime_id", serviceConfig.getRuntimeId()).
                    addTag("application_id", serviceConfig.getApplicationId()).
                    addTag("runtime_name", serviceConfig.getRuntimeName()).
                    addTag("application_name", serviceConfig.getApplicationName()).
                    addTag("project_name", serviceConfig.getProjectName()).
                    addTag("workspace", serviceConfig.getWorkspace()).
                    addTag("org_name", serviceConfig.getOrgName()).
                    addTag("org_id", serviceConfig.getOrgId());
        }
        ServiceManager.INSTANCE.findService(MetricDispatcher.class).dispatch(metrics.toArray(new Metric[0]));
    }
}
