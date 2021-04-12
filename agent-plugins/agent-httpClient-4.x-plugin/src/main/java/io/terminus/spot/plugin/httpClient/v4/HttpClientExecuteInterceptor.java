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

package io.terminus.spot.plugin.httpClient.v4;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.agent.core.util.Strings;
import cloud.erda.agent.core.tracing.Scope;
import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.propagator.TextMapCarrier;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.TracerUtils;
import cloud.erda.plugin.app.insight.AppMetricBuilder;
import cloud.erda.plugin.app.insight.AppMetricContext;
import cloud.erda.plugin.app.insight.AppMetricRecorder;
import cloud.erda.plugin.app.insight.AppMetricUtils;
import org.apache.http.*;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static cloud.erda.agent.core.utils.Constants.Tags.*;


public class HttpClientExecuteInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        Object[] allArguments = context.getArguments();
        if (allArguments[0] == null || allArguments[1] == null) {
            // illegal args, can't trace. ignore.
            return;
        }

        final HttpHost httpHost = (HttpHost) allArguments[0];
        HttpRequest httpRequest = (HttpRequest) allArguments[1];
        URL url = new URL(httpRequest.getRequestLine().getUri());
        String hostname = httpHost.getHostName();
        if (httpHost.getPort() > 0) {
            hostname += ":" + httpHost.getPort();
        }

        Tracer tracer = TracerManager.tracer();
        SpanContext spanContext = tracer.active() != null ? tracer.active().span().getContext() : null;
        Span span = tracer.buildSpan(url.getPath()).childOf(spanContext).startActive().span();
        span.tag(COMPONENT, COMPONENT_HTTPCLIENT);
        span.tag(SPAN_KIND, SPAN_KIND_CLIENT);
        span.tag(SPAN_LAYER, SPAN_LAYER_HTTP);
        span.tag(PEER_ADDRESS, httpHost.getSchemeName() + "://" + hostname);
        span.tag(PEER_HOSTNAME, hostname);
        span.tag(PEER_PORT, String.valueOf(httpHost.getPort()));
        span.tag(HTTP_URL, httpRequest.getRequestLine().getUri());
        span.tag(HTTP_METHOD, httpRequest.getRequestLine().getMethod());

        tracer.context().put(AppMetricContext.instance);
        Map<String, String> map = new HashMap<String, String>(16);
        TextMapCarrier carrier = new TextMapCarrier(map);
        tracer.inject(span.getContext(), carrier);

        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (Strings.isEmpty(entry.getKey())) {
                continue;
            }
            if (Strings.isEmpty(entry.getValue())) {
                httpRequest.removeHeaders(entry.getKey());
                continue;
            }
            httpRequest.setHeader(entry.getKey(), entry.getValue());
        }

        AppMetricBuilder appMetricBuilder = AppMetricUtils.createHttpMetric(hostname);
        context.setAttachment(Constants.Keys.METRIC_BUILDER, appMetricBuilder);
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) {
        Object[] allArguments = context.getArguments();
        if (allArguments[0] == null || allArguments[1] == null) {
            return ret;
        }
        if (!(ret instanceof HttpResponse)) {
            return ret;
        }
        HttpResponse response = (HttpResponse) ret;

        AppMetricBuilder appMetricBuilder = context.getAttachment(Constants.Keys.METRIC_BUILDER);
        if (appMetricBuilder != null) {
            this.recordResponseAppMetric(appMetricBuilder, response);
        }

        Scope scope = TracerManager.tracer().active();
        if (scope != null) {
            this.wrapResponseSpan(scope.span(), response);
            scope.close();
        }
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        TracerUtils.handleException(t);
    }

    private void recordResponseAppMetric(AppMetricBuilder appMetricBuilder, HttpResponse response) {
        Header[] headers = response.getHeaders(Constants.Carriers.RESPONSE_TERMINUS_KEY);
        if (headers != null && headers.length > 0) {
            return;
        }

        StatusLine statusLine = response.getStatusLine();
        if (statusLine == null) {
            return;
        }
        int statusCode = statusLine.getStatusCode();
        AppMetricUtils.handleStatusCode(appMetricBuilder, statusCode);
        AppMetricRecorder.record(appMetricBuilder);
    }

    private void wrapResponseSpan(Span span, HttpResponse response) {
        StatusLine statusLine = response.getStatusLine();
        if (statusLine == null) {
            return;
        }
        int statusCode = statusLine.getStatusCode();
        if (statusCode >= 400) {
            span.tag(ERROR, ERROR_TRUE);
        }
        span.tag(HTTP_STATUS, String.valueOf(statusCode));
    }
}
