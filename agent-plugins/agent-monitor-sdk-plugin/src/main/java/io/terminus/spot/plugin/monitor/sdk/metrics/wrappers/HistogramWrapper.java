package io.terminus.spot.plugin.monitor.sdk.metrics.wrappers;

import io.terminus.dice.monitor.sdk.metrics.Histogram;

/**
 * @author liuhaoyang 2020/3/20 16:34
 */
public class HistogramWrapper extends BaseWrapper<io.prometheus.client.Histogram> implements Histogram {

    public HistogramWrapper(io.prometheus.client.Histogram metric) {
        super(metric);
    }

    @Override public void observe(double value) {
        this.metric.observe(value);
    }

    @Override public void observe(double value, String[] args) {
        this.metric.labels(args).observe(value);
    }
}
