/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.apm.agent.core.plugin;

import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.loader.AgentClassLoader;
import org.apache.skywalking.apm.agent.core.util.AgentPackageNotFoundException;

import java.net.URL;
import java.util.*;

/**
 * @author liuhaoyang
 * @date 2021/5/10 16:23
 */
public class ResourcesPluginLoader implements PluginLoader {

    private static final ILog logger = LogManager.getLogger(ResourcesPluginLoader.class);

    @Override
    public Collection<PluginGroup> load() throws AgentPackageNotFoundException {

        PluginResourcesResolver resolver = new PluginResourcesResolver();
        List<URL> resources = resolver.getResources();

        if (resources == null || resources.size() == 0) {
            logger.info("no plugin files (erda-agent-plugin.def)) found, continue to start application.");
            return Collections.emptyList();
        }

        for (URL pluginUrl : resources) {
            try {
                PluginCfg.INSTANCE.load(pluginUrl.openStream());
            } catch (Throwable t) {
                logger.error(t, "plugin file [{}] init failure.", pluginUrl);
            }
        }

        List<PluginDefine> pluginClassList = PluginCfg.INSTANCE.getPluginClassList();

        Map<String, PluginGroup> plugins = new HashMap<>();
        for (PluginDefine pluginDefine : pluginClassList) {
            try {
//                logger.info("loading plugin class {}.", pluginDefine.getDefineClass());
                AbstractClassEnhancePluginDefine plugin =
                        (AbstractClassEnhancePluginDefine) Class.forName(pluginDefine.getDefineClass(),
                                        true,
                                        AgentClassLoader.getDefault())
                                .newInstance();
                PluginGroup pluginGroup = plugins.computeIfAbsent(pluginDefine.getName(), PluginGroup::group);
                pluginGroup.getPluginDefines().add(plugin);
            } catch (Throwable t) {
                logger.error(t, "load plugin [{}] failure.", pluginDefine.getDefineClass());
            }
        }

        return plugins.values();
    }
}