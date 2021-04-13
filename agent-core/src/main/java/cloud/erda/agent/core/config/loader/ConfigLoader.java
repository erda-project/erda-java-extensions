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

package cloud.erda.agent.core.config.loader;

import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.util.ReflectionUtils;
import org.apache.skywalking.apm.agent.core.util.Strings;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public abstract class ConfigLoader {

    private static final ILog log = LogManager.getLogger(ConfigLoader.class);

    protected abstract Map<String, String> getConfigurations(Object instance);

    public void load(Object instance) {
        Class insClass = instance.getClass();
        Field[] fields = insClass.getDeclaredFields();
        Map<String, String> configurations = getConfigurations(instance);
        Map<String, String> properties = new HashMap<String, String>(configurations.size());
        for (Map.Entry<String, String> entry : configurations.entrySet()) {
            if (Strings.isEmpty(entry.getKey())) {
                continue;
            }
            properties.put(entry.getKey().toUpperCase().replace(".", "_"), entry.getValue());
        }
        for (Field field : fields) {
            Configuration configuration = field.getAnnotation(Configuration.class);
            if (configuration != null) {
                String name = configuration.name();
                String value = properties.get(name);
                loadValue(name, value, instance, field);

            }
        }
    }

    protected boolean validateValue(String name, String value, Object instance, Field field) {
        return value != null && value.length() > 0;
    }

    private void loadValue(String name, String value, Object instance, Field field) {
        if (validateValue(name, value, instance, field)) {
            try {
                field.setAccessible(true);
                field.set(instance, ReflectionUtils.castValue(value, field.getType()));
                log.info("Load {} config {}={} from {}", instance.getClass().getSimpleName(), name, value, getClass().getSimpleName());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}