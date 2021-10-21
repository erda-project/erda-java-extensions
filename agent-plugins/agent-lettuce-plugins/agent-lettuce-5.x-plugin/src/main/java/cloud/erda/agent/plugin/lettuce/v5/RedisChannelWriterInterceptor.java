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

package cloud.erda.agent.plugin.lettuce.v5;

import io.lettuce.core.protocol.RedisCommand;
import cloud.erda.agent.core.utils.Constants;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.*;
import org.apache.skywalking.apm.agent.core.util.Strings;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.tracing.span.SpanBuilder;
import cloud.erda.agent.core.utils.TracerUtils;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricBuilder;
import cloud.erda.agent.plugin.app.insight.MetricReporter;

import java.util.Collection;

public class RedisChannelWriterInterceptor implements InstanceMethodsAroundInterceptor, InstanceConstructorInterceptor {

    @Override
    public void onConstruct(EnhancedInstance objInst, Object[] allArguments) {
        DynamicFieldEnhancedInstance optionsInst = (DynamicFieldEnhancedInstance) allArguments[0];
        ((DynamicFieldEnhancedInstance)objInst).setDynamicField(optionsInst.getDynamicField());
    }

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        Tracer tracer = TracerManager.tracer();
        String peer = String.valueOf( ((DynamicFieldEnhancedInstance)context.getInstance()).getDynamicField());
        SpanContext spanContext = tracer.active() != null ? tracer.active().span().getContext() : null;
        String operationName = "Lettuce/";

        StringBuilder dbStatement = new StringBuilder();
        if (context.getArguments()[0] instanceof RedisCommand) {
            RedisCommand redisCommand = (RedisCommand) context.getArguments()[0];
            String command = redisCommand.getType().name();
            operationName = operationName + command;
            dbStatement.append(command);
        } else if (context.getArguments()[0] instanceof Collection) {
            Collection<RedisCommand> redisCommands = (Collection<RedisCommand>) context.getArguments()[0];
            operationName = operationName + "BATCH_WRITE";
            for (RedisCommand redisCommand : redisCommands) {
                dbStatement.append(redisCommand.getType().name()).append(";");
            }
        }

        SpanBuilder spanBuilder = tracer.buildSpan(operationName + "/" + context.getMethod().getName());
        Span span = spanBuilder.childOf(spanContext).startActive().span();
        span.tag(Constants.Tags.DB_TYPE, Constants.Tags.DB_TYPE_REDIS);
        span.tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_LETTUCE);
        span.tag(Constants.Tags.PEER_SERVICE, peer);
        span.tag(Constants.Tags.PEER_ADDRESS, peer);
        span.tag(Constants.Tags.PEER_HOSTNAME, peer);
        span.tag(Constants.Tags.SPAN_LAYER, Constants.Tags.SPAN_LAYER_CACHE);
        span.tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_CLIENT);
        span.tag(Constants.Tags.HOST, peer);
        span.tag(Constants.Tags.DB_STATEMENT, dbStatement.toString());

        if (Strings.isEmpty(dbStatement.toString())) {
            return;
        }

        TransactionMetricBuilder transactionMetricBuilder = new TransactionMetricBuilder(Constants.Metrics.APPLICATION_CACHE, false);
        context.setAttachment(Constants.Keys.METRIC_BUILDER, transactionMetricBuilder);
        transactionMetricBuilder.tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_LETTUCE)
                .tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_CLIENT)
                .tag(Constants.Tags.PEER_SERVICE, peer)
                .tag(Constants.Tags.HOST, peer)
                .tag(Constants.Tags.DB_STATEMENT, dbStatement.toString())
                .tag(Constants.Tags.DB_TYPE, Constants.Tags.DB_TYPE_REDIS);
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        TransactionMetricBuilder transactionMetricBuilder = context.getAttachment(Constants.Keys.METRIC_BUILDER);
        if (transactionMetricBuilder != null) {
            MetricReporter.report(transactionMetricBuilder);
        }
        TracerManager.tracer().active().close();
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        TracerUtils.handleException(t);
    }
}
