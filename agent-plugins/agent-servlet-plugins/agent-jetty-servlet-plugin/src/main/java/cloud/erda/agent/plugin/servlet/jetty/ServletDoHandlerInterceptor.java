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

package cloud.erda.agent.plugin.servlet.jetty;

import cloud.erda.agent.core.utils.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import cloud.erda.agent.plugin.servlet.BaseServletInvokeInterceptor;


/**
 * @author randomnil
 */
public class ServletDoHandlerInterceptor extends BaseServletInvokeInterceptor {

    @Override
    protected HttpServletRequest getRequest(IMethodInterceptContext context) {
        return (HttpServletRequest) context.getArguments()[2];
    }

    @Override
    protected HttpServletResponse getResponse(IMethodInterceptContext context) {
        return (HttpServletResponse) context.getArguments()[3];
    }

    @Override
    protected String getComponent() {
        return Constants.Tags.COMPONENT_JETTY;
    }
}
