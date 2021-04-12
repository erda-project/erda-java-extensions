package io.terminus.spot.plugin.jvm;

import org.apache.skywalking.apm.agent.core.boot.ScheduledService;

public class JVMStatsService extends ScheduledService {

    private final JVMStatsCollector collector = new JVMStatsCollector();

    @Override
    protected void executing() {
        collector.collect();
    }

    @Override
    protected long initialDelay() {
        return 20;
    }
}
