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
import org.apache.skywalking.apm.agent.core.util.Strings;

/**
 * @author liuhaoyang
 * @since 2020-02-19 14:34
 **/
public class TelegrafProxyConfig implements Config {
    @Configuration(name = "HOST")
    private String proxyHost;

    @Configuration(name = "HOST_IP")
    private String k8sProxyHost;

    @Configuration(name = "HOST_PORT", defaultValue = "7082")
    private int proxyPort;

    public int getProxyPort() {
        return proxyPort;
    }

    public String getProxyHost() {
        if (!Strings.isEmpty(k8sProxyHost)) {
            return k8sProxyHost;
        }
        return proxyHost;
    }
}
