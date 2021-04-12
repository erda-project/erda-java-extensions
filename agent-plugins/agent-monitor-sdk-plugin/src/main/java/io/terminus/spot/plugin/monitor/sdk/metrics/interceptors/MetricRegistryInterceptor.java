package io.terminus.spot.plugin.monitor.sdk.metrics.interceptors;

import io.terminus.dice.monitor.sdk.metrics.*;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import cloud.erda.agent.core.utils.Collections;
import org.apache.skywalking.apm.agent.core.util.Strings;
import cloud.erda.agent.core.adapter.prometheus.MonitorRegistryBridge;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import io.terminus.spot.plugin.monitor.sdk.metrics.*;
import io.terminus.spot.plugin.monitor.sdk.metrics.wrappers.*;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.StaticMethodsAroundInterceptor;

import java.util.Map;

/**
 * @author liuhaoyang 2020/3/21 21:32
 */
public class MetricRegistryInterceptor implements StaticMethodsAroundInterceptor {

    private static final ILog log = LogManager.getLogger(MetricRegistryInterceptor.class);

    private Counter registerCounter(CounterBuilder counterBuilder) {
        AutoResetCounter.Builder prometheusCounterBuilder = AutoResetCounter.build(counterBuilder.getMetric(),
            Strings.isEmpty(counterBuilder.getDescription()) ? "Counter metric." : counterBuilder.getDescription());

        if (!Collections.IsNullOrEmpty(counterBuilder.getLabels())) {
            prometheusCounterBuilder.labelNames(processLabelNames((counterBuilder.getLabels())));
        }
        if (!Strings.isEmpty(counterBuilder.getNamespace())) {
            prometheusCounterBuilder.namespace(counterBuilder.getNamespace());
        }
        if (!Strings.isEmpty(counterBuilder.getSubsystem())) {
            prometheusCounterBuilder.subsystem(counterBuilder.getSubsystem());
        }
        return new CounterWrapper(prometheusCounterBuilder.register(MonitorRegistryBridge.MONITOR_REGISTRY));
    }

    private Gauge registerGauge(GaugeBuilder gaugeBuilder) {
        io.prometheus.client.Gauge.Builder prometheusGaugeBuilder = io.prometheus.client.Gauge.build().name(gaugeBuilder.getMetric());

        prometheusGaugeBuilder.help(Strings.isEmpty(gaugeBuilder.getDescription()) ? "Gauge metric." : gaugeBuilder.getDescription());

        if (!Collections.IsNullOrEmpty(gaugeBuilder.getLabels())) {
            prometheusGaugeBuilder.labelNames(processLabelNames(gaugeBuilder.getLabels()));
        }
        if (!Strings.isEmpty(gaugeBuilder.getNamespace())) {
            prometheusGaugeBuilder.namespace(gaugeBuilder.getNamespace());
        }
        if (!Strings.isEmpty(gaugeBuilder.getSubsystem())) {
            prometheusGaugeBuilder.subsystem(gaugeBuilder.getSubsystem());
        }
        return new GaugeWrapper(prometheusGaugeBuilder.register(MonitorRegistryBridge.MONITOR_REGISTRY));
    }

    private Meter registerMeter(MeterBuilder meterBuilder) {
        ExponentialMovingAveragesMeter.Builder prometheusMeterBuilder = ExponentialMovingAveragesMeter.build(meterBuilder.getMetric(),
            Strings.isEmpty(meterBuilder.getDescription()) ? "Meter metric." : meterBuilder.getDescription());

        if (!Collections.IsNullOrEmpty(meterBuilder.getLabels())) {
            prometheusMeterBuilder.labelNames(processLabelNames(meterBuilder.getLabels()));
        }
        if (!Strings.isEmpty(meterBuilder.getNamespace())) {
            prometheusMeterBuilder.namespace(meterBuilder.getNamespace());
        }
        if (!Strings.isEmpty(meterBuilder.getSubsystem())) {
            prometheusMeterBuilder.subsystem(meterBuilder.getSubsystem());
        }

        return new MeterWrapper(prometheusMeterBuilder.register(MonitorRegistryBridge.MONITOR_REGISTRY));
    }

