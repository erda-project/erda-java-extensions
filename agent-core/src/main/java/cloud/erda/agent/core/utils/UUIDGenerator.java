package cloud.erda.agent.core.utils;

import java.util.UUID;

public class UUIDGenerator {

    public static String New()
    {
        return UUID.randomUUID().toString();
    }
}
