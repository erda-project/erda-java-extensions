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

import java.util.Map;

/**
 * @author liuhaoyang
 * @since 2019-01-04 16:54
 **/
public class TracerContext extends Context<Object> implements ContextSnapshot<TracerContext> {

    public <T> T getAttachment(String key) {
        return (T) super.get(key);
    }

    public <T> void setAttachment(String key, T value) {
        super.put(key, value);
    }

    public void attach(TracerContext tracerContext) {
        for (Map.Entry<String, Object> att : tracerContext) {
            setAttachment(att.getKey(), att.getValue());
        }
    }

    public TracerContext capture() {
        TracerContext context = new TracerContext();
        context.attach(this);
        return context;
    }
}
