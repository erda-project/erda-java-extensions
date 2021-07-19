package org.apache.skywalking.apm.agent.core.util;

/**
 * @author liuhaoyang
 * @since 2019-06-20 11:35
 **/
public class ReflectionUtils {
    public static Object castValue(String value, Class<?> type) {
        if (int.class.equals(type)) {
            if (Strings.isEmpty(value)) {
                return 0;
            }
            return Integer.valueOf(value);
        } else if (Integer.class.equals(type)) {
            if (Strings.isEmpty(value)) {
                return null;
            }
            return Integer.valueOf(value);
        } else if (long.class.equals(type)) {
            if (Strings.isEmpty(value)) {
                return 0L;
            }
            return Integer.valueOf(value);
        } else if (Long.class.equals(type)) {
            if (Strings.isEmpty(value)) {
                return null;
            }
            return Long.valueOf(value);
        } else if (boolean.class.equals(type)) {
            if (Strings.isEmpty(value)) {
                return false;
            }
            return Boolean.valueOf(value);
        } else if (Boolean.class.equals(type)) {
            if (Strings.isEmpty(value)) {
                return null;
            }
            return Boolean.valueOf(value);
        } else if (type.isEnum()) {
            return Enum.valueOf((Class<Enum>) type, value.toUpperCase());
        }
        return value;
    }
}
