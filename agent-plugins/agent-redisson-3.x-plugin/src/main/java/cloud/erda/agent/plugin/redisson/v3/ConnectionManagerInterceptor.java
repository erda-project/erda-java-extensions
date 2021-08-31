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


package cloud.erda.agent.plugin.redisson.v3;

import cloud.erda.agent.core.utils.ReflectUtils;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.redisson.config.Config;
import org.redisson.connection.ConnectionManager;

import java.net.URI;
import java.util.Collection;

/**
 * Reference from https://github.com/apache/skywalking-java/blob/main/apm-sniffer/apm-sdk-plugin/redisson-3.x-plugin/src/main/java/org/apache/skywalking/apm/plugin/redisson/v3/ConnectionManagerInterceptor.java
 */
public class ConnectionManagerInterceptor implements InstanceMethodsAroundInterceptor {


    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {

    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        ConnectionManager connectionManager = (ConnectionManager) context.getInstance();
        Config config = connectionManager.getCfg();

        Object singleServerConfig = ReflectUtils.getObjectField(config, "singleServerConfig");
        Object sentinelServersConfig = ReflectUtils.getObjectField(config, "sentinelServersConfig");
        Object masterSlaveServersConfig = ReflectUtils.getObjectField(config, "masterSlaveServersConfig");
        Object clusterServersConfig = ReflectUtils.getObjectField(config, "clusterServersConfig");
        Object replicatedServersConfig = ReflectUtils.getObjectField(config, "replicatedServersConfig");

        StringBuilder peer = new StringBuilder();
        EnhancedInstance retInst = (EnhancedInstance) ret;

        if (singleServerConfig != null) {
            Object singleAddress = ReflectUtils.getObjectField(singleServerConfig, "address");
            peer.append(getPeer(singleAddress));
            retInst.setDynamicField(peer.toString());
            return ret;
        }
        if (sentinelServersConfig != null) {
            appendAddresses(peer, (Collection) ReflectUtils.getObjectField(sentinelServersConfig, "sentinelAddresses"));
            retInst.setDynamicField(peer.toString());
            return ret;
        }
        if (masterSlaveServersConfig != null) {
            Object masterAddress = ReflectUtils.getObjectField(masterSlaveServersConfig, "masterAddress");
            peer.append(getPeer(masterAddress));
            appendAddresses(peer, (Collection) ReflectUtils.getObjectField(masterSlaveServersConfig, "slaveAddresses"));
            retInst.setDynamicField(peer.toString());
            return ret;
        }
        if (clusterServersConfig != null) {
            appendAddresses(peer, (Collection) ReflectUtils.getObjectField(clusterServersConfig, "nodeAddresses"));
            retInst.setDynamicField(peer.toString());
            return ret;
        }
        if (replicatedServersConfig != null) {
            appendAddresses(peer, (Collection) ReflectUtils.getObjectField(replicatedServersConfig, "nodeAddresses"));
            retInst.setDynamicField(peer.toString());
            return ret;
        }

        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {

    }

    private void appendAddresses(StringBuilder peer, Collection nodeAddresses) {
        if (nodeAddresses != null && !nodeAddresses.isEmpty()) {
            for (Object uri : nodeAddresses) {
                peer.append(getPeer(uri)).append(";");
            }
        }
    }

    private String getPeer(Object obj) {
        if (obj instanceof String) {
            return ((String) obj).replace("redis://", "");
        } else if (obj instanceof URI) {
            URI uri = (URI) obj;
            return uri.getHost() + ":" + uri.getPort();
        } else {
            return null;
        }
    }

}
