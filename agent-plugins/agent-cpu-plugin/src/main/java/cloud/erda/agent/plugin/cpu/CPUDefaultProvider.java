package cloud.erda.agent.plugin.cpu;

import cloud.erda.agent.core.config.AgentConfig;
import cloud.erda.agent.core.config.ServiceConfig;
import cloud.erda.agent.core.config.loader.ConfigAccessor;
import cloud.erda.agent.core.metrics.Metric;

/**
 * @author zhaihongwei
 * @since 2022/2/21
 */
public interface CPUDefaultProvider {

    AgentConfig agentConfig = ConfigAccessor.Default.getConfig(AgentConfig.class);
    ServiceConfig serviceConfig = ConfigAccessor.Default.getConfig(ServiceConfig.class);

    default void addDefaultTags(Metric metric) {
        metric.addTag("terminus_key", agentConfig.terminusKey()).
                addTag("instance_id", serviceConfig.getServiceInstanceId()).
                addTag("service_instance_id", serviceConfig.getServiceInstanceId()).
                addTag("service_id", serviceConfig.getServiceId()).
                addTag("service_ip", serviceConfig.getServiceIp()).
                addTag("service_name", serviceConfig.getServiceName()).
                addTag("project_id", serviceConfig.getProjectId()).
                addTag("runtime_id", serviceConfig.getRuntimeId()).
                addTag("application_id", serviceConfig.getApplicationId()).
                addTag("runtime_name", serviceConfig.getRuntimeName()).
                addTag("application_name", serviceConfig.getApplicationName()).
                addTag("project_name", serviceConfig.getProjectName()).
                addTag("workspace", serviceConfig.getWorkspace()).
                addTag("org_name", serviceConfig.getOrgName()).
                addTag("org_id", serviceConfig.getOrgId());
    }
}
