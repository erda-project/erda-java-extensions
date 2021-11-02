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

import cloud.erda.agent.core.tracing.Scope;
import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.propagator.TextMapCarrier;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.HttpUtils;
import cloud.erda.agent.core.utils.TracerUtils;
import cloud.erda.agent.plugin.app.insight.MetricReporter;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricBuilder;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricContext;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricUtils;
import cloud.erda.agent.plugin.spring.EnhanceCommonInfo;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.DynamicFieldEnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class RestExecuteInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        Object[] allArguments = context.getArguments();
        if (allArguments == null || allArguments.length < 2
                || !(allArguments[0] instanceof URI) || !(allArguments[1] instanceof HttpMethod)) {
            return;
        }
        URI uri = (URI) allArguments[0];
        HttpMethod method = (HttpMethod) allArguments[1];

        int port = HttpUtils.getPort(uri.getPort(), uri.getScheme());
        String path = HttpUtils.getPath(uri.getPath());
        String peerHost = uri.getHost();
        if (port > 0) {
            peerHost += ":" + port;
        }
        String peerAddress = uri.getScheme() + "://" + peerHost;

        Tracer tracer = TracerManager.currentTracer();
        SpanContext spanContext = tracer.active() != null ? tracer.active().span().getContext() : null;
        Span span = tracer.buildSpan(path).childOf(spanContext).startActive().span();
        span.tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_REST_TEMPLATE);
        span.tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_CLIENT);
        span.tag(Constants.Tags.SPAN_LAYER, Constants.Tags.SPAN_LAYER_HTTP);
        span.tag(Constants.Tags.PEER_ADDRESS, peerAddress);
        span.tag(Constants.Tags.PEER_HOSTNAME, peerHost);
        span.tag(Constants.Tags.PEER_PORT, String.valueOf(port));
        span.tag(Constants.Tags.HTTP_URL, uri.toString());
        span.tag(Constants.Tags.HTTP_PATH, path);
        span.tag(Constants.Tags.HTTP_METHOD, method.name().toUpperCase());

        span.getContext().getBaggage().putAll(TransactionMetricContext.instance);
        Map<String, String> map = new HashMap<String, String>(16);
        TextMapCarrier carrier = new TextMapCarrier(map);
        tracer.inject(span.getContext(), carrier);

        EnhanceCommonInfo info = new EnhanceCommonInfo();
        info.setContext(map);
        ((DynamicFieldEnhancedInstance)context.getInstance()).setDynamicField(info);

        TransactionMetricBuilder transactionMetricBuilder = TransactionMetricUtils.createHttpMetric(peerHost);
        tracer.context().setAttachment(Constants.Keys.METRIC_BUILDER, transactionMetricBuilder);
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        Object[] allArguments = context.getArguments();
        if (allArguments == null || allArguments.length < 2
                || !(allArguments[0] instanceof URI) || !(allArguments[1] instanceof HttpMethod)) {
            return ret;
        }

        TransactionMetricBuilder transactionMetricBuilder =
                TracerManager.currentTracer().context().getAttachment(Constants.Keys.METRIC_BUILDER);
        if (transactionMetricBuilder != null) {
            MetricReporter.report(transactionMetricBuilder);
        }

        Scope scope = TracerManager.currentTracer().active();
        if (scope != null) {
            scope.close();
        }
        return ret;

    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        TracerUtils.handleException(t);
    }
}
