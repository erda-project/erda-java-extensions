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

    @Override public List<Metric> convert(Collector.MetricFamilySamples metricFamilySamples, long timestamp) {
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
