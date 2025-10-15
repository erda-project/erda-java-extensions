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

package cloud.erda.agent.plugin.spring.resttemplate.sync;

import cloud.erda.agent.core.config.AgentConfig;
import cloud.erda.agent.core.config.loader.ConfigAccessor;
import cloud.erda.agent.core.tracing.Scope;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.TracerUtils;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricBuilder;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricUtils;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.springframework.http.client.ClientHttpResponse;

import java.util.List;

import static cloud.erda.agent.core.utils.Constants.Tags.*;

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
        ClientHttpResponse response = (ClientHttpResponse) allArguments[2];

        TransactionMetricBuilder transactionMetricBuilder =
                TracerManager.currentTracer().context().getAttachment(Constants.Keys.METRIC_BUILDER);
        if (transactionMetricBuilder != null) {
            if (equalsTerminusKey(response.getHeaders().get(Constants.Carriers.RESPONSE_TERMINUS_KEY))) {
                transactionMetricBuilder.tag(PEER_SERVICE_SCOPE, PEER_SERVICE_INTERNAL);
            } else {
                transactionMetricBuilder.tag(PEER_SERVICE_SCOPE, PEER_SERVICE_EXTERNAL);
            }
            TransactionMetricUtils.handleStatusCode(transactionMetricBuilder, response.getRawStatusCode());
        }

        Scope scope = TracerManager.currentTracer().active();
        TracerUtils.handleStatusCode(scope, response.getRawStatusCode());
        return ret;
    }

    private boolean equalsTerminusKey(List<String> tkList) {
        if (tkList == null || tkList.size() == 0) {
            return false;
        }
        String tk = ConfigAccessor.Default.getConfig(AgentConfig.class).terminusKey();
        for (String value : tkList) {
            if (tk.equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        TracerUtils.handleException(t);
    }
}

