package org.apache.skywalking.apm.agent.core.util;

public class RunnableWithExceptionProtection implements Runnable {
    private Runnable run;
    private CallbackWhenException callback;

    public RunnableWithExceptionProtection(Runnable run, CallbackWhenException callback) {
        this.run = run;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            run.run();
        } catch (Throwable t) {
            callback.handle(t);
        }
    }

    public interface CallbackWhenException {
        void handle(Throwable t);
    }
}
