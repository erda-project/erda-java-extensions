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

/**
 * The writeAndFlush method is used in versions lower than 5.0.2.RELEASE
 */
public class RedisChannelWriterInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {

    private static final String ENHANCE_CLASS = "io.lettuce.core.protocol.DefaultEndpoint";

    private static final String REDIS_CHANNEL_WRITER_INTERCEPTOR_CLASS = "io.terminus.spot.plugin.lettuce.v5.RedisChannelWriterInterceptor";

    @Override
    public ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[] {
            new ConstructorInterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getConstructorMatcher() {
                    return takesArgumentWithType(0, "io.lettuce.core.ClientOptions");
                }

                @Override
                public String getConstructorInterceptor() {
                    return REDIS_CHANNEL_WRITER_INTERCEPTOR_CLASS;
                }
            }
        };
    }

    @Override
    public InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[] {
            new InstanceMethodsInterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return named("writeToChannelAndFlush").or(named("writeAndFlush"));
                }

                @Override
                public String getMethodsInterceptor() {
                    return REDIS_CHANNEL_WRITER_INTERCEPTOR_CLASS;
                }

                @Override
                public boolean isOverrideArgs() {
                    return false;
                }
            }
        };
    }

    @Override
    public ClassMatch enhanceClass() {
        return byName(ENHANCE_CLASS);
    }
}
