package io.terminus.spot.plugin.jvm;

import cloud.erda.agent.core.metric.Metric;
import cloud.erda.agent.core.utils.DateTimeUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;

public class ThreadStatsProvider implements StatsProvider {
    private final ThreadMXBean threads = ManagementFactory.getThreadMXBean();

    @Override
    public List<Metric> get() {
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(Metric.New("jvm_thread", DateTimeUtils.currentTimeNano()).addTag("name", "count").addField("count", threads.getThreadCount()));
        metrics.add(Metric.New("jvm_thread", DateTimeUtils.currentTimeNano()).addTag("name", "daemon_count").addField("state", threads.getDaemonThreadCount()));
        long[] deadThreads = threads.findDeadlockedThreads();
        metrics.add(Metric.New("jvm_thread", DateTimeUtils.currentTimeNano()).addTag("name", "dead_locked_count").addField("state", deadThreads == null ? 0 : deadThreads.length));
        java.lang.management.ThreadInfo[] threadInfo = threads.getThreadInfo(threads.getAllThreadIds(), 0);
        for (final Thread.State state : Thread.State.values()) {
            metrics.add(Metric.New("jvm_thread", DateTimeUtils.currentTimeNano()).addTag("name", state.name().toLowerCase() + "_count").addField("state", getThreadCount(state, threadInfo)));
        }
        return metrics;
    }

    private int getThreadCount(Thread.State state, java.lang.management.ThreadInfo[] allThreads) {
        int count = 0;
        for (java.lang.management.ThreadInfo info : allThreads) {
            if (info != null && info.getThreadState() == state) {
                count++;
            }
        }
        return count;
    }
}
