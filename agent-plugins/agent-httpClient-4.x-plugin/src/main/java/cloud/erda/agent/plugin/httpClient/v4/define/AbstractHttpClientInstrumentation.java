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


package cloud.erda.agent.plugin.httpClient.v4.define;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;

import static org.apache.skywalking.apm.agent.core.plugin.match.NameMatch.byName;

/**
 * {@link AbstractHttpClientInstrumentation} presents that skywalking intercepts
 * AbstractHttpClient#doExecute
 * by using {@link HttpClientInstrumentation#INTERCEPT_CLASS}.
 *
 * @author zhangxin
 */
public class AbstractHttpClientInstrumentation extends cloud.erda.agent.plugin.httpClient.v4.define.HttpClientInstrumentation {

    private static final String ENHANCE_CLASS = "org.apache.http.impl.client.AbstractHttpClient";

    @Override
    public ClassMatch enhanceClass() {
        return byName(ENHANCE_CLASS);
    }

    /**
     * version 4.2, intercept method: execute, intercept
     * public final HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context)
     */
    @Override
    protected InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return getInstanceMethodsInterceptPoints("doExecute");
    }
}
