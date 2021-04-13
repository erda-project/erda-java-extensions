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

package cloud.erda.agent.plugin.monitor.sdk.config;

import cloud.erda.agent.core.config.loader.ConfigAccessor;
import cloud.erda.agent.core.config.ServiceConfig;
import cloud.erda.agent.core.tracing.Scope;
import cloud.erda.agent.core.tracing.TracerManager;
import io.terminus.dice.monitor.sdk.config.Config;

/**
 * @author liuhaoyang 2020/3/17 23:51
 */
public class MonitorConfig implements Config {

    private static final ServiceConfig serviceConfig = ConfigAccessor.Default.getConfig(ServiceConfig.class);

    @Override
    public String getRequestId() {
        String requestId = TracerManager.tracer().context().requestId();
        return requestId == null ? "" : requestId;
    }

    @Override
    public String getSpanId() {
        Scope scope = TracerManager.tracer().active();
        if (scope != null) {
            return scope.span().getContext().getSpanId();
        }
        return "";
    }

    @Override
    public String getServiceName() {
        return serviceConfig.getServiceName();
    }

    @Override
    public String getServiceInstanceId() {
        return serviceConfig.getServiceInstanceId();
    }

    @Override
    public String getApplicationId() {
        return serviceConfig.getApplicationId();
    }

    @Override
    public String getApplicationName() {
        return serviceConfig.getApplicationName();
    }

    @Override
    public String getProjectId() {
        return serviceConfig.getProjectId();
    }

    @Override
    public String getProjectName() {
        return serviceConfig.getProjectName();
    }
}
