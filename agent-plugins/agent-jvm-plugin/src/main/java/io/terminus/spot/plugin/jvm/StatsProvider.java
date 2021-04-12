package io.terminus.spot.plugin.jvm;

import cloud.erda.agent.core.metric.Metric;
import java.util.List;

public interface StatsProvider {
    List<Metric> get();
}
