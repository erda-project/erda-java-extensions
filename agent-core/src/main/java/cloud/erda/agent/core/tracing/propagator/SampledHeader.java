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
 * @since 2019-01-07 16:18
 **/
public class SampledHeader extends Header {

    private static final String Request_Sampled = "terminus-request-sampled";
    private final Sampler sampler;

    public SampledHeader(Sampler sampler, Header next) {
        super(next);
        this.sampler = sampler;
    }

    @Override
    public void inject(SpanContext context, Carrier carrier) {
        String sampled = String.valueOf(context.getSampled());
        carrier.put(Request_Sampled, sampled);
    }

    @Override
    public void extract(SpanContext.Builder builder, Carrier carrier) {
        Boolean sampled = getSample(carrier.get(Request_Sampled));
        if (sampled == null) {
            sampled = sampler.shouldSample();
        }
        builder.setSampled(sampled);
    }

    private Boolean getSample(String sampled) {
        if (sampled == null) {
            return null;
        }
        try {
            return Boolean.parseBoolean(sampled);
        } catch (Exception ignored) {
            return null;
        }
    }
}
