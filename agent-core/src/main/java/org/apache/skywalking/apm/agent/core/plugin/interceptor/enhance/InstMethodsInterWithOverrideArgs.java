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

import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.MethodInterceptContext;
import net.bytebuddy.implementation.bind.annotation.*;
import org.apache.skywalking.apm.agent.core.plugin.PluginException;
import org.apache.skywalking.apm.agent.core.plugin.loader.InterceptorInstanceLoader;

import java.lang.reflect.Method;

/**
 * The actual byte-buddy's interceptor to intercept class instance methods. In this class, it provide a bridge between
 * byte-buddy and sky-walking plugin.
 *
 * @author wusheng
 */
public class InstMethodsInterWithOverrideArgs {
    private static final ILog logger = LogManager.getLogger(InstMethodsInterWithOverrideArgs.class);

    /**
     * An {@link InstanceMethodsAroundInterceptor} This name should only stay in {@link String}, the real {@link Class}
     * type will trigger classloader failure. If you want to know more, please check on books about Classloader or
     * Classloader appointment mechanism.
     */
    private InstanceMethodsAroundInterceptor interceptor;

    /**
     * @param instanceMethodsAroundInterceptorClassName class full name.
     */
    public InstMethodsInterWithOverrideArgs(String instanceMethodsAroundInterceptorClassName, ClassLoader classLoader) {
        try {
            interceptor = InterceptorInstanceLoader.load(instanceMethodsAroundInterceptorClassName, classLoader);
        } catch (Throwable t) {
            throw new PluginException("Can't create InstanceMethodsAroundInterceptor.", t);
        }
    }

    /**
     * Intercept the target instance method.
     *
     * @param obj          target class instance.
     * @param allArguments all method arguments
     * @param method       method description.
     * @param zuper        the origin call ref.
     * @return the return value of target instance method.
     * @throws Exception only throw exception because of zuper.call() or unexpected exception in sky-walking ( This is a
     *                   bug, if anything triggers this condition ).
     */
    @RuntimeType
    public Object intercept(@This Object obj,
                            @Origin Class<?> clazz,
                            @AllArguments Object[] allArguments,
                            @Origin Method method,
                            @Morph OverrideCallable zuper
    ) throws Throwable {
//        EnhancedInstance targetObject = (EnhancedInstance) obj;
        IMethodInterceptContext context = new MethodInterceptContext(obj, clazz, method, allArguments, method.getParameterTypes());
        MethodInterceptResult result = new MethodInterceptResult();
        Object ret = null;

        try {

            try {
                interceptor.beforeMethod(context, result);
            } catch (Throwable t) {
                logger.error(t, "class[{}] before method[{}] intercept failure", obj.getClass(), method.getName());
            }

            if (!result.isContinue()) {
                ret = result._ret();
            }

            Throwable ex = context.getException();
            if (ex != null) {
                context.forceThrowException(null);
                throw ex;
            }

            try {
                if (result.isContinue()) {
                    ret = zuper.call(context.getArguments());
                }
            } catch (Throwable t) {
                try {
                    interceptor.handleMethodException(context, t);
                } catch (Throwable t2) {
                    logger.error(t2, "class[{}] handle method[{}] exception failure", obj.getClass(), method.getName());
                }
                throw t;
            }
        } finally {
            try {
                ret = interceptor.afterMethod(context, ret);
                context.clearAttachments();
            } catch (Throwable t) {
                logger.error(t, "class[{}] after method[{}] intercept failure. [{}]", obj.getClass(), method.getName(), t.getMessage());
            }
        }
        return ret;
    }
}
