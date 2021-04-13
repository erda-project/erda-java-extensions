package org.apache.skywalking.apm.agent.core.util;

/**
 * @author liuhaoyang
 * @since 2019-06-20 11:35
 **/
public class ReflectionUtils {
    public static Object castValue(String value, Class<?> type) {
        if (value == null) {
            return null;
        }
        if (type.equals(int.class)) {
            return Integer.valueOf(value);
        } else if (type.equals(long.class)) {
            return Long.valueOf(value);
        } else if (type.equals(boolean.class)) {
            return Boolean.valueOf(value);
        } else if (type.isEnum()) {
            return Enum.valueOf((Class<Enum>) type, value.toUpperCase());
        } else {
            return value;
        }
    }
}
