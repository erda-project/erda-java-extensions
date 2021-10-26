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

import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.utils.DateTime;
import cloud.erda.agent.core.tracing.Tracer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author liuhaoyang
 * @since 2019-01-07 19:20
 **/
public class SpanImpl implements Span {

    private SpanContext spanContext;
    private Long startTime;
    private Long endTime;
    private String operationName;
    private Map<String, String> tags;
    private Tracer tracer;
    private List<SpanLog> logs;
    private AtomicInteger logInteger;

    public SpanImpl(String operationName, Map<String, String> tags, SpanContext spanContext, Tracer tracer) {
        this.operationName = operationName;
        this.spanContext = spanContext;
        this.tags = tags;
        this.startTime = DateTime.currentTimeNano();
        this.tracer = tracer;
        this.logs = new ArrayList<>();
        this.logInteger = new AtomicInteger();
    }

    @Override
    public SpanContext getContext() {
        return spanContext;
    }

    @Override
    public String getOperationName() {
        return operationName;
    }

    @Override
    public void updateName(String operationName) {
        if (operationName == null) {
            return;
        }
        this.operationName = operationName;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public long getEndTime() {
        if (endTime == null) {
            return DateTime.currentTimeNano();
        }
        return endTime;
    }

    @Override
    public Map<String, String> getTags() {
        return tags;
    }

    @Override
    public void tag(String key, String value) {
        tags.put(key, value);
    }

    @Override
    public SpanLogImpl log(Long timestamp) {
        SpanLogImpl log = new SpanLogImpl(timestamp + logInteger.addAndGet(100));
        logs.add(log);
        return log;
    }

    @Override
    public List<SpanLog> getLogs() {
        return logs;
    }

    @Override
    public void finish() {
        if (endTime == null) {
            endTime = DateTime.currentTimeNano();
            tracer.dispatch(this);
        }
    }
}
