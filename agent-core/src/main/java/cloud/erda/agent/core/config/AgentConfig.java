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
import org.apache.skywalking.apm.agent.core.logging.core.LogLevel;

public class AgentConfig implements Config {
    @Configuration(name = "TERMINUS_KEY", defaultValue = "")
    private String _terminusKey;

    @Configuration(name = "TERMINUS_SPOT_SAMPLING_RATE", defaultValue = "50")
    private int _samplingRate;

    @Configuration(name = "TERMINUS_SPOT_SAMPLING_LIMIT", defaultValue = "100")
    private int _samplingLimit;

    @Configuration(name = "TERMINUS_AGENT_ENABLE", defaultValue = "false")
    private boolean _agentEnable;

    @Configuration(name = "TERMINUS_AGENT_LOGLEVEL", defaultValue = "OFF")
    private LogLevel _logLevel;

    @Configuration(name = "MONITOR_AGENT_NAME")
    private String _agentName;

    @Configuration(name = "MONITOR_AGENT_VERSION")
    private String _agentVersion;

    @Configuration(name = "MONITOR_AGENT_PLATFORM")
    private String _agentPlatform;

    @Configuration(name = "MONITOR_AGENT_OS")
    private String _agentOsInfo;

    public String terminusKey() {
        return _terminusKey;
    }

    public int samplingRate() {
        return _samplingRate;
    }

    public int samplingLimit() {
        return _samplingLimit;
    }

    public boolean agentEnable() {
        return _agentEnable;
    }

    public LogLevel logLevel() {
        return _logLevel;
    }

    public String agentName() {
        return this._agentName;
    }

    public String agentVersion() {
        return this._agentVersion;
    }

    public String agentPlatform() {
        return this._agentPlatform;
    }

    public String agentOsInfo() {
        return this._agentOsInfo;
    }
}
