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

package cloud.erda.agent.core.tracing.span;

import cloud.erda.agent.core.tracing.*;
import cloud.erda.agent.core.utils.UUIDGenerator;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liuhaoyang
 * @since 2019-01-07 21:43
 **/
public class SpanBuilder {
    private final String operationName;
    private final Tracer tracer;
    private final Sampler sampler;

    private Map<String, String> tags;
    private SpanContext reference;

    public SpanBuilder(String operationName, Tracer tracer, Sampler sampler) {
        this.operationName = operationName;
        this.tracer = tracer;
        this.sampler = sampler;
    }

    public SpanBuilder tag(String key, String value) {
        if (tags == null) {
            tags = new HashMap<>();
        }
        tags.put(key, value);
        return this;
    }

    public SpanBuilder childOf(SpanContext spanContext) {
        reference = spanContext;
        return this;
    }

    public Scope startActive() {
        return startActive(true);
    }

    public Scope startActive(boolean activate) {
        if (activate) {
            return tracer.activate(build());
        }
        return new Scope(tracer, build(), null);
    }

    private Span build() {
        SpanContext.Builder contextBuilder = new SpanContext.Builder();
        if (reference == null) {
            contextBuilder.setTraceId(UUIDGenerator.New());
            contextBuilder.setSampled(sampler.shouldSample());
        } else {
            contextBuilder.setParentSpanId(reference.getSpanId());
            contextBuilder.setTraceId(reference.getTraceId() != null ? reference.getTraceId() : UUIDGenerator.New());
            contextBuilder.setSampled(reference.getSampled() != null ? reference.getSampled() : sampler.shouldSample());
            contextBuilder.setBaggage(reference.getBaggage());
        }
        SpanContext context = contextBuilder.build();
        if (!context.getSampled()) {
            return new SpanNoop(context);
        }
        return new SpanImpl(operationName, tags != null ? tags : new HashMap<>(), context, tracer);
    }
}
