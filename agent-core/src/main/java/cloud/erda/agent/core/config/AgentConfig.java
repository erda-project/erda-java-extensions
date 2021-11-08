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
import cloud.erda.agent.core.config.loader.MapConfiguration;
import org.apache.skywalking.apm.agent.core.logging.core.LogLevel;
import org.apache.skywalking.apm.agent.core.util.Strings;

import java.util.Map;

public class AgentConfig implements Config {
    @Configuration(name = "TERMINUS_KEY", defaultValue = "")
    private String _terminusKey;

    @Configuration(name = "TERMINUS_SPOT_SAMPLING_RATE", defaultValue = "50")
    private int _samplingRate;

    @Configuration(name = "TERMINUS_SPOT_SAMPLING_LIMIT", defaultValue = "1000")
    private int _samplingLimit;

    @Configuration(name = "TERMINUS_AGENT_ENABLE", defaultValue = "true")
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

    @Configuration(name = "MSP_ENV_ID", defaultValue = "")
    private String _mspEnvId;

    @Configuration(name = "MSP_ENV_TOKEN", defaultValue = "")
    private String _mspEnvToken;

    @MapConfiguration(pattern = "MSP_PLUGIN_.*._ENABLED", valueType = Boolean.class)
    private Map<String, Boolean> _pluginEnabled;

    public String terminusKey() {
        if (!Strings.isEmpty(_terminusKey)) {
            return _terminusKey;
        }
        return _mspEnvId;
    }

    public String mspEnvToken() {
        return _mspEnvToken;
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

    public Map<String, Boolean> pluginEnabled() {
        return _pluginEnabled;
    }
}
