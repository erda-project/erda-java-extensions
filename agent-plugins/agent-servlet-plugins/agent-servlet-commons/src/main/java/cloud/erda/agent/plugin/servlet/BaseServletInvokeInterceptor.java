/*
 * Copyright (c) 2021 Terminus, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.erda.agent.plugin.servlet;

import cloud.erda.agent.core.config.AgentConfig;
import cloud.erda.agent.core.config.ServiceConfig;
import cloud.erda.agent.core.config.loader.ConfigAccessor;
import cloud.erda.agent.core.tracing.*;
import cloud.erda.agent.core.tracing.span.LogFields;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.tracing.span.SpanBuilder;
import cloud.erda.agent.core.utils.Caller;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.DateTime;
import cloud.erda.agent.core.utils.TracerUtils;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricBuilder;
import cloud.erda.agent.plugin.app.insight.MetricReporter;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.agent.core.util.Strings;


/**
 * @author randomnil
 */
public abstract class BaseServletInvokeInterceptor implements InstanceMethodsAroundInterceptor {

    private static ServiceConfig config = ConfigAccessor.Default.getConfig(ServiceConfig.class);

    private final static String REQUEST_ATTRIBUTE_METRICS = "cloud.erda.metrics";

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        HttpServletRequest request = getRequest(context);
        preRequest(context, request);

        HttpServletResponse response = getResponse(context);
        response.setHeader(Constants.Carriers.RESPONSE_TERMINUS_KEY, ConfigAccessor.Default.getConfig(AgentConfig.class).terminusKey());
        Scope scope = TracerManager.currentTracer().active();
        if (scope != null) {
            response.setHeader("x-msp-trace-id", scope.span().getContext().getTraceId());
        }
    }

    @Override
    public Object afterMethod(final IMethodInterceptContext context, Object ret) throws Throwable {

        Caller.invoke(() -> {
            HttpServletRequest request = getRequest(context);
            HttpServletResponse response = getResponse(context);
            postRequest(context, request, response);
        });

        // 安全关闭http的入口span，避免request-id泄漏
        Caller.invoke(() -> {
            Scope scope = context.getAttachment(Constants.Keys.TRACE_SCOPE);
            if (scope != null) {
                scope.close();
            }
        });
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        TracerUtils.handleException(t);
        TransactionMetricUtils.handleException(context);
    }

    private void preRequest(IMethodInterceptContext context, HttpServletRequest request) {
        Object value = request.getAttribute(REQUEST_ATTRIBUTE_METRICS);
        if (value instanceof Integer && !Integer.valueOf(context.hashCode()).equals(value)) {
            return;
        }

        Tracer tracer = TracerManager.currentTracer();
        TracerContext tracerContext = tracer.context();
        tracerContext.setAttachment(Constants.Keys.REQUEST_KEY_IN_RUNTIME_CONTEXT, request);

        cloud.erda.agent.plugin.servlet.ServletRequestCarrier carrier = new cloud.erda.agent.plugin.servlet.ServletRequestCarrier(request);
        SpanContext spanContext = tracer.extract(carrier);
        SpanBuilder spanBuilder = tracer.buildSpan(request.getMethod() + " " + request.getRequestURL().toString());
        spanBuilder.childOf(spanContext);

        String host = request.getServerName() + ":" + request.getServerPort();

        Scope scope = spanBuilder.startActive();
        context.setAttachment(Constants.Keys.TRACE_SCOPE, scope);
        Span span = scope.span();
        span.tag(Constants.Tags.HTTP_PATH, request.getRequestURI());
        span.tag(Constants.Tags.DB_HOST, host);
        span.tag(Constants.Tags.HTTP_METHOD, request.getMethod());
        span.tag(Constants.Tags.HTTP_URL, request.getRequestURL().toString());
        span.tag(Constants.Tags.SPAN_LAYER, Constants.Tags.SPAN_LAYER_HTTP);
        span.tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_SERVER);
        span.tag(Constants.Tags.COMPONENT, getComponent());
        span.log(DateTime.currentTimeNano()).event(LogFields.Event, "server received");
        if (Strings.isEmpty(request.getRequestURI())) {
            return;
        }
        TransactionMetricBuilder transactionMetricBuilder = new TransactionMetricBuilder(Constants.Metrics.APPLICATION_HTTP, true);
        context.setAttachment(Constants.Keys.METRIC_BUILDER, transactionMetricBuilder);
        transactionMetricBuilder.tag(Constants.Tags.COMPONENT, getComponent())
                .tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_SERVER)
                .tag(Constants.Tags.DB_HOST, host)
                .tag(Constants.Tags.PEER_ADDRESS, host)
                .tag(Constants.Tags.HTTP_URL, request.getRequestURL().toString())
                .tag(Constants.Tags.HTTP_PATH, request.getRequestURI())
                .tag(Constants.Tags.HTTP_METHOD, request.getMethod())
                .tag(Constants.Tags.PEER_HOSTNAME, request.getRemoteHost());

        String healthCheckPath = config.getHttpHealthCheckPath();
        if (!Strings.isEmpty(healthCheckPath) && healthCheckPath.equals(request.getServletPath())) {
            transactionMetricBuilder.tag(Constants.Tags.HEALTH_CHECK, Boolean.TRUE.toString());
        }

        request.setAttribute(REQUEST_ATTRIBUTE_METRICS, context.hashCode());
    }

    private void postRequest(IMethodInterceptContext context,
                             HttpServletRequest request,
                             HttpServletResponse response) {
        Object value = request.getAttribute(REQUEST_ATTRIBUTE_METRICS);
        if (value instanceof Integer && !Integer.valueOf(context.hashCode()).equals(value)) {
            return;
        }

        TransactionMetricBuilder transactionMetricBuilder = context.getAttachment(Constants.Keys.METRIC_BUILDER);
        if (transactionMetricBuilder != null) {
            transactionMetricBuilder.field(Constants.Tags.HTTP_STATUS, response.getStatus());

            TransactionMetricUtils.handleStatusCode(transactionMetricBuilder, response.getStatus());
            MetricReporter.report(transactionMetricBuilder);
        }

        Scope scope = context.getAttachment(Constants.Keys.TRACE_SCOPE);
        if (scope != null) {
            Span span = scope.span();
            span.tag(Constants.Tags.HTTP_STATUS, String.valueOf(response.getStatus()));
            if (response.getStatus() >= 500) {
                span.tag(Constants.Tags.ERROR, Constants.Tags.ERROR_TRUE);
            }
            span.log(DateTime.currentTimeNano()).event(LogFields.Event, "server send");
        }
    }

    protected abstract HttpServletRequest getRequest(IMethodInterceptContext context);

    protected abstract HttpServletResponse getResponse(IMethodInterceptContext context);

    protected abstract String getComponent();
}
