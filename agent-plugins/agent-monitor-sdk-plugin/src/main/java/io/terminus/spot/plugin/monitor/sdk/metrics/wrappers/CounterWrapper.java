package io.terminus.spot.plugin.monitor.sdk.metrics.wrappers;

import io.terminus.dice.monitor.sdk.metrics.Counter;
import io.terminus.spot.plugin.monitor.sdk.metrics.AutoResetCounter;

/**
 * @author liuhaoyang 2020/3/18 20:09
 */
public class CounterWrapper extends BaseWrapper<AutoResetCounter> implements Counter {

    public CounterWrapper(AutoResetCounter metric) {
        super(metric);
    }

    @Override public void add(double value) {
        this.metric.inc(value);
    }

    @Override public void inc() {
        this.metric.inc();
    }

    @Override public void add(double value, String[] args) {
        this.metric.labels(args).inc(value);
    }

    @Override public void inc(String[] args) {
        this.metric.labels(args).inc();
    }
}
