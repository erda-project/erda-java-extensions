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

package cloud.erda.agent.plugin.app.insight.transaction;

import cloud.erda.agent.core.config.AddonConfig;
import cloud.erda.agent.core.config.AgentConfig;
import cloud.erda.agent.core.config.loader.ConfigAccessor;
import cloud.erda.agent.core.config.ServiceConfig;
import cloud.erda.agent.core.tracing.TracerContext;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.plugin.app.insight.Configs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author liuhaoyang
 * @since 2019-01-21 18:27
 **/
public class TransactionMetricContext implements TracerContext.ContextIterator {
    public static final TracerContext.ContextIterator instance = new TransactionMetricContext();

    private final Map<String, String> map = new HashMap<String, String>();

    public TransactionMetricContext() {
        map.put(Constants.Metrics.SOURCE_ADDON_TYPE_ATTACH, Configs.AddonConfig.getAddonType());
        map.put(Constants.Metrics.SOURCE_ADDON_ID_ATTACH, Configs.AddonConfig.getAddonId());
        map.put(Constants.Metrics.SOURCE_ORG_ID, Configs.ServiceConfig.getOrgId());
        map.put(Constants.Metrics.SOURCE_PROJECT_ID, Configs.ServiceConfig.getProjectId());
        map.put(Constants.Metrics.SOURCE_PROJECT_NAME, Configs.ServiceConfig.getProjectName());
        map.put(Constants.Metrics.SOURCE_APPLICATION_ID, Configs.ServiceConfig.getApplicationId());
        map.put(Constants.Metrics.SOURCE_APPLICATION_NAME, Configs.ServiceConfig.getApplicationName());
        map.put(Constants.Metrics.SOURCE_RUNTIME_ID, Configs.ServiceConfig.getRuntimeId());
        map.put(Constants.Metrics.SOURCE_RUNTIME_NAME, Configs.ServiceConfig.getRuntimeName());
        map.put(Constants.Metrics.SOURCE_SERVICE_NAME, Configs.ServiceConfig.getServiceName());
        map.put(Constants.Metrics.SOURCE_SERVICE_ID, Configs.ServiceConfig.getServiceId());
        map.put(Constants.Metrics.SOURCE_TERMINUS_KEY, Configs.AgentConfig.terminusKey());
        map.put(Constants.Metrics.SOURCE_WORKSPACE, Configs.ServiceConfig.getWorkspace());
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return map.entrySet().iterator();
    }
}
