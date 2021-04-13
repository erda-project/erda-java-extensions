package cloud.erda.agent.core.adapter.prometheus.converters;

import cloud.erda.agent.core.metric.Metric;
import io.prometheus.client.Collector;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuhaoyang 2020/3/19 17:26
 */
public class GaugeConverter implements MetricConverter {

    public static final MetricConverter instance = new GaugeConverter();

    @Override public List<Metric> convert(Collector.MetricFamilySamples metricFamilySamples, long timestamp) {
        List<Metric> metrics = new ArrayList<>(metricFamilySamples.samples.size());
        for (Collector.MetricFamilySamples.Sample sample : metricFamilySamples.samples) {
            Metric metric = Metric.New(metricFamilySamples.name, timestamp);
            metric.addField("value", sample.value);
            for (int i = 0; i < sample.labelNames.size(); i++) {
                metric.addTag(sample.labelNames.get(i), sample.labelValues.get(i));
            }
            metrics.add(metric);
        }
        return metrics;
    }
}
