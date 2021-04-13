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

package cloud.erda.agent.plugin.logback.appender;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import cloud.erda.agent.core.config.loader.ConfigAccessor;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import cloud.erda.agent.plugin.log.config.LogConfig;
import cloud.erda.agent.plugin.log.error.ErrorConsts;
import cloud.erda.agent.plugin.logback.error.ErrorInsightAppender;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AppenderProcessor {

    private final static ILog log = LogManager.getLogger(AppenderProcessor.class);
    private final static ConfigAccessor logConfigAccessor = new ConfigAccessor(LogConfig.class.getClassLoader());

    public void replace(LoggerContext loggerContext) {
        Logger logger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        List<Appender<ILoggingEvent>> appenders = new ArrayList<Appender<ILoggingEvent>>();
        Iterator<Appender<ILoggingEvent>> appenderIterator = logger.iteratorForAppenders();
        while (appenderIterator.hasNext()) {
            Appender<ILoggingEvent> appender = appenderIterator.next();
            appenders.add(appender);
        }
        LogConfig logConfig = logConfigAccessor.getConfig(LogConfig.class);
        for (Appender<ILoggingEvent> appender : appenders) {
            log.info("Find [{}] appender {}.", appender.getName(), appender.getClass().getName());
            if (appender instanceof ConsoleAppender<?>) {
                continue;
            }
            if (appender instanceof ErrorInsightAppender) {
                continue;
            }
            if (logConfig.getForceStdout()) {
                appender.stop();
                logger.detachAppender(appender.getName());
                log.info("Detach [{}] appender {}.", appender.getName(), appender.getClass().getName());
            }
        }
        // add error appender
        Appender<ILoggingEvent> errorInsightAppender = logger.getAppender(ErrorConsts.ERROR_INSIGHT);
        if (errorInsightAppender == null) {
            errorInsightAppender = new ErrorInsightAppender();
            errorInsightAppender.setName(ErrorConsts.ERROR_INSIGHT);
            errorInsightAppender.setContext(loggerContext);
            errorInsightAppender.start();
            logger.addAppender(errorInsightAppender);
            log.info("Add [{}] appender {}.", errorInsightAppender.getName(), errorInsightAppender.getClass().getName());
        }

        // add console appender
//        Appender<ILoggingEvent> consoleAppender = logger.getAppender("console");
//        if (consoleAppender == null) {
//            consoleAppender = new ConsoleAppender<ILoggingEvent>();
//            consoleAppender.setName("console");
//            consoleAppender.setContext(loggerContext);
//            consoleAppender.start();
//            logger.addAppender(consoleAppender);
//            log.info("add console log appender.");
//        }
    }
}
