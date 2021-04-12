package cloud.erda.agent.core.config;

import cloud.erda.agent.core.config.loader.Config;
import cloud.erda.agent.core.config.loader.Configuration;
import org.apache.skywalking.apm.agent.core.util.Strings;

/**
 * @author: liuhaoyang
 * @create: 2020-02-19 14:34
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
