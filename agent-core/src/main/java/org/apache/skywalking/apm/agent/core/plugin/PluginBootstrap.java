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

import cloud.erda.agent.core.config.AgentConfig;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.loader.AgentClassLoader;
import org.apache.skywalking.apm.agent.core.util.AgentPackageNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Plugins finder.
 * Use {@link PluginResourcesResolver} to find all plugins,
 * and ask {@link PluginCfg} to load all plugin definitions.
 *
 * @author wusheng
 */
public class PluginBootstrap {
    private static final ILog logger = LogManager.getLogger(PluginBootstrap.class);

    /**
     * load all plugins.
     *
     * @return plugin definition list.
     */
    public List<AbstractClassEnhancePluginDefine> loadPlugins(AgentConfig agentConfig) throws AgentPackageNotFoundException {
        AgentClassLoader.initDefaultLoader();
        List<AbstractClassEnhancePluginDefine> plugins = new ArrayList<AbstractClassEnhancePluginDefine>();
        ServiceLoader<PluginLoader> serviceLoader = ServiceLoader.load(PluginLoader.class, AgentClassLoader.getDefault());
        for (PluginLoader pluginLoader : serviceLoader) {
            try {
                Collection<PluginGroup> pluginGroups = pluginLoader.load();
                for (PluginGroup pluginGroup : pluginGroups) {
                    if (pluginGroup.empty()) {
                        logger.info("plugin {} skipped has {} instrumentation", pluginGroup.getName(), pluginGroup.getPluginDefines().size());
                        continue;
                    }
                    Boolean pluginEnabled = agentConfig.isPluginEnabled(pluginGroup.getName());
                    if (pluginEnabled == null || pluginEnabled) {
                        plugins.addAll(pluginGroup.getPluginDefines());
                        logger.info("plugin {} enabled with instrumentations \n\t\t\t\t\t\t - {}", pluginGroup.getName(), pluginGroup.getPluginDefines().stream().map(x -> x.getClass().getName()).reduce((x, y) -> x + "\n\t\t\t\t\t\t - " + y).get());
                    } else {
                        logger.info("plugin {} disabled with instrumentations \n\t\t\t\t\t\t - {}", pluginGroup.getName(), pluginGroup.getPluginDefines().stream().map(x -> x.getClass().getName()).reduce((x, y) -> x + "\n\t\t\t\t\t\t - " + y).get());
                    }
                }
            } catch (Exception exception) {
                logger.error(exception, "{} load error", pluginLoader.getClass());
            }
        }
        return plugins;
    }
}
