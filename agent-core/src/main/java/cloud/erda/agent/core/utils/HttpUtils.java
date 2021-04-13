package cloud.erda.agent.core.utils;

import org.apache.skywalking.apm.agent.core.util.Strings;

/**
 * @author randomnil
 */
public class HttpUtils {

    public static int getPort(int port, String schema) {
        return port > 0 ? port : "https".equalsIgnoreCase(schema) ? 443 : 80;
    }

    public static String getPath(String path) {
        return Strings.isEmpty(path) ? "/" : path;
    }
}
