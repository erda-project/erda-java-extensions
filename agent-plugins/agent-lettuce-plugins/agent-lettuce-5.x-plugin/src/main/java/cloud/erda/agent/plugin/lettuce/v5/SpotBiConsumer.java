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

package cloud.erda.agent.plugin.lettuce.v5;

import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.TracerSnapshot;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.tracing.span.SpanBuilder;
import cloud.erda.agent.core.utils.TracerUtils;

import java.util.function.BiConsumer;

public class SpotBiConsumer<T, U> implements BiConsumer<T, U> {

    private TracerSnapshot snapshot;
    private BiConsumer<T, U> biConsumer;
    private String operationName;

    SpotBiConsumer(BiConsumer<T, U> biConsumer, TracerSnapshot snapshot, String operationName) {
        this.biConsumer = biConsumer;
        this.operationName = operationName;
        this.snapshot = snapshot;
    }

    @Override
    public void accept(T t, U u) {
        Tracer tracer = TracerManager.tracer();
        tracer.context().attach(snapshot.getTracerContext());
        SpanContext spanContext = snapshot.getSpan() != null ? snapshot.getSpan().getContext() : null;
        SpanBuilder spanBuilder = tracer.buildSpan(operationName + "/accept");
        Span span = spanBuilder.childOf(spanContext).startActive().span();
        span.tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_LETTUCE);
        span.tag(Constants.Tags.DB_TYPE, Constants.Tags.DB_TYPE_REDIS);
        span.tag(Constants.Tags.SPAN_LAYER, Constants.Tags.SPAN_LAYER_CACHE);
        try {
            biConsumer.accept(t, u);
        } catch (Throwable th) {
            TracerUtils.handleException(th);
        } finally {
            TracerManager.tracer().active().close();
        }
    }
}
