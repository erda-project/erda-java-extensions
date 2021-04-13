package cloud.erda.plugin.app.insight;

import org.apache.skywalking.apm.agent.core.boot.ServiceManager;
import cloud.erda.agent.core.reporter.TelegrafReporter;

/**
 * @author: liuhaoyang
 * @create: 2019-01-21 18:12
 **/
public class AppMetricRecorder {

    private static final TelegrafReporter transporter = ServiceManager.INSTANCE.findService(TelegrafReporter.class);

    public static void record(AppMetricBuilder builder) {
        transporter.send(builder.build());
    }
}
