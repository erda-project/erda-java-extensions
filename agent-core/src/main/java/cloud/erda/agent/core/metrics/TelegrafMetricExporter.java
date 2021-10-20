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

package cloud.erda.agent.core.metrics;

import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.Numbers;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.data.*;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author liuhaoyang
 * @date 2021/10/12 14:00
 */
public class TelegrafMetricExporter implements MetricExporter {

    private final MetricDispatcher reporter;
    private final ILog logger;

    public TelegrafMetricExporter(MetricDispatcher reporter) {
        this.reporter = reporter;
        this.logger = LogManager.getLogger(TelegrafMetricExporter.class);
    }

    @Override
    public CompletableResultCode export(Collection<MetricData> collection) {

        Metric[] metrics = collection.stream().flatMap(this::toMetrics).toArray(Metric[]::new);
        logger.info("[{}] execute export {} metrics.", this.getClass().getClassLoader(), metrics.length);
        return new CompletableResultCode().whenComplete(() -> reporter.dispatch(metrics)).succeed();
    }

    private Stream<Metric> toMetrics(MetricData metricData) {
        switch (metricData.getType()) {
            case LONG_GAUGE:
                return metricData.getLongGaugeData().getPoints().stream().flatMap(point -> toValueMetric(metricData, point));
            case DOUBLE_GAUGE:
                return metricData.getDoubleGaugeData().getPoints().stream().flatMap(point -> toValueMetric(metricData, point));
            case LONG_SUM:
                return metricData.getLongSumData().getPoints().stream().flatMap(point -> toValueMetric(metricData, point));
            case DOUBLE_SUM:
                return metricData.getDoubleSumData().getPoints().stream().flatMap(point -> toValueMetric(metricData, point));
            case SUMMARY:
                return metricData.getDoubleSummaryData().getPoints().stream().flatMap(point -> toSummaryMetric(metricData, point));
            case HISTOGRAM:
                return metricData.getDoubleHistogramData().getPoints().stream().flatMap(point -> toHistogramMetric(metricData, point));
        }
        return Stream.empty();
    }

    private Stream<Metric> toHistogramMetric(MetricData metricData, DoubleHistogramPointData pointData) {
        Metric.FieldBuilder fieldBuilder = Metric.FieldBuilder.newFields().add(getFieldKey(pointData.getAttributes(), "sum"), pointData.getSum()).add(getFieldKey(pointData.getAttributes(), "count"), pointData.getCount());
        long cumulativeCount = 0;
        List<Long> counts = pointData.getCounts();
        for (int i = 0; i < counts.size(); i++) {
            double boundary = pointData.getBucketUpperBound(i);
            cumulativeCount += counts.get(i);
            fieldBuilder.add(getFieldKey(pointData.getAttributes(), "legend_") + Numbers.doubleToGoString(boundary), cumulativeCount);
        }
        return Stream.of(createMetric(metricData.getName(), pointData.getEpochNanos(), mergeTag(metricData, pointData.getAttributes()), fieldBuilder));
    }

    private Stream<Metric> toSummaryMetric(MetricData metricData, DoubleSummaryPointData pointData) {
        Metric.FieldBuilder fieldBuilder = Metric.FieldBuilder.newFields().add(getFieldKey(pointData.getAttributes(), "sum"), pointData.getSum()).add(getFieldKey(pointData.getAttributes(), "count"), pointData.getCount());
        pointData.getPercentileValues().forEach(v -> fieldBuilder.add(getFieldKey(pointData.getAttributes(), "quantile_") + Numbers.doubleToGoString(v.getPercentile()), v.getValue()));
        return Stream.of(createMetric(metricData.getName(), pointData.getEpochNanos(), mergeTag(metricData, pointData.getAttributes()), fieldBuilder));
    }

    private Stream<Metric> toValueMetric(MetricData metricData, LongPointData pointData) {
        return Stream.of(createMetric(metricData.getName(), pointData.getEpochNanos(), mergeTag(metricData, pointData.getAttributes()),
                Metric.FieldBuilder.newFields().add(getFieldKey(pointData.getAttributes(), "value"), pointData.getValue())));
    }

    private Stream<Metric> toValueMetric(MetricData metricData, DoublePointData pointData) {
        return Stream.of(createMetric(metricData.getName(), pointData.getEpochNanos(), mergeTag(metricData, pointData.getAttributes()),
                Metric.FieldBuilder.newFields().add(getFieldKey(pointData.getAttributes(), "value"), pointData.getValue())));
    }

    private Metric.TagBuilder mergeTag(MetricData metricData, Attributes attributes) {
        Metric.TagBuilder tagBuilder = Metric.TagBuilder.newTags();
        attributes.forEach((key, value) -> tagBuilder.add(key.getKey(), value == null ? "" : value.toString()));
        metricData.getResource().getAttributes().forEach((key, value) -> tagBuilder.add(key.getKey(), value == null ? "" : value.toString()));
        tagBuilder.add(Constants.Metrics.INSTRUMENTATION_LIBRARY, metricData.getInstrumentationLibraryInfo().getName());
        tagBuilder.add(Constants.Metrics.INSTRUMENTATION_LIBRARY_VERSION, metricData.getInstrumentationLibraryInfo().getVersion());
        return tagBuilder;
    }

    private Metric createMetric(String name, Long timestamp, Metric.TagBuilder tagBuilder, Metric.FieldBuilder fieldBuilder) {
        String indexName = tagBuilder.getOrDefault("_metric_index", null);
        return Metric.New(indexName != null ? indexName : name, timestamp)
                .addTags(tagBuilder).addFields(fieldBuilder);
    }

    private String getFieldKey(Attributes attributes, String defaultValue) {
        return getFieldKey(attributes, "", defaultValue);
    }

    private String getFieldKey(Attributes attributes, String suffix, String defaultValue) {
        String fieldKey = attributes.get(AttributeKey.stringKey(Constants.Metrics.FIELD_KEY));
        return fieldKey == null ? defaultValue : fieldKey + suffix;
    }

    @Override
    public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
    }
}
