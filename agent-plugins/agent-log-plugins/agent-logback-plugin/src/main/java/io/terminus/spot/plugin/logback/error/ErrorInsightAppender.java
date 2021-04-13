package io.terminus.spot.plugin.logback.error;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;
import org.apache.skywalking.apm.agent.core.boot.ServiceManager;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import cloud.erda.agent.core.reporter.TelegrafReporter;
import io.terminus.spot.plugin.log.error.ErrorConsts;
import io.terminus.spot.plugin.log.error.ErrorEventBuilder;

public class ErrorInsightAppender extends AppenderBase<ILoggingEvent> {

    private final static ILog log = LogManager.getLogger(ErrorInsightAppender.class);

    @Override
    protected void append(ILoggingEvent loggingEvent) {
        if (loggingEvent.getLevel().isGreaterOrEqual(Level.ERROR)) {
            appendErrorEvent(loggingEvent);
        }
    }

    public void appendErrorEvent(ILoggingEvent loggingEvent) {
        IThrowableProxy throwableProxy = loggingEvent.getThrowableProxy();
        if (throwableProxy != null) {
            ErrorEventBuilder builder = ErrorEventBuilder.newBuilder().addRequestData().
                    addTagsFromConfig().addTagsFromMDC(loggingEvent.getMDCPropertyMap());
            builder.addMetaData(ErrorConsts.TYPE, loggingEvent.getThrowableProxy().getClassName());
            builder.addMetaData(ErrorConsts.EXCEPTION_MESSAGE, loggingEvent.getThrowableProxy().getMessage());
            builder.addMetaData(ErrorConsts.MESSAGE, loggingEvent.getFormattedMessage());

            StackTraceElementProxy[] stackTraceElementProxies = throwableProxy.getStackTraceElementProxyArray();
            if (stackTraceElementProxies != null && stackTraceElementProxies.length > 0) {
                StackTraceElementProxy stackTrace = stackTraceElementProxies[0];
                builder.addMetaData(ErrorConsts.CLASS, stackTrace.getStackTraceElement().getClassName());
                builder.addMetaData(ErrorConsts.FILE, stackTrace.getStackTraceElement().getFileName());
                builder.addMetaData(ErrorConsts.LINE, String.valueOf(stackTrace.getStackTraceElement().getLineNumber()));
                String method = stackTrace.getStackTraceElement().getMethodName();
                if (method.contains("$")) {
                    method = method.split("\\$")[0];
                }
                builder.addMetaData(ErrorConsts.METHOD, method);
                for (int index = 0; index < stackTraceElementProxies.length; index++) {
                    builder.addStack(stackTraceElementProxies[index].getStackTraceElement(), index);
                }
            }
            ServiceManager.INSTANCE.findService(TelegrafReporter.class).send(builder.build().toMetric());
        }
    }
}
