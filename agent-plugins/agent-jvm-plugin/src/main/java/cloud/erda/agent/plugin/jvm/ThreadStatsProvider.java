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

import cloud.erda.agent.core.metrics.Metric;
import cloud.erda.agent.core.utils.DateTime;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;

public class ThreadStatsProvider implements StatsProvider {
    private final ThreadMXBean threads = ManagementFactory.getThreadMXBean();

    @Override
    public List<Metric> get() {
        List<Metric> metrics = new ArrayList<Metric>();
        metrics.add(Metric.New("jvm_thread", DateTime.currentTimeNano()).addTag("name", "count").addField("count", threads.getThreadCount()));
        ThreadGroup group = Thread.currentThread().getThreadGroup();

        // 循环遍历获取Group中所有Thread信息
        while (group.getParent() != null) {
            group = group.getParent(); // 获取上一级线程组
        }

        int daemonCount = 0;
        int totalCount = group.activeCount();
        Thread[] daemonThreads = new Thread[totalCount];
        group.enumerate(daemonThreads, true);

        // 计算daemon线程数
        for (int i = 0; i < totalCount; i++) {
            if (daemonThreads[i].isDaemon()) {
                daemonCount ++;
            }
        }

        metrics.add(Metric.New("jvm_thread", DateTime.currentTimeNano()).addTag("name", "daemon_count").addField("state", daemonCount));
        long[] deadThreads = threads.findDeadlockedThreads();
        metrics.add(Metric.New("jvm_thread", DateTime.currentTimeNano()).addTag("name", "dead_locked_count").addField("state", deadThreads == null ? 0 : deadThreads.length));
        java.lang.management.ThreadInfo[] threadInfo = threads.getThreadInfo(threads.getAllThreadIds(), 0);
        for (final Thread.State state : Thread.State.values()) {
            metrics.add(Metric.New("jvm_thread", DateTime.currentTimeNano()).addTag("name", state.name().toLowerCase() + "_count").addField("state", getThreadCount(state, threadInfo)));
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
