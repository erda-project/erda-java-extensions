package cloud.erda.agent.core.tracing;

import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import cloud.erda.agent.core.tracing.span.Span;

/**
 * @author: liuhaoyang
 * @create: 2019-01-04 17:37
 **/
public class Scope  {

    private static ILog log = LogManager.getLogger(Scope.class);

    private ScopeTracer tracer;
    private Scope last;
    private Span active;

    public Scope(ScopeTracer tracer, Span span) {
        this.tracer = tracer;
        this.last = tracer.active();
        this.active = span;
        this.tracer.activate(this);
    }

    public Span span() {
        return active;
    }

    /**
     * 跨线程传递时，需要显示调用close(false) 只释放context 不结束span。把span activate到目标进程的tracer中
     **/
    public void close(boolean finish) {
        if (tracer.active() != this) {
            return;
        }

        //span finish
        if (finish) {
            active.finish();
        }

        if (last == null) {
            tracer.context().close();
        }
        tracer.activate(last);
    }

    public void close() {
        close(true);
    }
}
