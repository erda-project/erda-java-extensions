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

package cloud.erda.agent.core.tracing.propagator;

import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.utils.UUIDGenerator;

/**
 * @author liuhaoyang
 * @since 2019-01-07 14:59
 **/
public class RequestIdHeader extends Header {

    private static final String Request_Id = "terminus-request-id";

    public RequestIdHeader(Header next) {
        super(next);
    }

    @Override
    public void inject(SpanContext context, Carrier carrier) {
        String requestId = context.getTraceId();
        carrier.put(Request_Id, requestId);
    }

    @Override
    public void extract(SpanContext.Builder builder, Carrier carrier) {
        String traceId = carrier.get(Request_Id);
        if (traceId == null) {
            traceId = UUIDGenerator.New();
        }
        builder.setTraceId(traceId);
    }
}
