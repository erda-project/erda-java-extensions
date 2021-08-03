package cloud.erda.agent.plugin.rocketmq.v4;

import cloud.erda.agent.core.tracing.TracerSnapshot;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricBuilder;

/**
 * @author randomnil
 */
public class MessageSendAsyncInfo {
    private TracerSnapshot tracerSnapshot;
    private TransactionMetricBuilder transactionMetricBuilder;

    public MessageSendAsyncInfo(TracerSnapshot tracerSnapshot, TransactionMetricBuilder transactionMetricBuilder) {
        this.tracerSnapshot = tracerSnapshot;
        this.transactionMetricBuilder = transactionMetricBuilder;
    }

    public TracerSnapshot getTracerSnapshot() {
        return tracerSnapshot;
    }

    public TransactionMetricBuilder getAppMetricBuilder() {
        return transactionMetricBuilder;
    }
}
