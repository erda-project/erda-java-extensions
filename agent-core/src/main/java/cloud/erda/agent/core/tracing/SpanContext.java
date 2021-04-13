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
public class SpanContext {

    private String spanId;

    private String parentSpanId;

    private SpanContext(String spanId, String parentSpanId) {
        this.spanId = spanId;
        this.parentSpanId = parentSpanId;
    }

    public String getSpanId() {
        return spanId;
    }

    public String getParentSpanId() {
        return parentSpanId;
    }

    public TracerContext getTracerContext() {
        return TracerManager.tracer().context();
    }

    public static class Builder {

        private String parentSpanId;

        private String spanId;

        public TracerContext getTracerContext() {
            return TracerManager.tracer().context();
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
            return new SpanContext(spanId, parentSpanId);
        }
    }
}
