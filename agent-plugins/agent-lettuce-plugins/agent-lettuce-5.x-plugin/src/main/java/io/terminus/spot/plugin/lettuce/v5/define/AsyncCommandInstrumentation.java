package io.terminus.spot.plugin.lettuce.v5.define;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static org.apache.skywalking.apm.agent.core.plugin.bytebuddy.ArgumentTypeNameMatch.takesArgumentWithType;
import static org.apache.skywalking.apm.agent.core.plugin.match.NameMatch.byName;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class AsyncCommandInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {

    private static final String ENHANCE_CLASS = "io.lettuce.core.protocol.AsyncCommand";

    private static final String ASYNC_COMMAND_METHOD_INTERCEPTOR = "io.terminus.spot.plugin.lettuce.v5.AsyncCommandMethodInterceptor";

    @Override
    public ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[0];
    }

    @Override
    public InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[] {
            new InstanceMethodsInterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return (named("onComplete").and(takesArgumentWithType(0, "java.util.function.Consumer"))).or(named("onComplete")
                        .and(takesArgumentWithType(0, "java.util.function.BiConsumer")));
                }

                @Override
                public String getMethodsInterceptor() {
                    return ASYNC_COMMAND_METHOD_INTERCEPTOR;
                }

                @Override
                public boolean isOverrideArgs() {
                    return true;
                }
            }
        };
    }

    @Override
    public ClassMatch enhanceClass() {
        return byName(ENHANCE_CLASS);
    }
}
