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

package cloud.erda.agent.plugin.okhttp.v4;

import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.propagator.TextMapCarrier;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.plugin.app.insight.AppMetricBuilder;
import cloud.erda.agent.plugin.app.insight.AppMetricContext;
import cloud.erda.agent.plugin.app.insight.AppMetricUtils;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.skywalking.apm.agent.core.util.Strings;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author randomnil
 */
public class CallInterceptorUtils {

    static AppMetricBuilder createRequestAppMetric(Request request) {
        if (request == null) {
            return null;
        }

        URI uri = request.url().uri();
        String hostname = uri.getHost();
        if (uri.getPort() > 0) {
            hostname += ":" + uri.getPort();
        }
        return AppMetricUtils.createHttpMetric(hostname);
    }

    static AppMetricBuilder wrapResponseAppMetric(AppMetricBuilder appMetricBuilder, Response response) {
        if (response == null) {
            return appMetricBuilder;
        }

        String terminusKey = response.header(Constants.Carriers.RESPONSE_TERMINUS_KEY);
        if (!Strings.isEmpty(terminusKey)) {
            return null;
        }

        AppMetricUtils.handleStatusCode(appMetricBuilder, response.code());
        return appMetricBuilder;
    }

    static void wrapRequestSpan(Span span, Request request) {
        if (request == null) {
            return;
        }

        URI uri = request.url().uri();
        String hostname = uri.getHost() + ":" + uri.getPort();
        span.tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_OKHTTP);
        span.tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_CLIENT);
        span.tag(Constants.Tags.SPAN_LAYER, Constants.Tags.SPAN_LAYER_HTTP);
        span.tag(Constants.Tags.PEER_ADDRESS, uri.getScheme() + "://" + hostname);
        span.tag(Constants.Tags.PEER_HOSTNAME, hostname);
        span.tag(Constants.Tags.PEER_PORT, String.valueOf(uri.getPort()));
        span.tag(Constants.Tags.HTTP_URL, uri.toString());
        span.tag(Constants.Tags.HTTP_METHOD, request.method());
    }

    static void wrapResponseSpan(Span span, Response response) {
        if (response == null) {
            return;
        }

        int statusCode = response.code();
        if (statusCode >= 400) {
            span.tag(Constants.Tags.ERROR, Constants.Tags.ERROR_TRUE);
        }
        span.tag(Constants.Tags.HTTP_STATUS, String.valueOf(statusCode));
    }

    static void injectRequestHeader(Request request, Span span) throws Throwable {
        Tracer tracer = TracerManager.tracer();
        tracer.context().put(AppMetricContext.instance);
        Map<String, String> map = new HashMap<String, String>(16);
        TextMapCarrier carrier = new TextMapCarrier(map);
        tracer.inject(span.getContext(), carrier);

        Field headersField = Request.class.getDeclaredField("headers");
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(headersField, headersField.getModifiers() & ~Modifier.FINAL);

        headersField.setAccessible(true);
        Headers.Builder headerBuilder = request.headers().newBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (Strings.isEmpty(entry.getKey())) {
                continue;
            }
            if (Strings.isEmpty(entry.getValue())) {
                headerBuilder.removeAll(entry.getKey());
                continue;
            }
            headerBuilder.add(entry.getKey(), entry.getValue());
        }
        headersField.set(request, headerBuilder.build());
    }
}
