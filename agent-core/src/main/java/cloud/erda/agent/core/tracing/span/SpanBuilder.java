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
    private String operationName;
    private Map<String, String> tags;
    private SpanContext reference;
    private Tracer tracer;
    private Sampler sampler;

    public SpanBuilder(String operationName, Tracer tracer, Sampler sampler) {
        this.operationName = operationName;
        this.tags = new HashMap<String, String>();
        this.tracer = tracer;
        this.sampler = sampler;
    }

    public SpanBuilder tag(String key, String value) {
        tags.put(key, value);
        return this;
    }

    public SpanBuilder childOf(SpanContext spanContext) {
        reference = spanContext;
        return this;
    }

    public Scope startActive() {
        return tracer.activate(build());
    }

    private Span build() {
        ensureRequestId();
        ensureSampled();
        SpanContext.Builder spanContextBuilder = new SpanContext.Builder();
        if (reference != null) {
            spanContextBuilder.setParentSpanId(reference.getSpanId());
        }
        SpanContext spanContext = spanContextBuilder.build();
        if (!tracer.context().sampled()) {
            return new SpanNoop(spanContext);
        }
        return new SpanImpl(operationName, tags, spanContext, tracer);
    }

    private void ensureSampled() {
        Boolean sampled = tracer.context().sampled();
        if (sampled == null) {
            sampled = sampler.shouldSample();
            tracer.context().put(TracerContext.SAMPLED, String.valueOf(sampled));
        }
    }

    private void ensureRequestId() {
        String requestId = tracer.context().requestId();
        if (requestId == null) {
            requestId = UUIDGenerator.New();
            tracer.context().put(TracerContext.REQUEST_ID, requestId);
        }
    }

}
