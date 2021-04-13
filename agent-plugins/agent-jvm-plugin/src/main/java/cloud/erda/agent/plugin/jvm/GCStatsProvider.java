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
