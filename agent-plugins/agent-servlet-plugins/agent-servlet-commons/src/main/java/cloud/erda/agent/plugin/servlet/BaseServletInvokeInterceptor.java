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
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.tracing.span.SpanBuilder;
import cloud.erda.agent.core.utils.Caller;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.TracerUtils;
import cloud.erda.agent.plugin.app.insight.AppMetricBuilder;
import cloud.erda.agent.plugin.app.insight.AppMetricRecorder;
import cloud.erda.agent.plugin.app.insight.AppMetricUtils;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.agent.core.util.Strings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    }

    @Override
    public Object afterMethod(final IMethodInterceptContext context, Object ret) throws Throwable {

        Caller.invoke(new Caller.Action() {
            @Override
            public void invoke() throws Exception {
                HttpServletRequest request = getRequest(context);
                HttpServletResponse response = getResponse(context);
                postRequest(context, request, response);
            }
        });

        // 安全关闭http的入口span，避免request-id泄漏
        Caller.invoke(new Caller.Action() {
            @Override
            public void invoke() throws Exception {
                Scope scope = context.getAttachment(Constants.Keys.TRACE_SCOPE);
                if (scope != null) {
                    scope.close();
                }
            }
        });
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        TracerUtils.handleException(t);
        AppMetricUtils.handleException(context);
    }

    private void preRequest(IMethodInterceptContext context, HttpServletRequest request) {
        Object value = request.getAttribute(REQUEST_ATTRIBUTE_METRICS);
        if (value instanceof Integer && !Integer.valueOf(context.hashCode()).equals(value)) {
            return;
        }

        Tracer tracer = TracerManager.tracer();
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
        span.tag(Constants.Tags.HOST, host);
        span.tag(Constants.Tags.HTTP_METHOD, request.getMethod());
        span.tag(Constants.Tags.HTTP_URL, request.getRequestURL().toString());
        span.tag(Constants.Tags.SPAN_LAYER, Constants.Tags.SPAN_LAYER_HTTP);
        span.tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_SERVER);
        span.tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_SPRING_BOOT);

        if (Strings.isEmpty(request.getRequestURI())) {
            return;
        }
        AppMetricBuilder appMetricBuilder = new AppMetricBuilder(Constants.Metrics.APPLICATION_HTTP, true);
        context.setAttachment(Constants.Keys.METRIC_BUILDER, appMetricBuilder);
        appMetricBuilder.tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_HTTP)
                .tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_SERVER)
                .tag(Constants.Tags.HOST, host)
                .tag(Constants.Tags.HTTP_URL, request.getRequestURL().toString())
                .tag(Constants.Tags.HTTP_PATH, request.getRequestURI())
                .tag(Constants.Tags.HTTP_METHOD, request.getMethod())
                .tag(Constants.Tags.PEER_HOSTNAME, request.getRemoteHost());

        String healthCheckPath = config.getHttpHealthCheckPath();
        if (!Strings.isEmpty(healthCheckPath) && healthCheckPath.equals(request.getServletPath())) {
            appMetricBuilder.tag(Constants.Tags.HEALTH_CHECK, Boolean.TRUE.toString());
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

        AppMetricBuilder appMetricBuilder = context.getAttachment(Constants.Keys.METRIC_BUILDER);
        if (appMetricBuilder != null) {
            appMetricBuilder.field(Constants.Tags.HTTP_STATUS, response.getStatus());

            AppMetricUtils.handleStatusCode(appMetricBuilder, response.getStatus());
            AppMetricRecorder.record(appMetricBuilder);
        }

        Scope scope = context.getAttachment(Constants.Keys.TRACE_SCOPE);
        if (scope != null) {
            Span span = scope.span();
            span.tag(Constants.Tags.HTTP_STATUS, String.valueOf(response.getStatus()));
            if (response.getStatus() >= 500) {
                span.tag(Constants.Tags.ERROR, Constants.Tags.ERROR_TRUE);
            }
        }
    }

    protected abstract HttpServletRequest getRequest(IMethodInterceptContext context);

    protected abstract HttpServletResponse getResponse(IMethodInterceptContext context);
}
