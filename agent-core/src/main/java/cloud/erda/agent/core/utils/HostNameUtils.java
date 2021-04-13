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

package cloud.erda.agent.core.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class HostNameUtils {

    private static final String hostname = getHostnameInternal();

    public static String getHostname() {
        return hostname;
    }

    private static String getHostnameInternal() {
        String host = null;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException("解析HostName失败。");
        }
        Integer length = host.length();
        if (length > 16) {
            return host.substring(0, 16);
        }
        if (length == 16) {
            return host;
        }
        char[] chars = new char[16];
        char[] hostChars = host.toCharArray();
        Integer complement = 16 - length;
        for (Integer i = 15; i >= 0; i--) {
            if (i - complement >= 0) {
                chars[i] = hostChars[i - complement];
            } else {
                chars[i] = '_';
            }
        }
        return new String(chars);
    }
}
