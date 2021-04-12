package cloud.erda.agent.core.tracing;

import cloud.erda.agent.core.config.AgentConfig;
import org.apache.skywalking.apm.agent.core.boot.ScheduledService;
import cloud.erda.agent.core.config.loader.ConfigAccessor;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: liuhaoyang
 * @create: 2019-01-07 16:29
 **/
public class Sampler extends ScheduledService {

    private final Random random = new Random();
    private final int rate = ConfigAccessor.Default.getConfig(AgentConfig.class).samplingRate();
    private final int limit = ConfigAccessor.Default.getConfig(AgentConfig.class).samplingLimit();
    private final AtomicInteger index = new AtomicInteger(0);

    public boolean shouldSample() {
        if (index.get() > limit) {
            return false;
        }
        if (rate < 0 || rate > 100) {
            return false;
        }
        int v = random.nextInt(100);
        if (v > rate) {
            return false;
        }
        index.getAndIncrement();
        return true;
    }

    @Override
    protected void executing() {
        do {
            int current = index.get();
            if (index.compareAndSet(current, 0)) {
                break;
            }
        } while (true);
    }

    @Override
    protected long period() {
        return 60;
    }
}
