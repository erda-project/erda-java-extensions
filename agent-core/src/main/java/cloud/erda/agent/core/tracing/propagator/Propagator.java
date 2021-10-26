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

import cloud.erda.agent.core.tracing.Sampler;
import cloud.erda.agent.core.tracing.SpanContext;

/**
 * @author liuhaoyang
 * @since 2019-01-07 13:32
 **/
public class Propagator {

    private final Header headers;

    public Propagator(Sampler sampler) {
        this.headers = createHeader(sampler);
    }

    public void inject(SpanContext spanContext, Carrier carrier) {
        Header header = headers;
        while (header.hasNext()) {
            header.inject(spanContext, carrier);
            header = header.getNext();
        }
    }

    public SpanContext extract(Carrier carrier) {
        SpanContext.Builder builder = new SpanContext.Builder();
        Header header = headers;
        while (header.hasNext()) {
            header.extract(builder, carrier);
            header = header.getNext();
        }
        return builder.build(false);
    }

    private static Header createHeader(Sampler sampler) {
        Header header = new NoopHeader();
        header = new BaggageHeader(header);
        header = new SpanIdHeader(header);
        header = new SampledHeader(sampler, header);
        header = new RequestIdHeader(header);
        return header;
    }
}
