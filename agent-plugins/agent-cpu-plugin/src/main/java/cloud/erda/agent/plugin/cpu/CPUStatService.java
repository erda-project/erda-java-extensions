package cloud.erda.agent.plugin.cpu;

import cloud.erda.agent.core.utils.PluginConstants;
import org.apache.skywalking.apm.agent.core.boot.ScheduledService;

/**
 * @author zhaihongwei
 * @since 2022/2/21
 */
public class CPUStatService extends ScheduledService {

    private final CPUStatCollector collector = new CPUStatCollector();

    @Override
    public String pluginName() {
        return PluginConstants.CPU_PLUGIN;
    }

    @Override
    protected void executing() {
        collector.collect();
    }

    @Override
    protected long initialDelay() {
        return 20;
    }
}
