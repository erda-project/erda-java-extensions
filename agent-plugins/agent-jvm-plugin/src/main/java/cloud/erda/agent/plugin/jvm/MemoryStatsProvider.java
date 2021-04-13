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
