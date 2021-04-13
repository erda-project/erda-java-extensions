package io.terminus.spot.plugin.jdbc.mysql.v8.define;

import com.mysql.cj.conf.HostInfo;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.StaticMethodsAroundInterceptor;
import io.terminus.spot.plugin.jdbc.connectionurl.parser.URLParser;
import io.terminus.spot.plugin.jdbc.trace.ConnectionInfo;


public class ConnectionCreateInterceptor implements StaticMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) {

    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) {
        if (ret instanceof EnhancedInstance) {
            HostInfo hostInfo = (HostInfo) context.getArguments()[0];
            ConnectionInfo connectionInfo = URLParser.parser(hostInfo.getDatabaseUrl(), hostInfo.getHostPortPair());
            ((EnhancedInstance) ret).setDynamicField(connectionInfo);
        }
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) throws Throwable {

    }
}
