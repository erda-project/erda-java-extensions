package io.terminus.spot.plugin.monitor.sdk.metrics.wrappers;

import io.terminus.dice.monitor.sdk.metrics.Summary;

/**
 * @author liuhaoyang 2020/3/20 16:37
 */
public class SummaryWrapper extends BaseWrapper<io.prometheus.client.Summary> implements Summary {

    public SummaryWrapper(io.prometheus.client.Summary metric) {
        super(metric);
    }

    @Override public void observe(double value) {
        this.metric.observe(value);
    }

    @Override public void observe(double value, String[] args) {
        this.metric.labels(args).observe(value);
    }
}
