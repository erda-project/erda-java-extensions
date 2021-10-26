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

package cloud.erda.agent.core.tracing;

import cloud.erda.agent.core.tracing.propagator.Propagator;
import cloud.erda.agent.core.utils.Constants;
import org.apache.skywalking.apm.agent.core.boot.ServiceManager;
import cloud.erda.agent.core.tracing.propagator.Carrier;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.tracing.span.SpanBuilder;
import cloud.erda.agent.core.tracing.span.SpanSerializer;
import cloud.erda.agent.core.metrics.MetricDispatcher;

/**
 * @author liuhaoyang
 * @since 2019-01-04 17:36
 **/
public class ScopeTracer implements Tracer {

    private static final SpanSerializer spanSerializer = new SpanSerializer();
    private static final MetricDispatcher transporter = ServiceManager.INSTANCE.findService(MetricDispatcher.class);
    private static final Sampler sampler = ServiceManager.INSTANCE.findService(SamplerService.class);
    private static final Propagator propagator = new Propagator(sampler);

    private TracerContext tracerContext = new TracerContext();
    private Scope activeScope;

    @Override
    public TracerContext context() {
        return tracerContext;
    }

    @Override
    public Scope active() {
        return activeScope;
    }

    @Override
    public Scope attach(TracerSnapshot snapshot) {
        tracerContext.attach(snapshot.getTracerContext());
        SpanContext spanContext = snapshot.getSpanContext();
        SpanBuilder spanBuilder = buildSpan("tracer context switch")
                .tag(Constants.Tags.SPAN_LAYER, Constants.Tags.SPAN_LAYER_LOCAL);
        spanBuilder.childOf(spanContext);
        return spanBuilder.startActive();
    }

    @Override
    public TracerSnapshot capture() {
        Scope scope = active();
        return new TracerSnapshot(tracerContext, scope == null ? null : scope.span().getContext());
    }

    @Override
    public void dispatch(Span span) {
        transporter.dispatch(spanSerializer.serialize(span));
    }

    @Override
    public void inject(SpanContext spanContext, Carrier carrier) {
        propagator.inject(spanContext, carrier);
    }

    @Override
    public SpanContext extract(Carrier carrier) {
        return propagator.extract(carrier);
    }

    @Override
    public Scope activate(Span span) {
        Scope scope = activeScope;
        while (scope != null) {
            if (scope.span() == span) {
                return scope;
            }
            scope = scope.getPrevious();
        }
        return activate(new Scope(this, span, activeScope));
    }

    @Override
    public Scope activate(Scope scope) {
        if (activeScope == scope) {
            return scope;
        }
        return this.activeScope = scope;
    }

    @Override
    public SpanBuilder buildSpan(String operationName) {
        return new SpanBuilder(operationName, this, sampler);
    }
}
