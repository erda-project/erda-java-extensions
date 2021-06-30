package cloud.erda.agent.plugin.method.defines;

import cloud.erda.agent.plugin.method.InterceptPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.apache.skywalking.apm.agent.core.plugin.match.NameMatch.byName;

/**
 * @author liuhaoyang
 * @date 2021/5/10 15:34
 */
public class InstanceMethodInterceptInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {

    private static final String INTERCEPT_CLASS = "cloud.erda.agent.plugin.method.interceptors.InstanceMethodInterceptor";

    private InterceptPoint interceptPoint;

    public InstanceMethodInterceptInstrumentation(InterceptPoint interceptPoint) {
        this.interceptPoint = interceptPoint;
    }

    @Override
    protected ClassMatch enhanceClass() {
        return byName(interceptPoint.getClassName());
    }

    @Override
    protected ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[0];
    }

    @Override
    protected InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        String[] methodNames = interceptPoint.getMethodNames();
        InstanceMethodsInterceptPoint[] instanceMethodsInterceptPoints = new InstanceMethodsInterceptPoint[methodNames.length];
        for (int i = 0; i < methodNames.length; i++) {
            int index = i;
            instanceMethodsInterceptPoints[i] = new InstanceMethodsInterceptPoint() {
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
        return instanceMethodsInterceptPoints;
    }
}
