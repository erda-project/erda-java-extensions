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

package cloud.erda.agent.core.utils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author randomnil
 */
public enum AddonTypeManager {
    /**
     * 单例
     */
    INSTANCE;

    private static final String CONFIG_CENTER_ADDON_TYPE = "configCenter";
    private static final String REGISTER_CENTER_ADDON_TYPE = "registerCenter";

    private final Map<String, String> ADDON_TYPE_SET = new ConcurrentHashMap<String, String>();

    public Set<String> getAddonTypeSet() {
        return ADDON_TYPE_SET.keySet();
    }

    public void addConfigCenter() {
        ADDON_TYPE_SET.put(CONFIG_CENTER_ADDON_TYPE, "");
    }

    public void addRegisterCenter() {
        ADDON_TYPE_SET.put(REGISTER_CENTER_ADDON_TYPE, "");
    }

}
