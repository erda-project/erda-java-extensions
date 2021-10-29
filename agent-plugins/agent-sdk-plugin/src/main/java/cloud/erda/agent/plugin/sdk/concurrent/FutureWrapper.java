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

package cloud.erda.agent.plugin.sdk.concurrent;

import cloud.erda.agent.core.tracing.Scope;
import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.TracerSnapshot;
import cloud.erda.agent.core.utils.Constants;

import java.util.concurrent.*;

/**
 * @author liuhaoyang
 * @date 2021/10/27 12:18
 */
public class FutureWrapper<T> implements Future<T> {
    private final Future<T> future;
    private final TracerSnapshot tracerSnapshot;

    public FutureWrapper(Future future, TracerSnapshot tracerSnapshot) {
        this.future = future;
        this.tracerSnapshot = tracerSnapshot;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        Scope scope = TracerManager.tracer().attach(tracerSnapshot);
        scope.span().updateName("Future Cancel");
        try {
            return future.cancel(mayInterruptIfRunning);
        } catch (Exception exception) {
            scope.span().tag(Constants.Tags.ERROR, Constants.Tags.ERROR_TRUE);
            throw exception;
        } finally {
            scope.close();
        }
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        Scope scope = TracerManager.tracer().attach(tracerSnapshot);
        scope.span().updateName("Future Get");
        try {
            return future.get();
        } catch (Exception exception) {
            scope.span().tag(Constants.Tags.ERROR, Constants.Tags.ERROR_TRUE);
            throw exception;
        } finally {
            scope.close();
        }
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        Scope scope = TracerManager.tracer().attach(tracerSnapshot);
        scope.span().updateName("Future Get With Timeout");
        try {
            return future.get(timeout, unit);
        } catch (Exception exception) {
            scope.span().tag(Constants.Tags.ERROR, Constants.Tags.ERROR_TRUE);
            throw exception;
        } finally {
            scope.close();
        }
    }
}
