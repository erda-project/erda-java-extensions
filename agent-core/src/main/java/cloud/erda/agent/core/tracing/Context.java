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
 * @date 2021/10/26 11:39
 */
public class Context<TValue> implements Iterable<Map.Entry<String, TValue>> {

    public static final String TRACE_ID = "x-msp-trace-id";
    public static final String SAMPLED = "x-msp-trace-sampled";
    private final Map<String, TValue> map = new HashMap<>();

    public Context() {
    }

    public Context(Context<TValue> context) {
        map.putAll(context.map);
    }

    public void put(String key, TValue value) {
        map.put(key, value);
    }

    public void putAll(Context.ContextIterator<TValue> iterator) {
        for (Map.Entry<String, TValue> entry : iterator) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public TValue get(String key) {
        return map.get(key);
    }

    public void close() {
        map.clear();
    }

    @Override
    public Iterator<Map.Entry<String, TValue>> iterator() {
        return map.entrySet().iterator();
    }

    public interface ContextIterator<TValue> extends Iterable<Map.Entry<String, TValue>> {
    }
}
