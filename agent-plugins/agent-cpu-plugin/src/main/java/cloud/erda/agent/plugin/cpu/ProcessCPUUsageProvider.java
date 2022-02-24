/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package cloud.erda.agent.plugin.cpu;

import cloud.erda.agent.core.metrics.Metric;
import cloud.erda.agent.core.utils.DateTime;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class ProcessCPUUsageProvider implements CPUDefaultProvider {

    private static final String METRIC_NAME = "process_cpu_usage";
    private final ThreadMXBean threadBean;
    boolean isFirst = true;
    private long preNanoTime = System.nanoTime();
    private long preUsedNanoTime = 0;

    public ProcessCPUUsageProvider() {
        threadBean = ManagementFactory.getThreadMXBean();
    }

    public Metric get() {
        Metric metric = Metric.New(METRIC_NAME, DateTime.currentTimeNano()).
                addField("process_cpu_usage", this.calculateProcessCpuUsage());
        this.addDefaultTags(metric);
        return metric;
    }

    /**
     * Calculate the CPU usage of all threads in the current process
     *
     * @return calculateProcessCpuUsage
     */
    public double calculateProcessCpuUsage() {
        long currentNanoTime = System.nanoTime();
        long processUsedTotalNanoTime = 0;
        for (long id : threadBean.getAllThreadIds()) {
            processUsedTotalNanoTime += threadBean.getThreadCpuTime(id);
        }
        long usedNanoTime = processUsedTotalNanoTime - preUsedNanoTime;

        if (isFirst) {
            isFirst = false;
            preNanoTime = currentNanoTime;
            preUsedNanoTime = processUsedTotalNanoTime;
            return 0;
        }

        long totalPassedNanoTime = currentNanoTime - preNanoTime;

        preNanoTime = currentNanoTime;
        preUsedNanoTime = processUsedTotalNanoTime;

        double cpuUsage = ((double) usedNanoTime) / totalPassedNanoTime * 100;
        if (cpuUsage < 0) {
            return 0;
        }
        return (double) Math.round(cpuUsage * 100) / 100;
    }
}