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
 */

package cloud.erda.agent.plugin.httpasyncclient.v4;

import org.apache.skywalking.apm.agent.core.util.Strings;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.propagator.TextMapCarrier;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.TracerUtils;
import cloud.erda.agent.plugin.app.insight.AppMetricBuilder;
import cloud.erda.agent.plugin.app.insight.AppMetricContext;
import cloud.erda.agent.plugin.app.insight.AppMetricUtils;
import org.apache.http.HttpHost;
import org.apache.http.RequestLine;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.protocol.HttpClientContext;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static cloud.erda.agent.core.utils.Constants.Tags.*;

/**
 * the actual point request begin fetch the request from thread local .
 *
 * @author lican
 */
public class HttpAsyncRequestExecutorInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        ThreadTransferInfo info = ThreadTransferInfo.LOCAL.get();
        ThreadTransferInfo.LOCAL.remove();
        if (info == null) {
            return;
        }

        HttpRequestWrapper requestWrapper =
                (HttpRequestWrapper) info.getHttpContext().getAttribute(HttpClientContext.HTTP_REQUEST);
        HttpHost httpHost = (HttpHost) info.getHttpContext().getAttribute(HttpClientContext.HTTP_TARGET_HOST);
        RequestLine requestLine = requestWrapper.getRequestLine();
        String uri = requestLine.getUri();
        String operationName = uri.startsWith("http") ? new URL(uri).getPath() : uri;
        String hostname = httpHost.getHostName();
        if (httpHost.getPort() > 0) {
            hostname += ":" + httpHost.getPort();
        }

        Tracer tracer = TracerManager.tracer();
        tracer.attach(info.getSnapshot());
        SpanContext spanContext = tracer.active() != null && tracer.active().span() != null
                ? tracer.active().span().getContext() : null;
        Span span = tracer.buildSpan("Async " + operationName).childOf(spanContext).startActive().span();
        span.tag(COMPONENT, COMPONENT_HTTPASYNCCLIENT);
        span.tag(SPAN_KIND, SPAN_KIND_CLIENT);
        span.tag(SPAN_LAYER, SPAN_LAYER_HTTP);
        span.tag(PEER_ADDRESS, httpHost.getSchemeName() + "://" + hostname);
        span.tag(PEER_HOSTNAME, hostname);
        span.tag(PEER_PORT, String.valueOf(httpHost.getPort()));
        span.tag(HTTP_URL, requestWrapper.getOriginal().getRequestLine().getUri());
        span.tag(HTTP_METHOD, requestLine.getMethod());

        tracer.context().put(AppMetricContext.instance);
        Map<String, String> map = new HashMap<String, String>(16);
        TextMapCarrier carrier = new TextMapCarrier(map);
        tracer.inject(span.getContext(), carrier);

        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (Strings.isEmpty(entry.getKey())) {
                continue;
            }
            if (Strings.isEmpty(entry.getValue())) {
                requestWrapper.removeHeaders(entry.getKey());
                continue;
            }
            requestWrapper.setHeader(entry.getKey(), entry.getValue());
        }

        AppMetricBuilder appMetricBuilder = AppMetricUtils.createHttpMetric(hostname);
        tracer.context().setAttachment(Constants.Keys.METRIC_BUILDER, appMetricBuilder);
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        TracerUtils.handleException(t);
    }
}
