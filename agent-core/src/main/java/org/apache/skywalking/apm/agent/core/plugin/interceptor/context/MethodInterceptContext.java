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


package org.apache.skywalking.apm.agent.core.plugin.interceptor.context;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MethodInterceptContext implements IMethodInterceptContext {
    private Map<String, Object> attachments;
    private EnhancedInstance instance;
    private Class<?> originClass;
    private Method method;
    private Object[] arguments;
    private Class<?>[] argumentsTypes;
    private Throwable exception;

    public MethodInterceptContext(EnhancedInstance objInst, Class<?> originClass, Method method, Object[] allArguments,
        Class<?>[] argumentsTypes) {
        this.instance = objInst;
        this.originClass = originClass;
        this.method = method;
        this.arguments = allArguments;
        this.argumentsTypes = argumentsTypes;
    }

    @Override
    public EnhancedInstance getInstance() {
        return instance;
    }

    @Override public Class<?> getOriginClass() {
        return originClass;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public Class<?>[] getArgumentsTypes() {
        return argumentsTypes;
    }

    @Override
    public <T> T getAttachment(String key) {
        ensureAttachments();
        return (T)attachments.get(key);
    }

    @Override
    public <T> void setAttachment(String key, T value) {
        ensureAttachments();
        attachments.put(key, value);
    }

    @Override
    public void clearAttachments() {
        if (attachments != null) {
            attachments.clear();
        }
    }

    @Override public void forceThrowException(Throwable exception) {
        this.exception = exception;
    }

    @Override public Throwable getException() {
        return this.exception;
    }

    private void ensureAttachments() {
        if (attachments == null) {
            attachments = new HashMap<String, Object>();
        }
    }
}
