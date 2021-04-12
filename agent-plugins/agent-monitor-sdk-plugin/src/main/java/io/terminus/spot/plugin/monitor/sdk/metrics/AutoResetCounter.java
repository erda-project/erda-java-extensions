package io.terminus.spot.plugin.monitor.sdk.metrics;

import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.DoubleAdder;
import io.prometheus.client.SimpleCollector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author liuhaoyang 2020/3/18 11:56
 */
public class AutoResetCounter extends SimpleCollector<AutoResetCounter.Child> implements Collector.Describable {

    protected AutoResetCounter(SimpleCollector.Builder b) {
        super(b);
    }

    public static AutoResetCounter.Builder build(String name, String help) {
        return new Builder().name(name).help(help);
    }

    @Override public List<MetricFamilySamples> describe() {
        return Collections.singletonList(new CounterMetricFamily(this.fullname, this.help, this.labelNames));
    }

    @Override protected Child newChild() {
        return new Child();
    }

    @Override public List<MetricFamilySamples> collect() {
        ArrayList samples = new ArrayList(this.children.size());

        for (Map.Entry<List<String>, Child> entry : this.children.entrySet()) {
            Map.Entry<List<String>, Child> c = (Map.Entry)entry;
            samples.add(new MetricFamilySamples.Sample(this.fullname, this.labelNames, (List)c.getKey(), c.getValue().get()));
        }
        return this.familySamplesList(Type.COUNTER, samples);
    }

    public void inc() {
        this.inc(1.0D);
    }

    public void inc(double amt) {
        this.noLabelsChild.inc(amt);
    }

    public double get() {
        return this.noLabelsChild.get();
    }

    public static class Child {
        private final DoubleAdder value = new DoubleAdder();

        public Child() {
        }

        public void inc() {
            this.inc(1.0D);
        }

        public void inc(double amt) {
            if (amt < 0.0D) {
                throw new IllegalArgumentException("Amount to increment must be non-negative.");
            } else {
                this.value.add(amt);
            }
        }

        public double get() {
            double value = this.value.sum();
            this.value.reset();
            return value;
        }
    }

    public static class Builder extends SimpleCollector.Builder<AutoResetCounter.Builder, AutoResetCounter> {
        public Builder() {
        }

        @Override public AutoResetCounter create() {
            return new AutoResetCounter(this);
        }
    }
}
