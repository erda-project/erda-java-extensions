package cloud.erda.agent.core.config;

import cloud.erda.agent.core.config.loader.Config;
import cloud.erda.agent.core.config.loader.Configuration;

/**
 * @author: liuhaoyang
 * @create: 2020-02-19 11:36
 **/
public class ServiceMeshConfig implements Config {

    @Configuration(name = "ADDON_SERVICE_MESH", defaultValue = "")
    private String serviceMesh;

    public String getServiceMesh() {
        return serviceMesh;
    }
}
