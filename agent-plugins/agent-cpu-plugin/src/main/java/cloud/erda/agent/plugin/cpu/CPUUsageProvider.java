package cloud.erda.agent.plugin.cpu;

import cloud.erda.agent.core.metrics.Metric;
import cloud.erda.agent.core.utils.DateTime;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.util.Util;

import java.text.DecimalFormat;

/**
 * @author zhaihongwei
 * @since 2022/2/21
 */
public class CPUUsageProvider implements CPUDefaultProvider {


    private static final String METRIC_NAME = "cpu_usage";
    private static final SystemInfo SI = new SystemInfo();
    private static final HardwareAbstractionLayer HAL = SI.getHardware();

    public Metric get() {
        CPUInfo cpuInfo = getCpuUsage(HAL.getProcessor());
        Metric metric = Metric.New(METRIC_NAME, DateTime.currentTimeNano()).
                addField("user", cpuInfo.user).
                addField("nice", cpuInfo.nice).
                addField("system", cpuInfo.system).
                addField("idle", cpuInfo.idle).
                addField("iowait", cpuInfo.iowait).
                addField("irq", cpuInfo.irq).
                addField("softirq", cpuInfo.softirq).
                addField("steal", cpuInfo.steal).
                addField("usage", cpuInfo.usage).
                addField("total", cpuInfo.total).
                addField("user_usage", cpuInfo.getUserUsage()).
                addField("system_usage", cpuInfo.getSystemUsage()).
                addField("idle_usage", cpuInfo.getIdleUsage()).
                addField("physicalCount", cpuInfo.physicalCount);
        this.addDefaultTags(metric);
        return metric;
    }

    /**
     * Get Cpu Usage information
     *
     * @param processor CentralProcessor
     * @return CPUUsage
     */
    private static CPUInfo getCpuUsage(CentralProcessor processor) {
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(1000);
        int physicalProcessorCount = processor.getPhysicalProcessorCount();
        long[] ticks = processor.getSystemCpuLoadTicks();
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long system = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long totalCpu = user + nice + system + idle + iowait + irq + softirq + steal;

        CPUInfo cpuInfo = new CPUInfo();
        cpuInfo.setUser(user);
        cpuInfo.setNice(nice);
        cpuInfo.setSystem(system);
        cpuInfo.setIdle(idle);
        cpuInfo.setIowait(iowait);
        cpuInfo.setIrq(irq);
        cpuInfo.setSoftirq(softirq);
        cpuInfo.setSteal(steal);
        cpuInfo.setPhysicalCount(physicalProcessorCount);
        cpuInfo.setTotal(totalCpu);
        return cpuInfo;
    }
}
