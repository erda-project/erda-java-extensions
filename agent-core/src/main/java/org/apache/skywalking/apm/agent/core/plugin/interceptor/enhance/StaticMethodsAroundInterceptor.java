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


package org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;

/**
 * The static method's interceptor interface. Any plugin, which wants to intercept static methods, must implement this
 * interface.
 *
 * @author wusheng
 */
public interface StaticMethodsAroundInterceptor {

    void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result);

    /**
     * called after target method invocation. Even method's invocation triggers an exception.
     *
     * @param method
     * @param ret    the method's original return value.
     * @return the method's actual return value.
     */
    Object afterMethod(IMethodInterceptContext context, Object ret);

    /**
     * called when occur exception.
     *
     * @param method
     * @param t      the exception occur.
     */
    void handleMethodException(IMethodInterceptContext context, Throwable t) throws Throwable;
}
