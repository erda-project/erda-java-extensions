package cloud.erda.agent.plugin.cpu;

import java.text.DecimalFormat;

/**
 * @author zhaihongwei
 * @since 2022/2/21
 */
public class CPUInfo {
    private static final DecimalFormat DF = new DecimalFormat("0.00");
    public float user;
    public float nice;
    public float system;
    public float idle;
    public float iowait;
    public float irq;
    public float softirq;
    public float steal;
    public float usage;
    public float total;
    public int physicalCount;

    public CPUInfo() {
    }

    public double getUserUsage() {
        return Double.parseDouble(DF.format(this.user * 1.0 / this.total));
    }

    public double getSystemUsage() {
        return Double.parseDouble(DF.format(this.system * 1.0 / this.total));
    }

    public double getIdleUsage() {
        return Double.parseDouble(DF.format(this.idle * 1.0 / this.total));
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public int getPhysicalCount() {
        return physicalCount;
    }

    public void setPhysicalCount(int physicalCount) {
        this.physicalCount = physicalCount;
    }

    public float getUser() {
        return user;
    }

    public void setUser(float user) {
        this.user = user;
    }

    public float getNice() {
        return nice;
    }

    public void setNice(float nice) {
        this.nice = nice;
    }

    public float getSystem() {
        return system;
    }

    public void setSystem(float system) {
        this.system = system;
    }

    public float getIdle() {
        return idle;
    }

    public void setIdle(float idle) {
        this.idle = idle;
    }

    public float getIowait() {
        return iowait;
    }

    public void setIowait(float iowait) {
        this.iowait = iowait;
    }

    public float getIrq() {
        return irq;
    }

    public void setIrq(float irq) {
        this.irq = irq;
    }

    public float getSoftirq() {
        return softirq;
    }

    public void setSoftirq(float softirq) {
        this.softirq = softirq;
    }

    public float getSteal() {
        return steal;
    }

    public void setSteal(float steal) {
        this.steal = steal;
    }

    public float getUsage() {
        return usage;
    }

    public void setUsage(float usage) {
        this.usage = usage;
    }

    @Override
    public String toString() {
        return "CPU usage: " + getUser() + " user, " + getSystem() + " sys, " + getIdle() + " idle";
    }
}
