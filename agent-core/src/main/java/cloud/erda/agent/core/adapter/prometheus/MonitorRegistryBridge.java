package cloud.erda.agent.core.adapter.prometheus;

import cloud.erda.agent.core.adapter.prometheus.converters.GaugeConverter;
import cloud.erda.agent.core.adapter.prometheus.converters.HistogramConverter;
import cloud.erda.agent.core.config.AgentConfig;
import cloud.erda.agent.core.config.ServiceConfig;
import cloud.erda.agent.core.metric.Metric;
import cloud.erda.agent.core.utils.DateTimeUtils;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.CollectorRegistry;
import cloud.erda.agent.core.config.loader.ConfigAccessor;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import cloud.erda.agent.core.adapter.prometheus.converters.CounterConverter;
import cloud.erda.agent.core.adapter.prometheus.converters.MeterConverter;
import cloud.erda.agent.core.adapter.prometheus.converters.MetricConverter;
import cloud.erda.agent.core.adapter.prometheus.converters.SummaryConverter;
import org.apache.skywalking.apm.agent.core.boot.ScheduledService;
import org.apache.skywalking.apm.agent.core.boot.ServiceManager;
import cloud.erda.agent.core.reporter.TelegrafReporter;
import java.util.Collections;
import java.util.List;

/**
 * @author liuhaoyang 2020/3/18 20:17
 */
public class MonitorRegistryBridge extends ScheduledService {

    private static final ILog LOG = LogManager.getLogger(MonitorRegistryBridge.class);

    public static final AgentConfig AGENT_CONFIG = ConfigAccessor.Default.getConfig(AgentConfig.class);
    public static final ServiceConfig SERVICE_CONFIG = ConfigAccessor.Default.getConfig(ServiceConfig.class);

    public static final CollectorRegistry MONITOR_REGISTRY = new CollectorRegistry();

    public void push() {
        long timestamp = DateTimeUtils.currentTimeNano();
        for (MetricFamilySamples metricFamilySamples : Collections.list(MONITOR_REGISTRY.metricFamilySamples())) {
            MetricConverter converter = getConverter(metricFamilySamples);
            if (converter != null) {
                List<Metric> metrics = converter.convert(metricFamilySamples, timestamp);
                for (Metric metric : metrics) {
                    attachDefaultTags(metric);
                }
                ServiceManager.INSTANCE.findService(TelegrafReporter.class).send(metrics.toArray(new Metric[0]));
            }
        }
    }

    private MetricConverter getConverter(MetricFamilySamples metricFamilySamples) {
        MetricConverter converter = null;
        switch (metricFamilySamples.type) {
            case COUNTER:
                converter = CounterConverter.instance;
                break;
            case GAUGE:
                converter = GaugeConverter.instance;
                break;
            case SUMMARY:
                converter = SummaryConverter.instance;
                break;
            case HISTOGRAM:
                converter = HistogramConverter.instance;
                break;
            case UNTYPED:
                if (metricFamilySamples.samples.size() > 0) {
                    int index = metricFamilySamples.samples.get(0).labelNames.indexOf("_custom_prom_type");
                    if (index > -1 && "meter".equals(metricFamilySamples.samples.get(0).labelValues.get(index))) {
                        converter = MeterConverter.instance;
                    }
                }
                break;
            default:
                break;
        }
        if (converter == null) {
            LOG.debug("Cannot find metric converter for {} {} {}", metricFamilySamples.name, metricFamilySamples.help, metricFamilySamples.type);
        }
        return converter;
    }

    private void attachDefaultTags(Metric metric) {
        metric.addTag("_custom", String.valueOf(true));
        metric.addTag("_meta", String.valueOf(true));
        metric.addTag("_sdk_version", "1.0.0");
        metric.addTag("_metric_scope", "micro_service");
        metric.addTag("_metric_scope_id", AGENT_CONFIG.terminusKey());
        metric.addTag("project_id", SERVICE_CONFIG.getProjectId());
        metric.addTag("project_name", SERVICE_CONFIG.getProjectName());
        metric.addTag("application_id", SERVICE_CONFIG.getApplicationId());
        metric.addTag("application_name", SERVICE_CONFIG.getApplicationName());
        metric.addTag("service_id", SERVICE_CONFIG.getServiceId());
        metric.addTag("service_name", SERVICE_CONFIG.getServiceName());
        metric.addTag("workspace", SERVICE_CONFIG.getWorkspace());
    }

    @Override protected void executing() {
        this.push();
    }

    @Override protected long initialDelay() {
        return 20;
    }
}
