package cloud.erda.agent.core.adapter.prometheus.converters;

import cloud.erda.agent.core.metric.Metric;
import io.prometheus.client.Collector.MetricFamilySamples;

import java.util.List;

/**
 * @author liuhaoyang 2020/3/19 15:55
 */
public interface MetricConverter {
    List<Metric> convert(MetricFamilySamples metricFamilySamples, long timestamp);
}
