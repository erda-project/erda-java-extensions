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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author liuhaoyang
 * @since 2019-01-04 16:54
 **/
public class TracerContext implements Iterable<Map.Entry<String, String>> {

    public final static String REQUEST_ID = "request-id";
    public final static String SAMPLED = "sampled";

    private final Map<String, String> map = new HashMap<String, String>();
    private final Map<String, Object> attachments = new HashMap<String, Object>();

    public String requestId() {
        return map.get(REQUEST_ID);
    }

    public Boolean sampled() {
        String sampled = map.get(SAMPLED);
        if (sampled != null) {
            return Boolean.valueOf(sampled);
        }
        return null;
    }

    public void put(String key, String value) {
        map.put(key, value);
    }

    public void put(ContextIterator iterator) {
        for (Map.Entry<String, String> entry : iterator) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public String get(String key) {
        return map.get(key);
    }

    public <T> T getAttachment(String key) {
        return (T) attachments.get(key);
    }

    public <T> void setAttachment(String key, T value) {
        attachments.put(key, value);
    }

    public void attach(TracerContext tracerContext) {
        for (Map.Entry<String, String> entry : tracerContext) {
            map.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Object> att : tracerContext.attachments.entrySet()) {
            attachments.put(att.getKey(), att.getValue());
        }
    }

    public TracerContext capture() {
        TracerContext context = new TracerContext();
        context.attach(this);
        return context;
    }

    public void close() {
        map.clear();
        attachments.clear();
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return map.entrySet().iterator();
    }

    public interface ContextIterator extends Iterable<Map.Entry<String, String>> {
    }
}
