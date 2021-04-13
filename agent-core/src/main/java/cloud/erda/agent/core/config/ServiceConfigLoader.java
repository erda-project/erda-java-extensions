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

package cloud.erda.agent.core.config;

import cloud.erda.agent.core.config.loader.ConfigLoader;
import cloud.erda.agent.core.config.loader.ConfigLoaderModule;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ConfigLoaderModule(priority = 2)
public class ServiceConfigLoader extends ConfigLoader {
    @Override
    protected Map<String, String> getConfigurations(Object instance) {
        Map<String, String> configMap = new HashMap<String, String>();
        configMap.put("SERVICE_INSTANCE_ID", UUID.randomUUID().toString());
        return configMap;
    }
}
