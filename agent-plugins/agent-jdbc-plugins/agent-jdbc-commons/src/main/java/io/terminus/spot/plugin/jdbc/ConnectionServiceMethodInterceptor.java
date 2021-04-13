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


package io.terminus.spot.plugin.jdbc;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

/**
 * {@link ConnectionServiceMethodInterceptor} create an exit span when the following methods execute:
 * 1. close
 * 2. rollback
 * 3. releaseSavepoint
 * 4. commit
 *
 * @author zhangxin
 */
public class ConnectionServiceMethodInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public final void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        // todo 现在并没有用到。请使用mysql插件
    }

    @Override
    public final Object afterMethod(IMethodInterceptContext context,
        Object ret) throws Throwable {
        // todo 现在并没有用到。请使用mysql插件
        return ret;
    }

    @Override public final void handleMethodException(IMethodInterceptContext context, Throwable t) {
    }

}
