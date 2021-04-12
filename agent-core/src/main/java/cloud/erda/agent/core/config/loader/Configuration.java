package cloud.erda.agent.core.config.loader;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Configuration {
    String name();
    String defaultValue() default "";
}
