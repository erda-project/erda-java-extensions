package io.terminus.spot.plugin.servlet;

import cloud.erda.agent.core.tracing.propagator.Carrier;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author: liuhaoyang
 * @create: 2019-01-21 16:02
 **/
public class ServletRequestCarrier implements Carrier {

    private final Map<String, String> map;

    public ServletRequestCarrier(HttpServletRequest request) {
        map = new HashMap<String, String>();
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            map.put(name.toLowerCase(), request.getHeader(name));
        }
    }

    @Override
    public void put(String key, String value) {
        throw new RuntimeException();
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
