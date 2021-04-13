package cloud.erda.agent.plugin.log.pattern;

public class PatternStrings {

    public static final String REQUEST_ID = "requestid";

    public static final String SPAN_ID = "spanid";

    public static final String SERVICE = "service";

    public static final String TAGS = "tags";

    public static final String PATTERN
        = "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%tags] - [%thread] %-40.40logger{39}: %msg%n";

    public static final String TAG_SEPARATOR = ",";
    public static final String KV_SEPARATOR = "=";

}