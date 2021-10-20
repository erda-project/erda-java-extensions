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

import java.util.HashMap;
import java.util.Map;

/**
 * @author liuhaoyang
 * @date 2021/10/20 18:10
 */
public class SpanLogImpl implements SpanLog {

    private final Long timestamp;

    private final Map<String, String> fields;

    public SpanLogImpl(long timestamp) {
        this.timestamp = timestamp;
        this.fields = new HashMap<>();
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public Map<String, String> getFields() {
        return fields;
    }

    @Override
    public SpanLog event(String key, String field) {
        fields.put(key, field);
        return this;
    }
}
