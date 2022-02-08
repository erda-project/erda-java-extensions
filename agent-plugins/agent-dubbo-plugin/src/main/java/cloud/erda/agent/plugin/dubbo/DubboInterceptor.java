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

package cloud.erda.agent.plugin.dubbo;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import cloud.erda.agent.core.tracing.Scope;
import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.propagator.TextMapCarrier;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.tracing.span.SpanBuilder;
import cloud.erda.agent.core.utils.Caller;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.TracerUtils;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricBuilder;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricContext;
import cloud.erda.agent.plugin.app.insight.MetricReporter;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricUtils;

/**
 * {@link DubboInterceptor} define how to enhance class {@link com.alibaba.dubbo.monitor.support.MonitorFilter#invoke
 * (Invoker, Invocation)}. the trace context transport to the provider side by {@link RpcContext#attachments}.but all
 * the version of dubbo framework below 2.8.3 don't support {@link RpcContext#attachments}, we support another way to
 * support it.
 *
 * @author zhangxin
 */
public class DubboInterceptor implements InstanceMethodsAroundInterceptor {
    /**
     * <h2>Consumer:</h2> The serialized trace context data will
     * inject to the {@link RpcContext#attachments} for transport to provider side.
     *
     * <h2>Provider:</h2> The serialized trace context data will extract from
     * {@link RpcContext#attachments}. current trace segment will ref if the serialize context data is not null.
     */
    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) {
        Object[] allArguments = context.getArguments();
        Invoker invoker = (Invoker) allArguments[0];
        Invocation invocation = (Invocation) allArguments[1];
        RpcContext rpcContext = RpcContext.getContext();
        boolean isConsumer = rpcContext.isConsumerSide();
        URL requestURL = invoker.getUrl();

        final String host = requestURL.getHost();
        final int port = requestURL.getPort();

        Tracer tracer = TracerManager.currentTracer();
        Span span;
        if (isConsumer) {
            SpanContext spanContext = tracer.active() != null ? tracer.active().span().getContext() : null;
            SpanBuilder spanBuilder = tracer.buildSpan(generateOperationName(requestURL, invocation));
            Scope scope = spanBuilder.childOf(spanContext).startActive();
            context.setAttachment(Constants.Keys.TRACE_SCOPE, scope);
            span = scope.span();
            TextMapCarrier carrier = new TextMapCarrier(rpcContext.getAttachments());
            span.getContext().getBaggage().putAll(TransactionMetricContext.instance);
            tracer.inject(span.getContext(), carrier);
            span.tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_CLIENT);
        } else {
            TextMapCarrier carrier = new TextMapCarrier(rpcContext.getAttachments());
            SpanContext spanContext = tracer.extract(carrier);
            SpanBuilder spanBuilder = tracer.buildSpan(generateOperationName(requestURL, invocation));
            spanBuilder.childOf(spanContext);
            Scope scope = spanBuilder.startActive();
            context.setAttachment(Constants.Keys.TRACE_SCOPE, scope);
            span = scope.span();
            span.tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_SERVER);

            TransactionMetricBuilder transactionMetricBuilder = new TransactionMetricBuilder(Constants.Metrics.APPLICATION_RPC, true);
            context.setAttachment(Constants.Keys.METRIC_BUILDER, transactionMetricBuilder);
            transactionMetricBuilder.tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_DUBBO)
                    .tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_SERVER)
                    .tag(Constants.Tags.PEER_ADDRESS, rpcContext.getRemoteAddressString())
                    .tag(Constants.Tags.PEER_SERVICE, invoker.getInterface().getName() + "." + invocation.getMethodName())
                    .tag(Constants.Tags.DB_HOST, rpcContext.getLocalAddressString())
                    .tag(Constants.Tags.DUBBO_SERVICE, invoker.getInterface().getName())
                    .tag(Constants.Tags.DUBBO_METHOD, invocation.getMethodName());
        }

        span.tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_DUBBO);
        span.tag(Constants.Tags.PEER_PORT, String.valueOf(port));
        span.tag(Constants.Tags.PEER_ADDRESS, rpcContext.getRemoteAddressString());
        span.tag(Constants.Tags.PEER_SERVICE, invoker.getInterface().getName() + "." + invocation.getMethodName());
        span.tag(Constants.Tags.SPAN_LAYER, Constants.Tags.SPAN_LAYER_RPC);
        span.tag(Constants.Tags.DB_HOST, rpcContext.getLocalAddressString());
        span.tag(Constants.Tags.DUBBO_SERVICE, invoker.getInterface().getName());
        span.tag(Constants.Tags.DUBBO_METHOD, invocation.getMethodName());
    }

    @Override
    public Object afterMethod(final IMethodInterceptContext context, final Object ret) {
        Caller.invoke(new Caller.Action() {
            @Override
            public void invoke() throws Exception {
                final Result result = (Result) ret;
                TransactionMetricBuilder transactionMetricBuilder = context.getAttachment(Constants.Keys.METRIC_BUILDER);
                if (result != null && result.getException() != null) {
                    TracerUtils.handleException(result.getException());
                    if (transactionMetricBuilder != null) {
                        TransactionMetricUtils.handleException(transactionMetricBuilder);
                    }
                }
                if (transactionMetricBuilder != null) {
                    MetricReporter.report(transactionMetricBuilder);
                }
            }
        });

        // 安全的关闭dubbo的span，避免request-id泄漏
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
        TransactionMetricUtils.handleException(context);
        TracerUtils.handleException(t);
    }

    /**
     * Format operation name. e.g. cloud.erda.agent.plugin.test.Test.test(String)
     *
     * @return operation name.
     */
    private String generateOperationName(URL requestURL, Invocation invocation) {
        StringBuilder operationName = new StringBuilder();
        operationName.append(requestURL.getPath());
        operationName.append("." + invocation.getMethodName() + "(");
        for (Class<?> classes : invocation.getParameterTypes()) {
            operationName.append(classes.getSimpleName() + ",");
        }

        if (invocation.getParameterTypes().length > 0) {
            operationName.delete(operationName.length() - 1, operationName.length());
        }

        operationName.append(")");

        return operationName.toString();
    }

    /**
     * Format request url. e.g. dubbo://127.0.0.1:20880/cloud.erda.agent.plugin.test.Test.test(String).
     *
     * @return request url.
     */
    private String generateRequestURL(URL url, Invocation invocation) {
        StringBuilder requestURL = new StringBuilder();
        requestURL.append(url.getProtocol() + "://");
        requestURL.append(url.getHost());
        requestURL.append(":" + url.getPort() + "/");
        requestURL.append(generateOperationName(url, invocation));
        return requestURL.toString();
    }
}
