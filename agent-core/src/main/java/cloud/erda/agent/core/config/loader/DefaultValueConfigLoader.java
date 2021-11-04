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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liuhaoyang
 */
@ConfigLoaderModule(priority = 9)
public class DefaultValueConfigLoader extends ConfigLoader {

    @Override
    protected Map<String, String> getConfigurations(Object instance) {
        Map<String, String> configMap = new HashMap<String, String>();
        Class insClass = instance.getClass();
        Field[] fields = insClass.getDeclaredFields();
        for (Field field : fields) {
            Configuration configuration = field.getAnnotation(Configuration.class);
            if (configuration != null) {
                configMap.put(configuration.name(), configuration.defaultValue());
            }
        }
        return configMap;
    }

    @Override
    protected boolean validateValue(String name, String value,  Field field) {
        return true;
    }
}