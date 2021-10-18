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

package cloud.erda.agent.plugin.tomcat.metric;

import cloud.erda.agent.core.metrics.MetricProviderService;
import cloud.erda.agent.core.metrics.TelegrafMetricExporter;
import org.apache.skywalking.apm.agent.core.boot.DependsOn;
import org.apache.skywalking.apm.agent.core.boot.ScheduledService;
import org.apache.skywalking.apm.agent.core.boot.ServiceManager;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.loader.AgentClassLoader;

/**
 * @author liuhaoyang
 * @date 2021/10/13 15:36
 */
@DependsOn(MetricProviderService.class)
public class TomcatStatsService extends ScheduledService {

    private final ILog logger = LogManager.getLogger(TomcatStatsService.class);

    private TomcatStatsCollector tomcatStatsCollector;

    @Override
    public void beforeBoot() throws Throwable {
        MetricProviderService metricProviderService = ServiceManager.INSTANCE.findService(MetricProviderService.class);
        tomcatStatsCollector = new TomcatStatsCollector(metricProviderService.getMeter());
    }

    @Override
    protected void executing() {
        this.tomcatStatsCollector.collect();
    }

}
