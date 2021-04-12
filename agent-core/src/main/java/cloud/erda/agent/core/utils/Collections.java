package cloud.erda.agent.core.utils;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * @author liuhaoyang 2020/3/22 14:25
 */
public class Collections {

    public static <E> boolean IsNullOrEmpty(Collection<E> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <E> boolean IsNullOrEmpty(E[] array) {
        return array == null || array.length == 0;
    }

    public static boolean IsNullOrEmpty(double[] array) {
        return array == null || array.length == 0;
    }
}
