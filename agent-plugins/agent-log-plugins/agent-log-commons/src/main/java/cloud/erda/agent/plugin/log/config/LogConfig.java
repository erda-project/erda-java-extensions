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

package cloud.erda.agent.plugin.log.config;

import cloud.erda.agent.core.config.loader.Config;
import cloud.erda.agent.core.config.loader.Configuration;

/**
 * @author liuhaoyang
 * @since 2020/11/17 10:54
 */
public class LogConfig implements Config {

    @Configuration(name = "TERMINUS_LOG_FORCE_STDOUT", defaultValue = "true")
    private boolean forceStdout;

    public Boolean getForceStdout() {
        return forceStdout;
    }

    @Configuration(name = "TERMINUS_LOG_FORCE_FORMAT", defaultValue = "true")
    private boolean forceFormat;

    public boolean getForceFormat() {
        return forceFormat;
    }
}
