package cloud.erda.agent.core.utils;

import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.Scope;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.span.Span;

import static cloud.erda.agent.core.utils.Constants.Tags.*;

/**
 * @author: liuhaoyang
 * @create: 2019-01-11 17:48
 **/
public class TracerUtils {

    public static void handleStatusCode(Scope scope, int statusCode) {
        if (scope == null) {
            return;
        }

        Span span = scope.span();
        if (statusCode >= 400) {
            span.tag(ERROR, ERROR_TRUE);
        }
        span.tag(HTTP_STATUS, String.valueOf(statusCode));
    }

    public static void handleException(Throwable throwable) {
        Tracer tracer = TracerManager.tracer();
        Scope scope = tracer.active();
        handleException(scope, throwable);
    }

    public static void handleException(Scope scope, Throwable throwable) {
        if (scope != null) {
            Span span = scope.span();
            span.tag(ERROR, ERROR_TRUE);
            span.tag(ERROR_MESSAGE, throwable.getMessage());
        }
    }
}
