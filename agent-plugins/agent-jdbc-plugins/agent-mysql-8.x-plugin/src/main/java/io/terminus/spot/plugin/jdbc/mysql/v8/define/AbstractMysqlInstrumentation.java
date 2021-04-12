package io.terminus.spot.plugin.jdbc.mysql.v8.define;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.StaticMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import io.terminus.spot.plugin.jdbc.define.Constants;

/**
 * @author randomnil
 */
public abstract class AbstractMysqlInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {

    @Override
    protected ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return null;
    }

    @Override
    protected StaticMethodsInterceptPoint[] getStaticMethodsInterceptPoints() {
        return null;
    }

    @Override
    protected InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return null;
    }

    @Override
    protected String[] witnessClasses() {
        return new String[]{Constants.WITNESS_MYSQL_8X_CLASS};
    }
}
