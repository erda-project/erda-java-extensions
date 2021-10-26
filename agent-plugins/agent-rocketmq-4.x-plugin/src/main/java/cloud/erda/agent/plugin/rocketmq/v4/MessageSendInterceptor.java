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

package cloud.erda.agent.plugin.rocketmq.v4;

import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.DynamicFieldEnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.propagator.TextMapCarrier;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.TracerUtils;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricBuilder;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricContext;
import cloud.erda.agent.plugin.app.insight.MetricReporter;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricUtils;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageDecoder;
import org.apache.rocketmq.common.protocol.header.SendMessageRequestHeader;

import java.util.HashMap;
import java.util.Map;

import static cloud.erda.agent.core.utils.Constants.Tags.*;

/**
 * {@link MessageSendInterceptor} create exit span when the method {@link org.apache.rocketmq.client.impl
 * .MQClientAPIImpl#sendMessage(String, String, Message, org.apache.rocketmq.common.protocol.header
 * .SendMessageRequestHeader, long, org.apache.rocketmq.client.impl.CommunicationMode,
 * org.apache.rocketmq.client.producer.SendCallback, org.apache.rocketmq.client.impl.producer.TopicPublishInfo,
 * org.apache.rocketmq.client.impl.factory.MQClientInstance, int, org.apache.rocketmq.client.hook.SendMessageContext,
 * org.apache.rocketmq.client.impl.producer .DefaultMQProducerImpl)} execute.
 *
 * @author zhang xin
 */
public class MessageSendInterceptor implements InstanceMethodsAroundInterceptor {

    private final static String SYNC_PRODUCER_PREFIX = "RocketMQ/Producer/Sync/";
    private final static String ASYNC_PRODUCER_PREFIX = "RocketMQ/Producer/Async/";

    private static ThreadLocal<Boolean> IS_SYNC = new ThreadLocal<Boolean>();

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        Object[] allArguments = context.getArguments();
        Message message = (Message) allArguments[2];
        IS_SYNC.set(allArguments[6] == null);
        String operationName = (IS_SYNC.get() ? SYNC_PRODUCER_PREFIX : ASYNC_PRODUCER_PREFIX) + message.getTopic();
        String nameServerAddress = String.valueOf( ((DynamicFieldEnhancedInstance)context.getInstance()).getDynamicField());

        Tracer tracer = TracerManager.tracer();
        SpanContext spanContext = tracer.active() != null ? tracer.active().span().getContext() : null;
        Span span = tracer.buildSpan(operationName).childOf(spanContext).startActive().span();
        span.tag(COMPONENT, COMPONENT_ROCKETMQ);
        span.tag(SPAN_KIND, SPAN_KIND_PRODUCER);
        span.tag(SPAN_LAYER, SPAN_LAYER_MQ);
        span.tag(PEER_ADDRESS, nameServerAddress);
        span.tag(HOST, nameServerAddress);
        span.tag(MESSAGE_BUS_DESTINATION, message.getTopic());

        span.getContext().getBaggage().putAll(TransactionMetricContext.instance);
        span.getContext().getBaggage().put(NAME_SERVER_ADDRESS, nameServerAddress);
        Map<String, String> map = new HashMap<String, String>(16);
        TextMapCarrier carrier = new TextMapCarrier(map);
        tracer.inject(span.getContext(), carrier);

        SendMessageRequestHeader requestHeader = (SendMessageRequestHeader) context.getArguments()[3];
        StringBuilder properties = new StringBuilder(requestHeader.getProperties());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }

            properties.append(entry.getKey())
                    .append(MessageDecoder.NAME_VALUE_SEPARATOR)
                    .append(entry.getValue())
                    .append(MessageDecoder.PROPERTY_SEPARATOR);
        }
        requestHeader.setProperties(properties.toString());

        TransactionMetricBuilder transactionMetricBuilder = new TransactionMetricBuilder(Constants.Metrics.APPLICATION_MQ, false);
        transactionMetricBuilder.tag(COMPONENT, COMPONENT_ROCKETMQ)
                .tag(SPAN_KIND, SPAN_KIND_PRODUCER)
                .tag(PEER_ADDRESS, nameServerAddress)
                .tag(HOST, nameServerAddress)
                .tag(MESSAGE_BUS_DESTINATION, message.getTopic());
        if (IS_SYNC.get()) {
            context.setAttachment(Constants.Keys.METRIC_BUILDER, transactionMetricBuilder);
        } else {
            EnhancedInstance instance = (EnhancedInstance) allArguments[6];
            MessageSendAsyncInfo info = new MessageSendAsyncInfo(tracer.capture(), transactionMetricBuilder);
            ((DynamicFieldEnhancedInstance)instance).setDynamicField(info);
        }
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        TransactionMetricBuilder transactionMetricBuilder = context.getAttachment(Constants.Keys.METRIC_BUILDER);
        if (transactionMetricBuilder != null) {
            MetricReporter.report(transactionMetricBuilder);
        }

        TracerManager.tracer().active().close(IS_SYNC.get());
        IS_SYNC.remove();
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        TransactionMetricUtils.handleException(context);
        TracerUtils.handleException(t);
    }
}
