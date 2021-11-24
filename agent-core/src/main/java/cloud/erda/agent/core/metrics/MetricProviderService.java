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

package cloud.erda.agent.core.metrics;

import cloud.erda.agent.core.config.AgentConfig;
import cloud.erda.agent.core.config.ServiceConfig;
import cloud.erda.agent.core.config.loader.ConfigAccessor;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.PluginConstants;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import org.apache.skywalking.apm.agent.core.boot.BootService;
import org.apache.skywalking.apm.agent.core.boot.DependsOn;
import org.apache.skywalking.apm.agent.core.boot.ServiceManager;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

/**
 * @author liuhaoyang
 * @date 2021/10/12 19:23
 */
@DependsOn({MetricDispatcher.class})
public class MetricProviderService implements BootService {

    private Properties properties;
    private SdkMeterProvider meterProvider;
    private Meter meter;
    private MetricDispatcher reporter;

    public Meter getMeter() {
        return meter;
    }

    public SdkMeterProvider getMeterProvider() {
        return meterProvider;
    }

    @Override
    public void boot() throws Throwable {

        ServiceConfig serviceConfig = ConfigAccessor.Default.getConfig(ServiceConfig.class);
        AgentConfig agentConfig = ConfigAccessor.Default.getConfig(AgentConfig.class);
        Attributes attributes = Attributes.builder().put("terminus_key", agentConfig.terminusKey()).
                put("service_instance_id", serviceConfig.getServiceInstanceId()).
                put("service_id", serviceConfig.getServiceId()).
                put("service_instance_ip", serviceConfig.getServiceIp()).
                put("service_name", serviceConfig.getServiceName()).
                put("runtime_id", serviceConfig.getRuntimeId()).
                put("runtime_name", serviceConfig.getRuntimeName()).
                put("application_name", serviceConfig.getApplicationName()).
                put("project_name", serviceConfig.getProjectName()).
                put("workspace", serviceConfig.getWorkspace()).
                put("org_name", serviceConfig.getOrgName()).
                put("_meta", String.valueOf(true)).
                put("_metric_scope", Constants.Metrics.SCOPE_MICRO_SERVICE).
                put("_metric_scope_id", agentConfig.terminusKey()).
                build();
        Resource resource = Resource.create(attributes);

        TelegrafMetricExporter telegrafMetricExporter = new TelegrafMetricExporter(this.reporter);
        meterProvider = SdkMeterProvider.builder()
                .setResource(resource)
                .setClock(Clock.getDefault())
                .registerMetricReader(PeriodicMetricReader.create(telegrafMetricExporter, Duration.ofSeconds(30))).build();
        meter = meterProvider.get(properties.getProperty("erda.agent.name"), properties.getProperty("erda.agent.version"), "");
    }

    @Override
    public void prepare() throws Throwable {
        reporter = ServiceManager.INSTANCE.findService(MetricDispatcher.class);
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("java-agent.properties");
        properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void complete() throws Throwable {
    }

    @Override
    public void shutdown() throws Throwable {
        if (meterProvider != null) {
            meterProvider.shutdown();
        }
    }

    @Override
    public String pluginName() {
        return PluginConstants.METRIC_PLUGIN;
    }
}
