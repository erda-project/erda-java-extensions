package cloud.erda.agent.plugin.cpu;

import cloud.erda.agent.core.metrics.Metric;
import cloud.erda.agent.core.metrics.MetricDispatcher;
import org.apache.skywalking.apm.agent.core.boot.ServiceManager;


/**
 * @author zhaihongwei
 * @since 2022/2/21
 */
public class CPUStatCollector {
    private final CPUUsageProvider cpuUsageProvider;

    public CPUStatCollector() {
        this.cpuUsageProvider = new CPUUsageProvider();
    }

    public void collect() {
        Metric metric = cpuUsageProvider.get();
        ServiceManager.INSTANCE.findService(MetricDispatcher.class).dispatch(metric);
    }
}
