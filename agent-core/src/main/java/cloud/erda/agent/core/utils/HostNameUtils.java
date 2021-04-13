package cloud.erda.agent.core.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class HostNameUtils {

    private static final String hostname = getHostnameInternal();

    public static String getHostname(){
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
