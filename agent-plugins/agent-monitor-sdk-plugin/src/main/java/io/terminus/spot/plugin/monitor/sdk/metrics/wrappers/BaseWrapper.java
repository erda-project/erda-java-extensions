package io.terminus.spot.plugin.monitor.sdk.metrics.wrappers;

/**
 * @author liuhaoyang 2020/3/20 15:54
 */
public abstract class BaseWrapper<W> {

    protected W metric;

    public BaseWrapper(W metric) {
        this.metric = metric;
    }
}
