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

import cloud.erda.agent.core.utils.UUIDGenerator;

/**
 * @author liuhaoyang
 * @since 2019-01-04 17:07
 **/
public class SpanContext implements ContextSnapshot<SpanContext> {

    private String spanId;

    private String parentSpanId;

    private String traceId;

    private Boolean sampled;

    private Context<String> baggage;

    private SpanContext(String traceId, String spanId, String parentSpanId, Boolean sampled, Context<String> baggage) {
        this.traceId = traceId;
        this.sampled = sampled;
        this.spanId = spanId;
        this.parentSpanId = parentSpanId;
        this.baggage = baggage;
    }

    public String getTraceId() {
        return traceId;
    }

    public Boolean getSampled() {
        return sampled;
    }

    public String getSpanId() {
        return spanId;
    }

    public String getParentSpanId() {
        return parentSpanId;
    }

    public Context<String> getBaggage() {
        return baggage;
    }

    @Override
    public void attach(SpanContext context) {

    }

    @Override
    public SpanContext capture() {
        return new SpanContext(traceId, spanId, parentSpanId, sampled, new Context<>(baggage));
    }

    public static class Builder {

        private String parentSpanId;

        private String spanId;

        private Context<String> baggage;

        private String traceId;

        private Boolean sampled;

        public void setBaggage(Context<String> baggage) {
            this.baggage = baggage;
        }

        public void setSampled(Boolean sampled) {
            this.sampled = sampled;
        }

        public void setTraceId(String traceId) {
            this.traceId = traceId;
        }

        public void setParentSpanId(String parentSpanId) {
            this.parentSpanId = parentSpanId;
        }

        public void setSpanId(String spanId) {
            this.spanId = spanId;
        }

        public SpanContext build() {
            return build(true);
        }

        public SpanContext build(final boolean initSpanId) {
            String spanId = initSpanId ? (this.spanId != null ? this.spanId : UUIDGenerator.New()) : this.spanId;
            return new SpanContext(traceId, spanId, parentSpanId, sampled, baggage != null ? baggage : new Context<>());
        }
    }
}
