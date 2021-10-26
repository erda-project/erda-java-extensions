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
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.propagator.Carrier;
import cloud.erda.agent.core.tracing.propagator.TextMapCarrier;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.TracerUtils;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricBuilder;
import cloud.erda.agent.plugin.app.insight.MetricReporter;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricUtils;
import org.apache.rocketmq.common.message.MessageExt;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link AbstractMessageConsumeInterceptor} create entry span when the <code>consumeMessage</code> in the {@link
 * org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently} and {@link
 * org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly} class.
 *
 * @author zhangxin
 */
public abstract class AbstractMessageConsumeInterceptor implements InstanceMethodsAroundInterceptor {

    private final static String CONSUMER_PREFIX = "RocketMQ/Consumer/";

    @Override
    public final void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        List<MessageExt> msgs = (List<MessageExt>) context.getArguments()[0];
        MessageExt firstMsg = msgs.get(0);
        String topic = firstMsg.getTopic();
        InetSocketAddress address = (InetSocketAddress) firstMsg.getStoreHost();
        String peerAddress = address.getHostName() + ":" + address.getPort();
        String operationName = CONSUMER_PREFIX + topic;

        Map<String, String> map = new HashMap<String, String>(16);
        for (MessageExt messageExt : msgs) {
            Map<String, String> properties = messageExt.getProperties();
            if (properties == null) {
                continue;
            }
            map.putAll(properties);
        }

        Tracer tracer = TracerManager.tracer();
        Carrier carrier = new TextMapCarrier(map);
        SpanContext spanContext = tracer.extract(carrier);
        String nameServerAddress = spanContext.getBaggage().get(Constants.Tags.NAME_SERVER_ADDRESS);

        Span span = tracer.buildSpan(operationName).childOf(spanContext).startActive().span();
        span.tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_ROCKETMQ);
        span.tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_CONSUMER);
        span.tag(Constants.Tags.SPAN_LAYER, Constants.Tags.SPAN_LAYER_MQ);
        span.tag(Constants.Tags.PEER_ADDRESS, nameServerAddress);
        span.tag(Constants.Tags.HOST, nameServerAddress);
        span.tag(Constants.Tags.MESSAGE_BUS_DESTINATION, topic);

        TransactionMetricBuilder transactionMetricBuilder = new TransactionMetricBuilder(Constants.Metrics.APPLICATION_MQ, true);
        context.setAttachment(Constants.Keys.METRIC_BUILDER, transactionMetricBuilder);
        transactionMetricBuilder.tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_ROCKETMQ)
                .tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_CONSUMER)
                .tag(Constants.Tags.PEER_ADDRESS, nameServerAddress)
                .tag(Constants.Tags.HOST, nameServerAddress)
                .tag(Constants.Tags.MESSAGE_BUS_DESTINATION, topic);
    }

    @Override
    public final void handleMethodException(IMethodInterceptContext context, Throwable t) {
        TransactionMetricUtils.handleException(context);
        TracerUtils.handleException(t);
    }

    protected void sendMetric(IMethodInterceptContext context, String status) {
        TransactionMetricBuilder transactionMetricBuilder = context.getAttachment(Constants.Keys.METRIC_BUILDER);
        if (transactionMetricBuilder != null) {
            transactionMetricBuilder.tag(Constants.Tags.MESSAGE_BUS_STATUS, status);
            MetricReporter.report(transactionMetricBuilder);
        }
    }
}
