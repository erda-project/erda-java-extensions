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

package cloud.erda.agent.core.tracing;

import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import cloud.erda.agent.core.tracing.span.Span;

/**
 * @author liuhaoyang
 * @since 2019-01-04 17:37
 **/
public class Scope {

    private static ILog log = LogManager.getLogger(Scope.class);

    private ScopeTracer tracer;
    private Scope last;
    private Span active;

    public Scope(ScopeTracer tracer, Span span) {
        this.tracer = tracer;
        this.last = tracer.active();
        this.active = span;
        this.tracer.activate(this);
    }

    public Span span() {
        return active;
    }

    /**
     * When transferring across threads, you need to explicitly call close(false) and only release the context without ending the span.
     * Activate span to the tracer of the target process
     **/
    public void close(boolean finish) {
        if (tracer.active() != this) {
            return;
        }

        //span finish
        if (finish) {
            active.finish();
        }

        if (last == null) {
            tracer.context().close();
        }
        tracer.activate(last);
    }

    public void close() {
        close(true);
    }
}
