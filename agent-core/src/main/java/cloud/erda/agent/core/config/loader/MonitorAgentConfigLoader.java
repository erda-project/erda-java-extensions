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

import org.apache.skywalking.apm.agent.core.util.AgentPackageNotFoundException;
import org.apache.skywalking.apm.agent.core.util.AgentPackagePath;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

@ConfigLoaderModule(priority = 2)
public class MonitorAgentConfigLoader extends ConfigLoader {

    private final static String OS_NAME = System.getProperty("os.name");
    private final static String OS_VERSION = System.getProperty("os.version");
    private final static String RUNTIME_VERSION = System.getProperty("java.runtime.version");

    @Override
    protected Map<String, String> getConfigurations(Object instance) {
        Map<String, String> configMap = new HashMap<String, String>();
        configMap.put("monitor.agent.platform", "JDK " + RUNTIME_VERSION);
        configMap.put("monitor.agent.os", OS_NAME + " " + OS_VERSION);
        configMap.put("erda.agent.name", "erda-java-agent");
        try {
            String versionFile = AgentPackagePath.getPath() + "/build";
            String version = Files
                    .lines(Paths.get(versionFile), Charset.defaultCharset()).findFirst().orElse("unknown");
            configMap.put("erda.agent.version", version);
        } catch (IOException | AgentPackageNotFoundException ignored) {
        }
        return configMap;
    }
}
