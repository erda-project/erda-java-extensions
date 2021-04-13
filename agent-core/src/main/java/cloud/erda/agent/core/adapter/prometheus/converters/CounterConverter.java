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

    @Override public List<Metric> convert(MetricFamilySamples metricFamilySamples, long timestamp) {
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
