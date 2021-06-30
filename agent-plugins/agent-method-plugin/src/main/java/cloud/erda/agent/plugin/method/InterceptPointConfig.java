package cloud.erda.agent.plugin.method;

import cloud.erda.agent.core.config.loader.Config;
import cloud.erda.agent.core.config.loader.Configuration;

/**
 * @author liuhaoyang
 * @date 2021/5/9 18:21
 */
public class InterceptPointConfig implements Config {

    /**
     * {class1}#{method1};{class2}#{method2}
     *
     * example:
     * io.terminus.spot.plugin.method.MethodConfig#getInstanceMethods;io.terminus.spot.plugin.method.MethodConfig#getStaticMethods
     */
    @Configuration(name = "TERMINUS_INTERCEPT_INSTANCE_POINTS", defaultValue = "")
    private String instancePoints;

    /**
     * {class1}#{method1};{class2}#{method2}
     *
     * example:
     * io.terminus.spot.plugin.method.MethodConfig#getStaticMethods
     */
    @Configuration(name = "TERMINUS_INTERCEPT_STATIC_POINTS", defaultValue = "")
    private String staticPoints;

    @Configuration(name = "TERMINUS_INTERCEPT_ATTACH_TRACE", defaultValue = "false")
    private boolean attachTrace;

    public String[] getInstancePoints() {
        if (instancePoints == null) {
            return new String[0];
        }
        return instancePoints.replace(" ", "").split(";");
    }

    public String[] getStaticPoints() {
        if (staticPoints == null) {
            return new String[0];
        }
        return staticPoints.replace(" ", "").split(";");
    }

    public boolean isAttachTrace() {
        return attachTrace;
    }
}