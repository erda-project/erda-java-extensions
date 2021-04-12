package cloud.erda.agent.core.tracing;

/**
 * @author: liuhaoyang
 * @create: 2019-01-04 17:27
 **/
public class TracerManager {

    private final static ThreadLocal<Tracer> ctx = new SpotThreadLocal();

    public static Tracer tracer() {
        return ctx.get();
    }

    public static class SpotThreadLocal extends ThreadLocal<Tracer> {
        @Override
        protected Tracer initialValue() {
            return new ScopeTracer();
        }
    }
}
