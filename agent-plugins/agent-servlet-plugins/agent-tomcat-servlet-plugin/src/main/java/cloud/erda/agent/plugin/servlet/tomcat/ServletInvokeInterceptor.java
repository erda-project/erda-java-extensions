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

package cloud.erda.agent.plugin.servlet.tomcat;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import cloud.erda.agent.plugin.servlet.BaseServletInvokeInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author randomnil
 */
public class ServletInvokeInterceptor extends BaseServletInvokeInterceptor {

    @Override
    protected HttpServletRequest getRequest(IMethodInterceptContext context) {
        return (HttpServletRequest) context.getArguments()[0];
    }

    @Override
    protected HttpServletResponse getResponse(IMethodInterceptContext context) {
        return (HttpServletResponse) context.getArguments()[1];
    }
}
