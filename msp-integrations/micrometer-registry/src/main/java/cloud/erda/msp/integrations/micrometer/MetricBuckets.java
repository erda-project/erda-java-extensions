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

package cloud.erda.msp.integrations.micrometer;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author liuhaoyang
 * @date 2021/10/10 14:11
 */
public class MetricBuckets implements Iterable<Metric[]> {

    private final Metric[] metrics;
    private final int bucket;

    public MetricBuckets(Metric... metrics) {
        this(metrics, 10);
    }

    public MetricBuckets(Metric[] metrics, int bucket) {
        this.metrics = metrics;
        this.bucket = bucket;
    }

    public MetricBuckets(Collection<Metric> metrics, int bucket) {
        this(metrics.toArray(new Metric[0]), bucket);
    }

    @Override
    public Iterator<Metric[]> iterator() {
        return new MetricsIterator(this.metrics, this.bucket);
    }

    private static class MetricsIterator implements Iterator<Metric[]> {

        private final Metric[] metrics;
        private final int maxBucket;

        private int index = 0;

        public MetricsIterator(Metric[] metrics, int maxBucket) {
            this.metrics = metrics;
            this.maxBucket = maxBucket;
        }

        @Override
        public boolean hasNext() {
            return index < metrics.length;
        }

        @Override
        public Metric[] next() {
            if (index >= metrics.length) {
                return new Metric[0];
            }
            int bucket = metrics.length - index;
            if (bucket > maxBucket) {
                bucket = maxBucket;
            }
            Metric[] result = new Metric[bucket];
            for (int i = 0; i < bucket; i++) {
                result[i] = metrics[index++];
            }
            return result;
        }
    }
}
