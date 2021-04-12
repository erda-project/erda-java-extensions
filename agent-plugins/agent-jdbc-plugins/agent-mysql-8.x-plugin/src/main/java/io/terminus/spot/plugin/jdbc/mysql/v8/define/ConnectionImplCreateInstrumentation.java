package io.terminus.spot.plugin.jdbc.mysql.v8.define;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.StaticMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static org.apache.skywalking.apm.agent.core.plugin.match.NameMatch.byName;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class ConnectionImplCreateInstrumentation extends AbstractMysqlInstrumentation {

    private static final String JDBC_ENHANCE_CLASS = "com.mysql.cj.jdbc.ConnectionImpl";

    private static final String CONNECT_METHOD = "getInstance";

    @Override
    public StaticMethodsInterceptPoint[] getStaticMethodsInterceptPoints() {
        return new StaticMethodsInterceptPoint[] {
            new StaticMethodsInterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return named(CONNECT_METHOD);
                }

                @Override
                public String getMethodsInterceptor() {
                    return "io.terminus.spot.plugin.jdbc.mysql.v8.define.ConnectionCreateInterceptor";
                }

                @Override
                public boolean isOverrideArgs() {
                    return false;
                }
            }
        };
    }

    @Override
    protected ClassMatch enhanceClass() {
        return byName(JDBC_ENHANCE_CLASS);
    }
}
