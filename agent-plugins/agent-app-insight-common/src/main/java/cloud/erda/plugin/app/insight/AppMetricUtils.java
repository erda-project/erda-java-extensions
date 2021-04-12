package cloud.erda.plugin.app.insight;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import cloud.erda.agent.core.utils.Constants;

/**
 * @author randomnil
 */
public class AppMetricUtils {

    public static AppMetricBuilder createHttpMetric(String host) {
        return new AppMetricBuilder(Constants.Metrics.APPLICATION_HTTP, false)
            .tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_HTTP)
            .tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_CLIENT)
            .tag(Constants.Tags.HOST, host);
    }

    public static AppMetricBuilder createMiroServiceMetric(String addonType) {
        return new AppMetricBuilder(Constants.Metrics.APPLICATION_MICRO_SERVICE, false)
            .tag(Constants.Metrics.TARGET_ADDON_TYPE, addonType)
            .tag(Constants.Metrics.TARGET_ADDON_ID, addonType);
    }

    public static void handleStatusCode(AppMetricBuilder appMetricBuilder, int statusCode) {
        if (appMetricBuilder == null) {
            return;
        }

        if (statusCode >= 500) {
            handleException(appMetricBuilder);
        }
        appMetricBuilder.field(Constants.Tags.HTTP_STATUS, statusCode);
        appMetricBuilder.tag(Constants.Tags.HTTP_STATUS, String.valueOf(statusCode));
    }

    public static void handleException(IMethodInterceptContext context) {
        AppMetricBuilder appMetricBuilder = context.getAttachment(Constants.Keys.METRIC_BUILDER);
        handleException(appMetricBuilder);
    }

    public static void handleException(AppMetricBuilder appMetricBuilder) {
        if (appMetricBuilder == null) {
            return;
        }
        appMetricBuilder.tag(Constants.Tags.ERROR, Constants.Tags.ERROR_TRUE);
    }
}
