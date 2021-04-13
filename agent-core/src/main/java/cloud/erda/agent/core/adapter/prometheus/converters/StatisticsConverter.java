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

package cloud.erda.agent.core.adapter.prometheus.converters;

import cloud.erda.agent.core.metric.Metric;
import io.prometheus.client.Collector;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuhaoyang 2020/3/20 09:49
 */
public class StatisticsConverter implements MetricConverter {

    private String label;

    public StatisticsConverter(String label) {
        this.label = label;
    }

    @Override
    public List<Metric> convert(Collector.MetricFamilySamples metricFamilySamples, long timestamp) {
        List<Metric> metrics = new ArrayList<>();
        Metric metric = Metric.New(metricFamilySamples.name, timestamp);
        int labelIndex = 0;
        if (metricFamilySamples.samples.size() > 0) {
            Collector.MetricFamilySamples.Sample first = metricFamilySamples.samples.get(0);
            for (int i = 0; i < first.labelNames.size(); i++) {
                if (label.equals(first.labelNames.get(i))) {
                    labelIndex = i;
                    continue;
                }
                metric.addTag(first.labelNames.get(i), first.labelValues.get(i));
            }
        }
        for (Collector.MetricFamilySamples.Sample sample : metricFamilySamples.samples) {
            if (sample.name.endsWith("_count")) {
                metric.addField("count", sample.value);
            } else if (sample.name.endsWith("_sum")) {
                metric.addField("sum", sample.value);
            } else {
                metric.addField(label + "_" + sample.labelValues.get(labelIndex), sample.value);
            }
        }
        metrics.add(metric);
        return metrics;
    }
}
