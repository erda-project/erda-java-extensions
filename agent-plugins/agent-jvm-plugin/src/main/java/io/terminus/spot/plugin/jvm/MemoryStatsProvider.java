package io.terminus.spot.plugin.jvm;

import cloud.erda.agent.core.metric.Metric;
import cloud.erda.agent.core.utils.DateTimeUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;

public class MemoryStatsProvider implements StatsProvider {

    private static final String JVM_MEMORY = "jvm_memory";

    private final MemoryMXBean mxBean = ManagementFactory.getMemoryMXBean();
    private final List<MemoryPoolMXBean> memoryPools = new ArrayList<MemoryPoolMXBean>(ManagementFactory.getMemoryPoolMXBeans());

    @Override
    public List<Metric> get() {
        List<Metric> metrics = new ArrayList<Metric>();
        addMemoryUsage(metrics, mxBean.getHeapMemoryUsage(), "heap_memory");
        addMemoryUsage(metrics, mxBean.getNonHeapMemoryUsage(), "non_heap_memory");
        for (final MemoryPoolMXBean pool : memoryPools) {
            addMemoryUsage(metrics, pool.getUsage(), pool.getName().toLowerCase());
        }
        return metrics;
    }

    private void addMemoryUsage(List<Metric> metrics, MemoryUsage memoryUsage, String name) {
        metrics.add(Metric.New(JVM_MEMORY, DateTimeUtils.currentTimeNano())
                .addTag("name", name.replace(' ', '_').toLowerCase())
                .addField("init", memoryUsage.getInit())
                .addField("used", memoryUsage.getUsed())
                .addField("committed", memoryUsage.getCommitted())
                .addField("max", memoryUsage.getMax() == -1L ? null : memoryUsage.getMax())
                .addField("usage_percent", memoryUsage.getUsed() / memoryUsage.getMax()));
    }
}
