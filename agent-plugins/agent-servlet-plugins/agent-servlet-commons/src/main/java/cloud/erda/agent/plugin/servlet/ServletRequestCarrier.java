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

package cloud.erda.agent.plugin.servlet;

import cloud.erda.agent.core.tracing.propagator.Carrier;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author liuhaoyang
 * @since 2019-01-21 16:02
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
