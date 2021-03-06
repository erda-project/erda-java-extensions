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

package cloud.erda.agent.plugin.httpClient.v4;

import cloud.erda.agent.core.config.AgentConfig;
import cloud.erda.agent.core.config.loader.ConfigAccessor;
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
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricBuilder;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricContext;
import cloud.erda.agent.plugin.app.insight.MetricReporter;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricUtils;
import org.apache.http.*;

import java.net.URL;
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

        Tracer tracer = TracerManager.currentTracer();
        SpanContext spanContext = tracer.active() != null ? tracer.active().span().getContext() : null;
        Span span = tracer.buildSpan("HTTP " + httpRequest.getRequestLine().getMethod() + " " + url.getPath()).childOf(spanContext).startActive().span();
        span.tag(COMPONENT, COMPONENT_HTTPCLIENT);
        span.tag(SPAN_KIND, SPAN_KIND_CLIENT);
        span.tag(SPAN_LAYER, SPAN_LAYER_HTTP);
        span.tag(PEER_ADDRESS, httpHost.getSchemeName() + "://" + hostname);
        span.tag(PEER_HOSTNAME, hostname);
        span.tag(PEER_PORT, String.valueOf(httpHost.getPort()));
        span.tag(HTTP_URL, httpRequest.getRequestLine().getUri());
        span.tag(HTTP_PATH, url.getPath());
        span.tag(HTTP_METHOD, httpRequest.getRequestLine().getMethod());

        span.getContext().getBaggage().putAll(TransactionMetricContext.instance);

        TextMapCarrier carrier = new TextMapCarrier();
        tracer.inject(span.getContext(), carrier);

        for (Map.Entry<String, String> entry : carrier) {
            if (Strings.isEmpty(entry.getKey())) {
                continue;
            }
            if (Strings.isEmpty(entry.getValue())) {
                httpRequest.removeHeaders(entry.getKey());
                continue;
            }
            httpRequest.setHeader(entry.getKey(), entry.getValue());
        }

        TransactionMetricBuilder transactionMetricBuilder = TransactionMetricUtils.createHttpMetric(hostname);
        transactionMetricBuilder.tag(SPAN_KIND, Constants.Tags.SPAN_KIND_CLIENT)
                .tag(COMPONENT, Constants.Tags.COMPONENT_HTTPCLIENT)
                .tag(PEER_ADDRESS, httpHost.getSchemeName() + "://" + hostname)
                .tag(PEER_PORT, String.valueOf(httpHost.getPort()))
                .tag(HTTP_URL, httpRequest.getRequestLine().getUri())
                .tag(HTTP_PATH, url.getPath())
                .tag(HTTP_METHOD, httpRequest.getRequestLine().getMethod().toUpperCase())
                .tag(PEER_HOSTNAME, hostname);
        context.setAttachment(Constants.Keys.METRIC_BUILDER, transactionMetricBuilder);
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

        TransactionMetricBuilder transactionMetricBuilder = context.getAttachment(Constants.Keys.METRIC_BUILDER);
        if (transactionMetricBuilder != null) {
            this.recordResponseAppMetric(transactionMetricBuilder, response);
        }

        Scope scope = TracerManager.currentTracer().active();
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

    private void recordResponseAppMetric(TransactionMetricBuilder transactionMetricBuilder, HttpResponse response) {
        StatusLine statusLine = response.getStatusLine();
        if (statusLine == null) {
            return;
        }
        if (equalsTerminusKey(response.getHeaders(Constants.Carriers.RESPONSE_TERMINUS_KEY))) {
            transactionMetricBuilder.tag(PEER_SERVICE_SCOPE, PEER_SERVICE_INTERNAL);
        } else {
            transactionMetricBuilder.tag(PEER_SERVICE_SCOPE, PEER_SERVICE_EXTERNAL);
        }
        int statusCode = statusLine.getStatusCode();
        TransactionMetricUtils.handleStatusCode(transactionMetricBuilder, statusCode);
        MetricReporter.report(transactionMetricBuilder);
    }

    private boolean equalsTerminusKey(Header[] headers) {
        if (headers == null || headers.length == 0) {
            return false;
        }
        String tk = ConfigAccessor.Default.getConfig(AgentConfig.class).terminusKey();
        for (Header header : headers) {
            if (tk.equals(header.getValue())) {
                return true;
            }
        }
        return false;
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
