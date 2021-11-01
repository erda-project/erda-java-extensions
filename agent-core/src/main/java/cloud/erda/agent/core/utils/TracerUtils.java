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

package cloud.erda.agent.core.utils;

import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.Scope;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.span.Span;

import static cloud.erda.agent.core.utils.Constants.Tags.*;

/**
 * @author liuhaoyang
 * @since 2019-01-11 17:48
 **/
public class TracerUtils {

    public static void handleStatusCode(Scope scope, int statusCode) {
        if (scope == null) {
            return;
        }

        Span span = scope.span();
        if (statusCode >= 400) {
            span.tag(ERROR, ERROR_TRUE);
        }
        span.tag(HTTP_STATUS, String.valueOf(statusCode));
    }

    public static void handleException(Throwable throwable) {
        Tracer tracer = TracerManager.currentTracer();
        Scope scope = tracer.active();
        handleException(scope, throwable);
    }

    public static void handleException(Scope scope, Throwable throwable) {
        if (scope != null) {
            Span span = scope.span();
            span.tag(ERROR, ERROR_TRUE);
            span.tag(ERROR_MESSAGE, throwable.getMessage());
        }
    }
}
