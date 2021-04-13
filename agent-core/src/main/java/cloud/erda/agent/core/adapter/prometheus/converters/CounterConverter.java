/*
 Copyright (c) 2021 Terminus, Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package cloud.erda.agent.core.adapter.prometheus.converters;

import cloud.erda.agent.core.metric.Metric;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuhaoyang 2020/3/19 15:59
 */
public class CounterConverter implements MetricConverter {

    public static final MetricConverter instance = new CounterConverter();

    @Override
    public List<Metric> convert(MetricFamilySamples metricFamilySamples, long timestamp) {
        List<Metric> metrics = new ArrayList<>(metricFamilySamples.samples.size());
        for (Sample sample : metricFamilySamples.samples) {
            Metric metric = Metric.New(metricFamilySamples.name, timestamp);
            metric.addField("count", sample.value);
            for (int i = 0; i < sample.labelNames.size(); i++) {
                metric.addTag(sample.labelNames.get(i), sample.labelValues.get(i));
            }
            metrics.add(metric);
        }
        return metrics;
    }
}
