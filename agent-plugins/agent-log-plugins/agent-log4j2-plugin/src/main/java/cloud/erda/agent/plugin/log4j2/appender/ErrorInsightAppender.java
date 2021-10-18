/*
 * Copyright (c) 2021 Terminus, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.erda.agent.plugin.log4j2.appender;

import org.apache.skywalking.apm.agent.core.boot.ServiceManager;
import cloud.erda.agent.core.metrics.reporter.TelegrafReporter;
import cloud.erda.agent.plugin.log.error.ErrorConsts;
import cloud.erda.agent.plugin.log.error.ErrorEventBuilder;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.impl.ThrowableProxy;

import java.io.Serializable;

/**
 * @author randomnil
 */
public class ErrorInsightAppender extends AbstractAppender {

    public ErrorInsightAppender(String name, Filter filter,
                                Layout<? extends Serializable> layout, boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
    }

    @Override
    public void append(LogEvent event) {
        if (!event.getLevel().isMoreSpecificThan(Level.ERROR)) {
            return;
        }
        ThrowableProxy proxy = event.getThrownProxy();
        if (proxy == null) {
            return;
        }

        ErrorEventBuilder builder = ErrorEventBuilder.newBuilder().addRequestData().
                addTagsFromConfig().addTagsFromMDC(event.getContextData().toMap());
        builder.addMetaData(ErrorConsts.TYPE, proxy.getThrowable().getClass().getName());
        builder.addMetaData(ErrorConsts.EXCEPTION_MESSAGE, proxy.getMessage());
        builder.addMetaData(ErrorConsts.MESSAGE, event.getMessage().getFormattedMessage());

        StackTraceElement[] stacks = proxy.getStackTrace();
        if (stacks != null && stacks.length > 0) {
            StackTraceElement stack = stacks[0];
            builder.addMetaData(ErrorConsts.CLASS, stack.getClassName());
            builder.addMetaData(ErrorConsts.FILE, stack.getFileName());
            builder.addMetaData(ErrorConsts.LINE, String.valueOf(stack.getLineNumber()));
            String method = stack.getMethodName();
            if (method.contains("$")) {
                method = method.split("\\$")[0];
            }
            builder.addMetaData(ErrorConsts.METHOD, method);
            for (int index = 0; index < stacks.length; index++) {
                builder.addStack(stacks[index], index);
            }
        }
        ServiceManager.INSTANCE.findService(TelegrafReporter.class).send(builder.build().toMetric());
    }
}
