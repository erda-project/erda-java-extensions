package io.terminus.spot.plugin.httpasyncclient.v4;

import cloud.erda.agent.core.tracing.TracerSnapshot;
import org.apache.http.protocol.HttpContext;

/**
 * @author randomnil
 */
public class ThreadTransferInfo {

    public static ThreadLocal<ThreadTransferInfo> LOCAL = new ThreadLocal<ThreadTransferInfo>();

    private TracerSnapshot snapshot;
    private HttpContext httpContext;

    public ThreadTransferInfo(TracerSnapshot snapshot, HttpContext httpContext) {
        this.snapshot = snapshot;
        this.httpContext = httpContext;
    }

    public TracerSnapshot getSnapshot() {
        return snapshot;
    }

    public HttpContext getHttpContext() {
        return httpContext;
    }
}
