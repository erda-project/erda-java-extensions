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
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.TracerSnapshot;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.utils.Constants;

/**
 * @author liuhaoyang
 * @date 2021/10/28 10:18
 */
public class RunnableWrapper implements Runnable {

    private final Runnable runnable;
    private final TracerSnapshot tracerSnapshot;

    public RunnableWrapper(Runnable runnable, TracerSnapshot tracerSnapshot) {
        this.runnable = runnable;
        this.tracerSnapshot = tracerSnapshot;
    }

    @Override
    public void run() {
        Scope scope = TracerManager.currentTracer().attach(tracerSnapshot);
        Span span = scope.span();
        span.updateName("Cross Thread Runnable");
        span.tag(Constants.Tags.SPAN_LAYER, Constants.Tags.SPAN_LAYER_LOCAL);
        span.tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_LOCAL);
        span.tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_THREAD_POOL);
        try {
            runnable.run();
        } catch (Exception exception) {
            scope.span().tag(Constants.Tags.ERROR, Constants.Tags.ERROR_TRUE);
            throw exception;
        } finally {
            scope.close();
        }
    }
}
