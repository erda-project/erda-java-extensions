/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package cloud.erda.agent.plugin.jdbc.mysql.v8.define;

import com.mysql.cj.conf.HostInfo;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.StaticMethodsAroundInterceptor;
import cloud.erda.agent.plugin.jdbc.connectionurl.parser.URLParser;
import cloud.erda.agent.plugin.jdbc.trace.ConnectionInfo;


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
