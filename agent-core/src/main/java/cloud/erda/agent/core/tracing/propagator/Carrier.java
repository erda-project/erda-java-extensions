package cloud.erda.agent.core.tracing.propagator;

import java.util.Map;

/**
 * @author: liuhaoyang
 * @create: 2019-01-07 16:08
 **/
public interface Carrier extends Iterable<Map.Entry<String, String>> {
    void put(String key, String value);

    String get(String key);
}
