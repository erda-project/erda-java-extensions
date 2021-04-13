package io.terminus.spot.plugin.jvm;

import cloud.erda.agent.core.metric.Metric;
import cloud.erda.agent.core.utils.DateTimeUtils;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;

public class ClassLoaderStatsProvider implements StatsProvider {

    private final ClassLoadingMXBean mxBean = ManagementFactory.getClassLoadingMXBean();

    @Override
    public List<Metric> get() {
        return Arrays.asList(Metric.
                New("jvm_class_loader", DateTimeUtils.currentTimeNano()).
                addField("loaded", mxBean.getTotalLoadedClassCount()).addField("unloaded", mxBean.getUnloadedClassCount()));
    }
}
