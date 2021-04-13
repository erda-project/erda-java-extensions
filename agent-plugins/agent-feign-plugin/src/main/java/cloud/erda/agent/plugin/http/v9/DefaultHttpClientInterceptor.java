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

package cloud.erda.agent.plugin.http.v9;

import feign.Request;
import feign.Response;
import org.apache.skywalking.apm.agent.core.util.Strings;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import cloud.erda.agent.core.tracing.Scope;
import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.propagator.TextMapCarrier;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.HttpUtils;
import cloud.erda.agent.core.utils.TracerUtils;
import cloud.erda.agent.plugin.app.insight.AppMetricBuilder;
import cloud.erda.agent.plugin.app.insight.AppMetricContext;
import cloud.erda.agent.plugin.app.insight.AppMetricRecorder;
import cloud.erda.agent.plugin.app.insight.AppMetricUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;

/**
 * {@link DefaultHttpClientInterceptor} intercept the default implementation of http calls by the Feign.
 *
 * @author peng-yongsheng
 */
public class DefaultHttpClientInterceptor implements InstanceMethodsAroundInterceptor {

    /**
     * Get the {@link feign.Request} from {@link EnhancedInstance}, then create {@link Span} and set host, port, kind,
     * component, url from {@link feign.Request}. Through the reflection of the way, set the http header of context data
     * into {@link feign.Request#headers}.
     *
     * @param context
     * @param result  change this result, if you want to truncate the method.
     * @throws Throwable
     */
    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        Object[] allArguments = context.getArguments();
        if (allArguments == null || allArguments.length < 1 || !(allArguments[0] instanceof Request)) {
            return;
        }
        Request request = (Request) allArguments[0];

        URL url = new URL(request.url());
        String path = HttpUtils.getPath(url.getPath());
        int port = HttpUtils.getPort(url.getPort(), url.getProtocol());
        String peerHost = url.getHost();
        if (port > 0) {
            peerHost += ":" + port;
        }

        Tracer tracer = TracerManager.tracer();
        SpanContext spanContext = tracer.active() != null ? tracer.active().span().getContext() : null;
        Span span = tracer.buildSpan(path).childOf(spanContext).startActive().span();
        span.tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_FEIGN);
        span.tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_CLIENT);
        span.tag(Constants.Tags.SPAN_LAYER, Constants.Tags.SPAN_LAYER_HTTP);
        span.tag(Constants.Tags.PEER_ADDRESS, request.url());
        span.tag(Constants.Tags.PEER_HOSTNAME, peerHost);
        span.tag(Constants.Tags.PEER_PORT, String.valueOf(port));
        span.tag(Constants.Tags.HTTP_URL, request.url());
        span.tag(Constants.Tags.HTTP_PATH, path);
        span.tag(Constants.Tags.HTTP_METHOD, request.method().toUpperCase());

        tracer.context().put(AppMetricContext.instance);
        Map<String, String> map = new HashMap<String, String>(16);
        TextMapCarrier carrier = new TextMapCarrier(map);
        tracer.inject(span.getContext(), carrier);

        Field headersField = Request.class.getDeclaredField("headers");
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(headersField, headersField.getModifiers() & ~Modifier.FINAL);
        headersField.setAccessible(true);
        Map<String, Collection<String>> headers = new LinkedHashMap<String, Collection<String>>(request.headers());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (Strings.isEmpty(entry.getKey())) {
                continue;
            }
            if (Strings.isEmpty(entry.getValue())) {
                headers.remove(entry.getKey());
                continue;
            }
            List<String> contextCollection = new LinkedList<String>();
            contextCollection.add(entry.getValue());
            headers.put(entry.getKey(), contextCollection);
        }
        headersField.set(request, Collections.unmodifiableMap(headers));

        AppMetricBuilder appMetricBuilder = AppMetricUtils.createHttpMetric(peerHost);
        context.setAttachment(Constants.Keys.METRIC_BUILDER, appMetricBuilder);
    }

    /**
     * Get the status code from {@link Response}, when status code greater than 400, it means there was some errors in
     * the server. Finish the {@link Span}.
     *
     * @param context
     * @param ret     the method's original return value.
     * @return
     * @throws Throwable
     */
    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        Object[] allArguments = context.getArguments();
        if (allArguments == null || allArguments.length < 1 || !(allArguments[0] instanceof Request)) {
            return ret;
        }
        if (!(ret instanceof Response)) {
            return ret;
        }
        Response response = (Response) ret;

        AppMetricBuilder appMetricBuilder = context.getAttachment(Constants.Keys.METRIC_BUILDER);
        if (appMetricBuilder != null) {
            Map headers = response.headers();
            if (headers == null || !headers.containsKey(Constants.Carriers.RESPONSE_TERMINUS_KEY)) {
                AppMetricUtils.handleStatusCode(appMetricBuilder, response.status());
                AppMetricRecorder.record(appMetricBuilder);
            }
        }

        Scope scope = TracerManager.tracer().active();
        if (scope != null) {
            TracerUtils.handleStatusCode(scope, response.status());
            scope.close();
        }
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        TracerUtils.handleException(t);
    }
}