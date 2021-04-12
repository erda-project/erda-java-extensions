package cloud.erda.agent.core.utils;

public class DateTimeUtils {

    public static long currentTimeNano() {
        Long currentTime = System.currentTimeMillis() * 1000000;
        Long nanoTime = System.nanoTime();
        return currentTime + (nanoTime - nanoTime / 1000000 * 1000000);
    }
}
