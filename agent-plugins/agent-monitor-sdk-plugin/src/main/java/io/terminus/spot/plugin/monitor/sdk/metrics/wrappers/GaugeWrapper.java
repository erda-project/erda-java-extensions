package io.terminus.spot.plugin.monitor.sdk.metrics.wrappers;

import io.terminus.dice.monitor.sdk.metrics.Gauge;

/**
 * @author liuhaoyang 2020/3/20 15:52
 */
public class GaugeWrapper extends BaseWrapper<io.prometheus.client.Gauge> implements Gauge {

    public GaugeWrapper(io.prometheus.client.Gauge metric) {
        super(metric);
    }

    @Override public void value(double value) {
        this.metric.set(value);
    }

    @Override public void value(double value, String[] args) {
        this.metric.labels(args).set(value);
    }
}
