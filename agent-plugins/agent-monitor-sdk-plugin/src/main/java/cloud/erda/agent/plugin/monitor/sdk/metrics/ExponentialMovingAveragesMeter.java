/*
 * Copyright (c) 2021 Terminus, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.erda.agent.plugin.monitor.sdk.metrics;

import com.codahale.metrics.Clock;
import com.codahale.metrics.ExponentialMovingAverages;
import com.codahale.metrics.MovingAverages;
import io.prometheus.client.Collector;
import io.prometheus.client.SimpleCollector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author liuhaoyang 2020/3/20 16:38
 */
public class ExponentialMovingAveragesMeter extends SimpleCollector<ExponentialMovingAveragesMeter.Child> implements Collector.Describable {

    public static ExponentialMovingAveragesMeter.Builder build(String name, String help) {
        return (new ExponentialMovingAveragesMeter.Builder()).name(name).help(help);
    }

    @Override
    protected Child newChild() {
        return new Child();
    }

    protected ExponentialMovingAveragesMeter(SimpleCollector.Builder b) {
        super(b);
    }

    @Override
    public List<Collector.MetricFamilySamples> describe() {
        return Collections.singletonList(new MetricFamilySamples(this.fullname, Type.UNTYPED, this.help, Collections.emptyList()));
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples.Sample> samples = new ArrayList();

        List<String> labelNamesCopy = new ArrayList(this.labelNames);
        labelNamesCopy.add("_custom_prom_type");

        List<String> labelNamesWithRateCopy = new ArrayList(labelNamesCopy);
        labelNamesWithRateCopy.add("rate");

        for (Map.Entry<List<String>, Child> entry : this.children.entrySet()) {
            List<String> labelValuesCopy = new ArrayList(entry.getKey());
            labelValuesCopy.add("meter");

            Child meter = entry.getValue();
            Map<String, Double> rateValues = new HashMap<>(3);
            rateValues.put("1", meter.getOneMinuteRate());
            rateValues.put("5", meter.getFiveMinuteRate());
            rateValues.put("15", meter.getFifteenMinuteRate());
            rateValues.put("mean", meter.getMeanRate());

            for (Map.Entry<String, Double> rateValue : rateValues.entrySet()) {
                List<String> labelValuesWithRateCopy = new ArrayList(labelValuesCopy);
                labelValuesWithRateCopy.add(rateValue.getKey());
                samples.add(new MetricFamilySamples.Sample(this.fullname, labelNamesWithRateCopy, labelValuesWithRateCopy, rateValue.getValue()));
            }

            samples.add(new MetricFamilySamples.Sample(this.fullname + "_count", labelNamesCopy, labelValuesCopy, meter.getCount()));
        }

        return this.familySamplesList(Type.UNTYPED, samples);
    }

    public void mark(long n) {
        this.noLabelsChild.mark(n);
    }

    public static class Child {
        private final MovingAverages movingAverages;
        private final LongAdder count;
        private final long startTime;
        private final Clock clock;

        public Child() {
            this.count = new LongAdder();
            this.clock = Clock.defaultClock();
            this.startTime = this.clock.getTick();
            this.movingAverages = new ExponentialMovingAverages(clock);
        }

        public void mark(long n) {
            this.movingAverages.tickIfNecessary();
            count.add(n);
            movingAverages.update(n);
        }

        public long getCount() {
            return this.count.sum();
        }

        public double getFifteenMinuteRate() {
            this.movingAverages.tickIfNecessary();
            return this.movingAverages.getM15Rate();
        }

        public double getFiveMinuteRate() {
            this.movingAverages.tickIfNecessary();
            return this.movingAverages.getM5Rate();
        }

        public double getMeanRate() {
            if (this.getCount() == 0) {
                return 0.0;
            } else {
                final double elapsed = this.clock.getTick() - startTime;
                return this.getCount() / elapsed * TimeUnit.SECONDS.toNanos(1);
            }
        }

        public double getOneMinuteRate() {
            this.movingAverages.tickIfNecessary();
            return this.movingAverages.getM1Rate();
        }
    }

    public static class Builder extends SimpleCollector.Builder<ExponentialMovingAveragesMeter.Builder, ExponentialMovingAveragesMeter> {
        public Builder() {
        }

        @Override
        public ExponentialMovingAveragesMeter create() {
            return new ExponentialMovingAveragesMeter(this);
        }
    }
}
