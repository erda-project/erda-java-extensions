package io.terminus.spot.plugin.monitor.sdk.metrics.wrappers;

import io.terminus.dice.monitor.sdk.metrics.Meter;
import io.terminus.spot.plugin.monitor.sdk.metrics.ExponentialMovingAveragesMeter;

/**
 * @author liuhaoyang 2020/3/22 11:34
 */
public class MeterWrapper extends BaseWrapper<ExponentialMovingAveragesMeter> implements Meter {

    public MeterWrapper(ExponentialMovingAveragesMeter metric) {
        super(metric);
    }

    @Override public void mark(double value) {
        this.metric.mark((long)value);
    }

    @Override public void mark(double value, String[] args) {
        this.metric.labels(args).mark((long)value);
    }
}
