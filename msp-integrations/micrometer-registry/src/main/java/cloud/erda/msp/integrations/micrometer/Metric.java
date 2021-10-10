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

package cloud.erda.msp.integrations.micrometer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liuhaoyang
 * @date 2021/10/10 14:10
 */

public class Metric {

    private String name;

    private long timestamp;

    private Map<String, Object> fields;

    private Map<String, String> tags;

    public String getName() {
        return name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    public static class FieldBuilder {

        private final Map<String, Object> fields = new HashMap<>();

        public FieldBuilder add(String key, Object value) {
            fields.put(key, value);
            return this;
        }

        public Map<String, Object> build() {
            return fields;
        }

        public static FieldBuilder newFields() {
            return new FieldBuilder();
        }
    }
}
