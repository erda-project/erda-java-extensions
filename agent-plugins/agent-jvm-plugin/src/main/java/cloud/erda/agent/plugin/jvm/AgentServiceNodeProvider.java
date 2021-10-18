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

import cloud.erda.agent.core.config.AgentConfig;
import cloud.erda.agent.core.config.loader.ConfigAccessor;
import cloud.erda.agent.core.metrics.Metric;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.DateTimeUtils;

import java.util.Arrays;
import java.util.List;

public class AgentServiceNodeProvider implements cloud.erda.agent.plugin.jvm.StatsProvider {

    private final long startTime = System.currentTimeMillis();

    @Override
    public List<Metric> get() {
        AgentConfig config = ConfigAccessor.Default.getConfig(AgentConfig.class);
        Metric metric = Metric.New(Constants.Metrics.APPLICATION_SERVICE_NODE, DateTimeUtils.currentTimeNano()).
                addTag("service_agent_name", config.agentName()).
                addTag("service_agent_version", config.agentVersion()).
                addTag("service_agent_os_info", config.agentOsInfo()).
                addTag("service_agent_platform", config.agentPlatform()).
                addField("start_time", startTime);
        return Arrays.asList(metric);
    }
}
