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

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.step.StepMeterRegistry;
import io.micrometer.core.instrument.util.MeterPartition;
import io.micrometer.core.instrument.util.NamedThreadFactory;
import io.micrometer.core.instrument.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author liuhaoyang
 * @date 2021/10/10 13:55
 */
public class ErdaMeterRegistry extends StepMeterRegistry {

    private static final ThreadFactory DEFAULT_THREAD_FACTORY = new NamedThreadFactory("erda-metrics-publisher");
    private final ErdaConfig config;
    private final Logger logger;
    private MetricReporter reporter;

    public ErdaMeterRegistry(ErdaConfig config, Clock clock) {
        this(config, clock, DEFAULT_THREAD_FACTORY);
    }

    private ErdaMeterRegistry(ErdaConfig config, Clock clock, ThreadFactory threadFactory) {
        super(config, clock);
        this.config = config;
        this.logger = LoggerFactory.getLogger(ErdaMeterRegistry.class);
        this.start(threadFactory);
    }

    public void start(ThreadFactory threadFactory) {
        if (this.config.enabled()) {
            if (this.config.proxyHost() != null) {
                try {
                    this.reporter = new NodeProxyReporter(this.config.proxyHost(), this.config.proxyPort());
                } catch (SocketException e) {
                    this.logger.error("Init NodeProxy Reporter fail", e);
                }
                this.logger.info("Using proxy {}:{} to write metrics", this.config.proxyHost(), this.config.proxyPort());
            }
        }
        super.start(threadFactory);
    }

    @Override
    protected void publish() {
        if (this.config.enabled() && this.reporter != null) {
            for (List<Meter> meters : MeterPartition.partition(this, this.config.batchSize())) {
                this.reporter.send(meters.stream().flatMap(m -> m.match(
                        this::writeGauge,
                        this::writeCounter,
                        this::writeTimer,
                        this::writeSummary,
                        this::writeLongTaskTimer,
                        this::writeTimeGauge,
                        this::writeFunctionCounter,
                        this::writeFunctionTimer,
                        this::writeMeter
                )).toArray(Metric[]::new));
            }
        }
    }

    private Stream<Metric> writeMeter(Meter meter) {
        Metric.FieldBuilder fieldBuilder = Metric.FieldBuilder.newFields();
        for (Measurement measurement : meter.measure()) {
            double value = measurement.getValue();
            if (Double.isFinite(value)) {
                String fieldKey = measurement.getStatistic().getTagValueRepresentation().replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
                fieldBuilder.add(fieldKey, value);
            }
        }
        return Stream.of(this.metricProtocol(meter.getId(), meter.getId().getType().name().toLowerCase(), fieldBuilder));
    }

    private Stream<Metric> writeFunctionTimer(FunctionTimer timer) {
        double sum = timer.totalTime(this.getBaseTimeUnit());
        if (!Double.isFinite(sum)) {
            return Stream.empty();
        }
        Metric.FieldBuilder fieldBuilder = Metric.FieldBuilder.newFields();
        fieldBuilder.add("sum", sum);
        fieldBuilder.add("count", timer.count());
        double mean = timer.mean(this.getBaseTimeUnit());
        if (Double.isFinite(mean)) {
            fieldBuilder.add("mean", mean);
        }
        return Stream.of(this.metricProtocol(timer.getId(), "histogram", fieldBuilder));
    }

    private Stream<Metric> writeFunctionCounter(FunctionCounter counter) {
        double count = counter.count();
        return Double.isFinite(count) ? Stream.of(this.metricProtocol(counter.getId(), "counter", Metric.FieldBuilder.newFields().add("value", count))) : Stream.empty();
    }

    private Stream<Metric> writeTimeGauge(TimeGauge gauge) {
        double count = gauge.value(this.getBaseTimeUnit());
        return Double.isFinite(count) ? Stream.of(this.metricProtocol(gauge.getId(), "gauge", Metric.FieldBuilder.newFields().add("value", count))) : Stream.empty();
    }

    private Stream<Metric> writeLongTaskTimer(LongTaskTimer timer) {
        return Stream.of(this.metricProtocol(timer.getId(), "long_task_timer",
                Metric.FieldBuilder.newFields()
                        .add("active_tasks", timer.activeTasks())
                        .add("duration", timer.duration(this.getBaseTimeUnit()))));
    }

    private Stream<Metric> writeSummary(DistributionSummary summary) {
        return Stream.of(this.metricProtocol(summary.getId(), "histogram",
                Metric.FieldBuilder.newFields()
                        .add("sum", summary.totalAmount())
                        .add("count", summary.count())
                        .add("mean", summary.mean())
                        .add("upper", summary.max())));
    }

    private Stream<Metric> writeTimer(Timer timer) {
        return Stream.of(this.metricProtocol(timer.getId(), "histogram",
                Metric.FieldBuilder.newFields()
                        .add("sum", timer.totalTime(this.getBaseTimeUnit()))
                        .add("count", timer.count())
                        .add("mean", timer.mean(this.getBaseTimeUnit()))
                        .add("upper", timer.max(this.getBaseTimeUnit()))));
    }

    private Stream<Metric> writeCounter(Counter counter) {
        double count = counter.count();
        return Double.isFinite(count) ? Stream.of(this.metricProtocol(counter.getId(), "counter", Metric.FieldBuilder.newFields().add("value", count))) : Stream.empty();
    }

    private Stream<Metric> writeGauge(Gauge gauge) {
        double count = gauge.value();
        return Double.isFinite(count) ? Stream.of(this.metricProtocol(gauge.getId(), "gauge", Metric.FieldBuilder.newFields().add("value", count))) : Stream.empty();
    }

    private Metric metricProtocol(Meter.Id id, String metricType, Metric.FieldBuilder fieldBuilder) {
        Metric metric = new Metric();
        metric.setName(this.getConventionName(id));
        metric.setTimestamp(this.clock.wallTime() * 1000000L);
        Map<String, String> tags = this.getConventionTags(id).stream().filter((t) -> StringUtils.isNotBlank(t.getValue())).collect(Collectors.toMap(Tag::getKey, Tag::getValue));
        tags.put("metric_type", metricType);
        tags.put("_custom", "true");
        tags.put("_meta", "true");
        tags.put("_metric_scope", "micro_service");
        tags.put("_metric_scope_id", this.config.mspEnvId());
        tags.put("_metric_src_instrument", "micrometer");
        tags.put("_metric_src", "integrated_client");
        tags.put("msp_env_id", this.config.mspEnvId());
        tags.put("terminus_key", this.config.mspEnvId());
        tags.put("org_name", this.config.orgName());
        metric.setTags(tags);
        metric.setFields(fieldBuilder.build());
        return metric;
    }

    @Override
    protected TimeUnit getBaseTimeUnit() {
        return TimeUnit.MILLISECONDS;
    }
}
