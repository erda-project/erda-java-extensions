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
 * @since 2020-02-19 11:39
 **/
public class ServiceConfig implements Config {

    @Configuration(name = "DICE_SERVICE")
    private String serviceName;

    @Configuration(name = "DICE_PROJECT")
    private String projectId;

    @Configuration(name = "DICE_PROJECT_NAME")
    private String projectName;

    @Configuration(name = "DICE_APPLICATION")
    private String applicationId;

    @Configuration(name = "DICE_APPLICATION_NAME")
    private String applicationName;

    @Configuration(name = "DICE_RUNTIME")
    private String runtimeId;

    @Configuration(name = "DICE_RUNTIME_NAME")
    private String runtimeName;

    @Configuration(name = "DICE_ORG")
    private String orgId;

    @Configuration(name = "DICE_ORG_NAME")
    private String orgName;

    @Configuration(name = "DICE_WORKSPACE", defaultValue = "DEV")
    private String workspace;

    @Configuration(name = "SERVICE_INSTANCE_ID")
    private String serviceInstanceId;

    @Configuration(name = "MESOS_TASK_ID")
    private String mesosTaskId;

    @Configuration(name = "POD_UUID")
    private String podUUID;

    @Configuration(name = "POD_IP")
    private String podIp;

    @Configuration(name = "MONITOR_JVM_PROFILER_ID")
    private String jvmProfilerId;

    @Configuration(name = "DICE_HTTP_HEALTHCHECK_PATH")
    private String httpHealthCheckPath;

    public String getApplicationId() {
        return applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getOrgId() {
        return orgId;
    }

    public String getOrgName() {
        return orgName;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getRuntimeId() {
        return runtimeId;
    }

    public String getRuntimeName() {
        return runtimeName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getWorkspace() {
        if (!Strings.isEmpty(workspace)) {
            return workspace.toUpperCase();
        }
        return workspace;
    }

    public String getServiceIp() {
        return podIp;
    }

    public String getJvmProfilerId() {
        return jvmProfilerId;
    }

    public String getHttpHealthCheckPath() {
        return httpHealthCheckPath;
    }

    public String getServiceInstanceId() {
        // 在DC/OS环境下，使用mesosTaskId作为服务实例id
        // 在k8s环境，使用pod_uuid作为服务实例id
        // 在非DICE环境，使用进程内生成uuid
        if (!Strings.isEmpty(mesosTaskId)) {
            return mesosTaskId;
        }
        if (!Strings.isEmpty(podUUID)) {
            return podUUID;
        }
        return serviceInstanceId;
    }

    public String getServiceId() {
        if (applicationId == null || "".equals(applicationId)) {
            if (runtimeName == null || "".equals(runtimeName)) {
                return serviceName;
            }
            return runtimeName + "_" + serviceName;
        }
        return applicationId + "_" + runtimeName + "_" + serviceName;
    }
}
