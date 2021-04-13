package cloud.erda.agent.core.tracing.propagator;

import java.util.Iterator;
import java.util.Map;

/**
 * @author: liuhaoyang
 * @create: 2019-01-07 16:11
 **/
public class TextMapCarrier implements Carrier {

    private final Map<String, String> map;

    public TextMapCarrier(Map<String, String> map) {
        this.map = map;
    }

    @Override
    public void put(String key, String value) {
        map.put(key, value);
    }

    @Override
    public String get(String key) {
        return map.get(key);
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return map.entrySet().iterator();
    }
}
