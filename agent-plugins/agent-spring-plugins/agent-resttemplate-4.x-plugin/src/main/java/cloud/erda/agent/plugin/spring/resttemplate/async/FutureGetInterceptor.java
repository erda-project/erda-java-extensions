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

package cloud.erda.agent.plugin.spring.resttemplate.async;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import cloud.erda.agent.core.tracing.Scope;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.TracerUtils;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricBuilder;
import cloud.erda.agent.plugin.app.insight.MetricReporter;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricUtils;
import cloud.erda.agent.plugin.spring.EnhanceCommonInfo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

public class FutureGetInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        Object obj = context.getInstance().getDynamicField();
        if (!(obj instanceof EnhanceCommonInfo)) {
            return;
        }
        EnhanceCommonInfo info = (EnhanceCommonInfo) obj;

        Tracer tracer = TracerManager.tracer();
        tracer.attach(info.getSnapshot());
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        Object obj = context.getInstance().getDynamicField();
        if (!(obj instanceof EnhanceCommonInfo)) {
            return ret;
        }
        EnhanceCommonInfo info = (EnhanceCommonInfo) obj;

        if (!(ret instanceof ResponseEntity)) {
            return ret;
        }
        ResponseEntity response = (ResponseEntity) ret;

        TransactionMetricBuilder transactionMetricBuilder = info.getAppMetricBuilder();
        if (transactionMetricBuilder != null) {
            HttpHeaders headers = response.getHeaders();
            if (headers == null || CollectionUtils.isEmpty(headers.get(Constants.Carriers.RESPONSE_TERMINUS_KEY))) {
                TransactionMetricUtils.handleStatusCode(transactionMetricBuilder, response.getStatusCodeValue());
                MetricReporter.report(transactionMetricBuilder);
            }
        }

        Scope scope = TracerManager.tracer().active();
        if (scope != null) {
            TracerUtils.handleStatusCode(scope, response.getStatusCodeValue());
            scope.close();
        }
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        Object obj = context.getInstance().getDynamicField();
        if (!(obj instanceof EnhanceCommonInfo)) {
            return;
        }
        TracerUtils.handleException(t);
    }
}
