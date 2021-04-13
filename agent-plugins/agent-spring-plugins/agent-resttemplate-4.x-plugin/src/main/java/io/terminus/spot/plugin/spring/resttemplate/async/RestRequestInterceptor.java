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

package io.terminus.spot.plugin.spring.resttemplate.async;

import org.apache.skywalking.apm.agent.core.util.Strings;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import cloud.erda.agent.core.utils.TracerUtils;
import io.terminus.spot.plugin.spring.EnhanceCommonInfo;
import org.springframework.http.client.AsyncClientHttpRequest;

import java.util.Map;

public class RestRequestInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        if (!(ret instanceof AsyncClientHttpRequest)) {
            return ret;
        }
        AsyncClientHttpRequest request = (AsyncClientHttpRequest)ret;

        Object obj = context.getInstance().getDynamicField();
        if (!(obj instanceof EnhanceCommonInfo)) {
            return ret;
        }
        EnhanceCommonInfo info = (EnhanceCommonInfo)obj;

        for (Map.Entry<String, String> entry : info.getContext().entrySet()) {
            if (Strings.isEmpty(entry.getKey())) {
                continue;
            }
            if (Strings.isEmpty(entry.getValue())) {
                request.getHeaders().remove(entry.getKey());
                continue;
            }
            request.getHeaders().set(entry.getKey(), entry.getValue());
        }
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        TracerUtils.handleException(t);
    }
}

