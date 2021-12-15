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

package cloud.erda.agent.core.metrics.exporters.tcp;

import cloud.erda.agent.core.config.loader.Config;
import cloud.erda.agent.core.config.loader.Configuration;
import org.apache.skywalking.apm.agent.core.util.Strings;

/**
 * @author liuhaoyang
 * @date 2021/11/29 21:06
 */
public class TcpExportConfig implements Config {

    /**
     * Host address of Erda automatic injection
     */
    @Configuration(name = "HOST_IP", defaultValue = "localhost")
    private String hostIp;

    @Configuration(name = "MSP_TCP_EXPORTER_HOST")
    private String exportHost;

    @Configuration(name = "MSP_TCP_EXPORTER_PORT", defaultValue = "7086")
    private Integer exportPort;

    @Configuration(name = "MSP_TCP_EXPORTER_RECONNECT_DELAY", defaultValue = "30000")
    private Integer reconnectDelay;

    @Configuration(name = "MSP_TCP_EXPORTER_IDLE_TIMEOUT", defaultValue = "60000")
    private Integer idleTimeout;

    @Configuration(name = "MSP_TCP_EXPORTER_CONNECT_TIMEOUT", defaultValue = "60000")
    private Integer connectTimeout;

    public String getExportHost() {
        if (!Strings.isEmpty(exportHost)) {
            return exportHost;
        }
        return hostIp;
    }

    public Integer getExportPort() {
        return exportPort;
    }

    public Integer getReconnectDelay() {
        return reconnectDelay;
    }

    public Integer getIdleTimeout() {
        return idleTimeout;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }
}
