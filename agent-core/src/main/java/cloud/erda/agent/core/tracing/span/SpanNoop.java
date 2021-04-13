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

import java.util.HashMap;
import java.util.Map;

/**
 * @author liuhaoyang
 * @since 2019-01-07 21:30
 **/
public class SpanNoop implements Span {

    private static Map<String, String> noopTags = new HashMap<String, String>(0);

    private SpanContext spanContext;

    public SpanNoop(SpanContext spanContext) {
        this.spanContext = spanContext;
    }

    @Override
    public SpanContext getContext() {
        return spanContext;
    }

    @Override
    public String getOperationName() {
        return null;
    }

    @Override
    public long getStartTime() {
        return 0;
    }

    @Override
    public long getEndTime() {
        return 0;
    }

    @Override
    public Map<String, String> getTags() {
        return noopTags;
    }

    @Override
    public void tag(String key, String value) {
    }

    @Override
    public void finish() {
    }
}
