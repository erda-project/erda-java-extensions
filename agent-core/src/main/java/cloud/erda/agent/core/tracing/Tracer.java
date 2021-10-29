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

import cloud.erda.agent.core.tracing.propagator.Carrier;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.tracing.span.SpanBuilder;

/**
 * @author liuhaoyang
 * @since 2019-01-04 17:26
 **/
public interface Tracer {

    TracerContext context();

    Scope activate(Span span);

    Scope activate(Scope scope);

    Scope active();

    Scope attach(TracerSnapshot snapshot);

    TracerSnapshot capture();

    TracerSnapshot capture(Scope scope);

    void dispatch(Span span);

    void inject(SpanContext spanContext, Carrier carrier);

    SpanContext extract(Carrier carrier);

    SpanBuilder buildSpan(String operationName);
}
