package cloud.erda.agent.plugin.method.defines;

import cloud.erda.agent.plugin.method.InterceptPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.StaticMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassStaticMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.apache.skywalking.apm.agent.core.plugin.match.NameMatch.byName;

/**
 * @author liuhaoyang
 * @date 2021/5/10 15:37
 */
public class StaticMethodInterceptInstrumentation extends ClassStaticMethodsEnhancePluginDefine {

    private static final String INTERCEPT_CLASS = "cloud.erda.agent.plugin.method.interceptors.StaticMethodInterceptor";

    private InterceptPoint interceptPoint;

    public StaticMethodInterceptInstrumentation(InterceptPoint interceptPoint) {
        this.interceptPoint = interceptPoint;
    }

    @Override
    protected ClassMatch enhanceClass() {
        return byName(interceptPoint.getClassName());
    }

    @Override
    protected StaticMethodsInterceptPoint[] getStaticMethodsInterceptPoints() {
        String[] methodNames = interceptPoint.getMethodNames();
        StaticMethodsInterceptPoint[] staticMethodsInterceptPoints = new StaticMethodsInterceptPoint[methodNames.length];
        for (int i = 0; i < methodNames.length; i++) {
            int index = i;
            staticMethodsInterceptPoints[i] = new StaticMethodsInterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return named(methodNames[index]);
                }

                @Override
                public String getMethodsInterceptor() {
                    return INTERCEPT_CLASS;
                }

                @Override
                public boolean isOverrideArgs() {
                    return true;
                }
            };
        }
        return staticMethodsInterceptPoints;
    }
}
