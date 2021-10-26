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

    private static ILog logger = LogManager.getLogger(Scope.class);
//    private ScopeTracer tracer;

    private Scope previous;
    private Scope next;

    private Tracer tracer;
    private Span span;

    public Scope(Tracer tracer, Span span, Scope previous) {
        this.span = span;
        this.tracer = tracer;
        if (previous != null) {
            previous.setNext(this);
        }
    }

    public Scope getPrevious() {
        return previous;
    }

    public Scope getNext() {
        return next;
    }

    public void setNext(Scope next) {
        this.next = next;
        if (next != null) {
            next.previous = this;
        }
    }

    public Span span() {
        return span;
    }

    /**
     * When transferring across threads, you need to explicitly call close(false) and only release the context without ending the span.
     * Activate span to the tracer of the target process
     **/
    public void close(boolean finish) {
        //span finish
        if (finish) {
            span.finish();
        }
        if (previous != null) {
            previous.setNext(next);
        }
        if (tracer.active() == this) {
            tracer.activate(previous);
        }
    }

    public void close() {
        close(true);
    }
}