    private Histogram registerHistogram(HistogramBuilder histogramBuilder) {
        io.prometheus.client.Histogram.Builder prometheusHistogramBuilder = io.prometheus.client.Histogram.build().name(histogramBuilder.getMetric());

        prometheusHistogramBuilder.help(Strings.isEmpty(histogramBuilder.getDescription()) ? "Histogram metric." : histogramBuilder.getDescription());

        if (!Collections.IsNullOrEmpty(histogramBuilder.getLabels())) {
            prometheusHistogramBuilder.labelNames(processLabelNames(histogramBuilder.getLabels()));
        }
        if (!Strings.isEmpty(histogramBuilder.getNamespace())) {
            prometheusHistogramBuilder.namespace(histogramBuilder.getNamespace());
        }
        if (!Strings.isEmpty(histogramBuilder.getSubsystem())) {
            prometheusHistogramBuilder.subsystem(histogramBuilder.getSubsystem());
        }

        if (!Collections.IsNullOrEmpty(histogramBuilder.getBuckets())) {
            prometheusHistogramBuilder.buckets(histogramBuilder.getBuckets());
        }

        return new HistogramWrapper(prometheusHistogramBuilder.register(MonitorRegistryBridge.MONITOR_REGISTRY));
    }

    private Summary registerSummary(SummaryBuilder summaryBuilder) {
        io.prometheus.client.Summary.Builder prometheusGaugeBuilder = io.prometheus.client.Summary.build().name(summaryBuilder.getMetric());

        prometheusGaugeBuilder.help(Strings.isEmpty(summaryBuilder.getDescription()) ? "Gauge metric." : summaryBuilder.getDescription());

        if (!Collections.IsNullOrEmpty(summaryBuilder.getLabels())) {
            prometheusGaugeBuilder.labelNames(processLabelNames(summaryBuilder.getLabels()));
        }
        if (!Strings.isEmpty(summaryBuilder.getNamespace())) {
            prometheusGaugeBuilder.namespace(summaryBuilder.getNamespace());
        }
        if (!Strings.isEmpty(summaryBuilder.getSubsystem())) {
            prometheusGaugeBuilder.subsystem(summaryBuilder.getSubsystem());
        }

        for (Map.Entry<Double, Double> item : summaryBuilder.getQuantilesSet()) {
            prometheusGaugeBuilder.quantile(item.getKey(), item.getValue());
        }

        return new SummaryWrapper(prometheusGaugeBuilder.register(MonitorRegistryBridge.MONITOR_REGISTRY));
    }

    private String[] processLabelNames(String[] labels) {
        for (int i = 0; i < labels.length; i++) {
            String label = labels[i];
            if (!Strings.isEmpty(label)) {
                labels[i] = label.replace('-', '_').replace('.', '_');
            }
        }
        return labels;
    }

    @Override public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) {
        try {
            Object[] allArguments = context.getArguments();
            for (Object obj : allArguments) {
                log.info("{}", obj);
            }
            String methodName = context.getMethod().getName();
            if (allArguments.length < 1 || allArguments[0] == null) {
                log.warn("Metric build is null, return noop metric for {}", methodName);
                return;
            }
            Object metric = null;
            switch (methodName) {
                case "registerCounter":
                    metric = registerCounter((CounterBuilder)allArguments[0]);
                    break;
                case "registerGauge":
                    metric = registerGauge((GaugeBuilder)allArguments[0]);
                    break;
                case "registerMeter":
                    metric = registerMeter((MeterBuilder)allArguments[0]);
                    break;
                case "registerHistogram":
                    metric = registerHistogram((HistogramBuilder)allArguments[0]);
                    break;
                case "registerSummary":
                    metric = registerSummary((SummaryBuilder)allArguments[0]);
                    break;
                default:
                    break;
            }
            if (result != null) {
                result.defineReturnValue(metric);
            }
        } catch (Throwable throwable) {
            log.error(throwable, "Call {} fail", context.getMethod().getName());
            context.forceThrowException(throwable);
        }
    }

    @Override public Object afterMethod(IMethodInterceptContext context, Object ret) {
        return ret;
    }

    @Override public void handleMethodException(IMethodInterceptContext context, Throwable t) throws Throwable {
    }
}
