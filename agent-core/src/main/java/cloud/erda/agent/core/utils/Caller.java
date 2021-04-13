package cloud.erda.agent.core.utils;

import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;

/**
 * @author: liuhaoyang
 * @create: 2019-11-28 19:17
 **/
public class Caller {
    private static final ILog log = LogManager.getLogger(Caller.class);

    public static void invoke(Action action) {
        try {
            action.invoke();
        } catch (Throwable t) {
            log.error(t, "Caller invoke exception");
        }
    }

    public static interface Action {
        void invoke() throws Exception;
    }
}
