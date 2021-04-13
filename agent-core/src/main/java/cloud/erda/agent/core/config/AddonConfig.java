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

import cloud.erda.agent.core.config.loader.Config;
import cloud.erda.agent.core.config.loader.Configuration;

/**
 * @author liuhaoyang
 * @since 2020-02-19 14:32
 **/
public class AddonConfig implements Config {

    @Configuration(name = "ADDON_TYPE")
    private String addonType;

    @Configuration(name = "ADDON_ID")
    private String addonId;

    public String getAddonId() {
        return addonId;
    }

    public String getAddonType() {
        return addonType;
    }
}
