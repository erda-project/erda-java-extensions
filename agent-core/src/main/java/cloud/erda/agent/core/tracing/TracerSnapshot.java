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

import cloud.erda.agent.core.tracing.span.Span;

/**
 * @author liuhaoyang
 * @since 2019-01-09 15:25
 **/
public class TracerSnapshot {

    private TracerContext tracerContext;
    private Span span;

    public TracerSnapshot(TracerContext tracerContext, Span span) {
        this.tracerContext = tracerContext;
        this.span = span;
    }

    public TracerContext getTracerContext() {
        return tracerContext;
    }

    public Span getSpan() {
        return span;
    }
}
