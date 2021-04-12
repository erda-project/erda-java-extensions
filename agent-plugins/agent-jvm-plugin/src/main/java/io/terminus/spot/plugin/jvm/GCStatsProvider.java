package io.terminus.spot.plugin.jvm;

import cloud.erda.agent.core.metric.Metric;
import cloud.erda.agent.core.utils.DateTimeUtils;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GCStatsProvider implements StatsProvider {

    private final List<GarbageCollectorMXBean> garbageCollectors;
    private final Map<String, Long> lastCountMap = new HashMap<String, Long>();
    private final Map<String, Long> lastTimeMap = new HashMap<String, Long>();

    public GCStatsProvider() {
        garbageCollectors = new ArrayList<GarbageCollectorMXBean>(ManagementFactory.getGarbageCollectorMXBeans());
        for (GarbageCollectorMXBean gc : garbageCollectors) {
            lastCountMap.put(gc.getName(), 0L);
            lastTimeMap.put(gc.getName(), 0L);
        }
    }

    @Override
    public List<Metric> get() {
        List<Metric> metrics = new ArrayList<Metric>();
        for (GarbageCollectorMXBean gc : garbageCollectors) {
            Metric metric = Metric.New("jvm_gc", DateTimeUtils.currentTimeNano()).addTag("name", gc.getName().replace(' ', '_').toLowerCase());
            long count = gc.getCollectionCount();
            long lastCount = lastCountMap.put(gc.getName(), count);
            metric.addField("count", count - lastCount);
            long time = gc.getCollectionTime();
            long lastTime = lastTimeMap.put(gc.getName(), time);
            metric.addField("time", time - lastTime);
            metrics.add(metric);
        }
        return metrics;
    }
}
