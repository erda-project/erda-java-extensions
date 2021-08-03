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

package cloud.erda.agent.plugin.app.insight;

import cloud.erda.agent.core.config.AddonConfig;
import cloud.erda.agent.core.config.AgentConfig;
import cloud.erda.agent.core.config.ServiceConfig;
import cloud.erda.agent.core.config.loader.ConfigAccessor;

/**
 * @author liuhaoyang
 * @date 2021/8/3 12:07
 */
public class Configs {

    public static final ServiceConfig ServiceConfig = ConfigAccessor.Default.getConfig(ServiceConfig.class);
    public static final AgentConfig AgentConfig = ConfigAccessor.Default.getConfig(AgentConfig.class);
    public static final AddonConfig AddonConfig = ConfigAccessor.Default.getConfig(AddonConfig.class);
}
