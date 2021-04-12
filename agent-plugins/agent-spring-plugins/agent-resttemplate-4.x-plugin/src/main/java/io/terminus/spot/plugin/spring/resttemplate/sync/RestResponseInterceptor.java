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

package io.terminus.spot.plugin.spring.resttemplate.sync;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import cloud.erda.agent.core.tracing.Scope;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.TracerUtils;
import cloud.erda.plugin.app.insight.AppMetricBuilder;
import cloud.erda.plugin.app.insight.AppMetricUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.CollectionUtils;

public class RestResponseInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        Object[] allArguments = context.getArguments();
        if (allArguments == null || allArguments.length < 3 || !(allArguments[2] instanceof ClientHttpResponse)) {
            return ret;
        }
        ClientHttpResponse response = (ClientHttpResponse)allArguments[2];

        AppMetricBuilder appMetricBuilder =
            TracerManager.tracer().context().getAttachment(Constants.Keys.METRIC_BUILDER);
        if (appMetricBuilder != null) {
            HttpHeaders headers = response.getHeaders();
            if (headers == null || CollectionUtils.isEmpty(headers.get(Constants.Carriers.RESPONSE_TERMINUS_KEY))) {
                AppMetricUtils.handleStatusCode(appMetricBuilder, response.getStatusCode().value());
            } else {
                TracerManager.tracer().context().setAttachment(Constants.Keys.METRIC_BUILDER, null);
            }
        }

        Scope scope = TracerManager.tracer().active();
        TracerUtils.handleStatusCode(scope, response.getStatusCode().value());
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        TracerUtils.handleException(t);
    }
}

