package io.terminus.spot.plugin.jvm;

import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.config.AgentConfig;
import cloud.erda.agent.core.config.loader.ConfigAccessor;
import cloud.erda.agent.core.metric.Metric;
import cloud.erda.agent.core.utils.DateTimeUtils;

import java.util.Arrays;
import java.util.List;

public class AgentServiceNodeProvider implements StatsProvider {

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
