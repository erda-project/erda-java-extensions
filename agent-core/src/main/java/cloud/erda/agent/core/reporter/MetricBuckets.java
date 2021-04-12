package cloud.erda.agent.core.reporter;

import cloud.erda.agent.core.metric.Metric;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author liuhaoyang 2020/3/22 15:34
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

    @Override public Iterator<Metric[]> iterator() {
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

        @Override public boolean hasNext() {
            return index < metrics.length;
        }

        @Override public Metric[] next() {
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
