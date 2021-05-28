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

package cloud.erda.agent.plugin.sdk;

import cloud.erda.msp.monitor.tracing.Span;

/**
 * @author liuhaoyang
 * @date 2021/5/26 16:56
 */
public class SpanWrapper extends Span {

    private final cloud.erda.agent.core.tracing.span.Span activeSpan;

    public SpanWrapper(cloud.erda.agent.core.tracing.span.Span activeSpan) {
        this.activeSpan = activeSpan;
    }

    @Override
    public String getOperationName() {
        return activeSpan.getOperationName();
    }

    @Override
    public void setOperationName(String operationName) {
        activeSpan.setOperationName(operationName);
    }

    @Override
    public String spanId() {
        return activeSpan.getContext().getSpanId();
    }

    @Override
    public void tag(String key, String value) {
        activeSpan.tag(key, value);
    }
}
