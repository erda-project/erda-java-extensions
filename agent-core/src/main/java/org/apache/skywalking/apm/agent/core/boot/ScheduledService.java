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

package org.apache.skywalking.apm.agent.core.boot;

import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.util.RunnableWithExceptionProtection;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class ScheduledService implements BootService {

    protected final ILog log = LogManager.getLogger(this.getClass());
    private volatile ScheduledFuture<?> scheduledFuture;

    @Override
    public void beforeBoot() throws Throwable {
    }

    @Override
    public void boot() throws Throwable {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }

        ScheduledExecutorService service = Executors
                .newSingleThreadScheduledExecutor(new DefaultNamedThreadFactory(serviceName()));
        scheduledFuture = service.scheduleAtFixedRate(new RunnableWithExceptionProtection(new Runnable() {
            @Override
            public void run() {
                ScheduledService.this.executing();
            }
        }, new RunnableWithExceptionProtection.CallbackWhenException() {
            @Override
            public void handle(Throwable t) {
                log.error("scheduled service execute error.", t);
            }
        }), initialDelay(), period(), timeUnit());
    }

    @Override
    public void afterBoot() throws Throwable {
    }

    @Override
    public void shutdown() throws Throwable {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }

    protected abstract void executing();

    protected String serviceName(){
        return this.getClass().getName();
    }

    protected TimeUnit timeUnit(){
        return TimeUnit.SECONDS;
    }

    protected long initialDelay(){
        return 0;
    }

    protected long period(){
        return 30;
    }
}
