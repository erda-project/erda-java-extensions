/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


package org.apache.skywalking.apm.agent.core.logging.core;

import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.core.Constants;
import org.apache.skywalking.apm.agent.core.util.ReflectionUtils;
import org.apache.skywalking.apm.agent.core.util.Strings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;

public class EasyLogger implements ILog {

    private static LogLevel logLevel;

    static {
        String level = System.getenv("TERMINUS_AGENT_LOGLEVEL");
        if (level == null || level.length() <= 0) {
            level = System.getProperty("TERMINUS_AGENT_LOGLEVEL");
            if (level == null || level.length() <= 0) {
                level = "ERROR";
            }
        }
        try {
            logLevel = (LogLevel) ReflectionUtils.castValue(level, LogLevel.class);
        } catch (Throwable throwable) {
            logLevel = LogLevel.OFF;
        }
    }

    private Class targetClass;

    public EasyLogger(Class targetClass) {
        this.targetClass = targetClass;
    }

    protected void logger(LogLevel level, String message, Throwable e) {
        WriterFactory.getLogWriter().write(format(level, message, e));
    }

    private String replaceParam(String message, Object... parameters) {
        int startSize = 0;
        int parametersIndex = 0;
        int index;
        String tmpMessage = message;
        while ((index = message.indexOf("{}", startSize)) != -1) {
            if (parametersIndex >= parameters.length) {
                break;
            }
            /**
             * @Fix the Illegal group reference issue
             */
            tmpMessage = tmpMessage.replaceFirst("\\{\\}", Matcher.quoteReplacement(String.valueOf(parameters[parametersIndex++])));
            startSize = index + 2;
        }
        return tmpMessage;
    }

    String format(LogLevel level, String message, Throwable t) {
        return Strings.join(' ', level.name(),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                targetClass.getSimpleName(),
                ": ",
                message,
                t == null ? "" : format(t)
        );
    }

    String format(Throwable t) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        t.printStackTrace(new java.io.PrintWriter(buf, true));
        String expMessage = buf.toString();
        try {
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Constants.LINE_SEPARATOR + expMessage;
    }

    @Override
    public void info(String format) {
        if (isInfoEnable())
            logger(LogLevel.INFO, format, null);
    }

    @Override
    public void info(String format, Object... arguments) {
        if (isInfoEnable())
            logger(LogLevel.INFO, replaceParam(format, arguments), null);
    }

    @Override
    public void warn(String format, Object... arguments) {
        if (isWarnEnable())
            logger(LogLevel.WARN, replaceParam(format, arguments), null);
    }

    @Override
    public void warn(Throwable e, String format, Object... arguments) {
        if (isWarnEnable())
            logger(LogLevel.WARN, replaceParam(format, arguments), e);
    }

    @Override
    public void error(String format, Throwable e) {
        if (isErrorEnable())
            logger(LogLevel.ERROR, format, e);
    }

    @Override
    public void error(Throwable e, String format, Object... arguments) {
        if (isErrorEnable())
            logger(LogLevel.ERROR, replaceParam(format, arguments), e);
    }

    @Override
    public boolean isDebugEnable() {
        return LogLevel.DEBUG.compareTo(logLevel) >= 0;
    }

    @Override
    public boolean isInfoEnable() {
        return LogLevel.INFO.compareTo(logLevel) >= 0;
    }

    @Override
    public boolean isWarnEnable() {
        return LogLevel.WARN.compareTo(logLevel) >= 0;
    }

    @Override
    public boolean isErrorEnable() {
        return LogLevel.ERROR.compareTo(logLevel) >= 0;
    }

    @Override
    public void debug(String format) {
        if (isDebugEnable()) {
            logger(LogLevel.DEBUG, format, null);
        }
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (isDebugEnable()) {
            logger(LogLevel.DEBUG, replaceParam(format, arguments), null);
        }
    }

    @Override
    public void error(String format) {
        if (isErrorEnable()) {
            logger(LogLevel.ERROR, format, null);
        }
    }
}
