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

package cloud.erda.agent.plugin.redisson.v3;

import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.tracing.span.SpanBuilder;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.ReflectUtils;
import cloud.erda.agent.core.utils.TracerUtils;
import cloud.erda.agent.plugin.app.insight.MetricReporter;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricBuilder;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceConstructorInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.redisson.client.RedisClient;
import org.redisson.client.RedisConnection;
import org.redisson.client.protocol.CommandData;
import org.redisson.client.protocol.CommandsData;

import java.net.InetSocketAddress;

/**
 * Reference from https://github.com/apache/skywalking-java/blob/main/apm-sniffer/apm-sdk-plugin/redisson-3.x-plugin/src/main/java/org/apache/skywalking/apm/plugin/redisson/v3/RedisConnectionMethodInterceptor.java
 */
public class RedisConnectionMethodInterceptor implements InstanceMethodsAroundInterceptor, InstanceConstructorInterceptor {

    private static final ILog logger = LogManager.getLogger(RedisConnectionMethodInterceptor.class);

    @Override
    public void onConstruct(EnhancedInstance objInst, Object[] allArguments) {
        String peer = (String) ((EnhancedInstance) allArguments[0]).getDynamicField();
        if (peer == null) {
            try {
                /*
                  In some high versions of redisson, such as 3.11.1.
                  The attribute address in the RedisClientConfig class changed from a lower version of the URI to a RedisURI.
                  But they all have the host and port attributes, so use the following code for compatibility.
                 */
                Object address = ReflectUtils.getObjectField(((RedisClient) allArguments[0]).getConfig(), "address");
                String host = (String) ReflectUtils.getObjectField(address, "host");
                String port = String.valueOf(ReflectUtils.getObjectField(address, "port"));
                peer = host + ":" + port;
            } catch (Exception e) {
                logger.warn("RedisConnection create peer error: ", e);
            }
        }
        objInst.setDynamicField(peer);
    }

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        String peer = (String) context.getInstance().getDynamicField();

        RedisConnection connection = (RedisConnection) context.getInstance();
        Channel channel = connection.getChannel();
        InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
        String dbInstance = remoteAddress.getAddress().getHostAddress() + ":" + remoteAddress.getPort();

        StringBuilder dbStatement = new StringBuilder();
        String operationName = "Redisson ";

        if (context.getArguments()[0] instanceof CommandsData) {
            operationName = operationName + "BATCH_EXECUTE";
            CommandsData commands = (CommandsData) context.getArguments()[0];
            for (CommandData commandData : commands.getCommands()) {
                addCommandData(dbStatement, commandData);
                dbStatement.append(";");
            }
        } else if (context.getArguments()[0] instanceof CommandData) {
            CommandData commandData = (CommandData) context.getArguments()[0];
            String command = commandData.getCommand().getName();
            operationName = operationName + command;
            addCommandData(dbStatement, commandData);
        }

        Tracer tracer = TracerManager.tracer();
        SpanContext spanContext = tracer.active() != null ? tracer.active().span().getContext() : null;
        SpanBuilder spanBuilder = tracer.buildSpan(operationName);
        Span span = spanBuilder.childOf(spanContext).startActive().span();
        span.tag(Constants.Tags.DB_TYPE, Constants.Tags.DB_TYPE_REDIS);
        span.tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_REDISSON);
        span.tag(Constants.Tags.PEER_SERVICE, peer);
        span.tag(Constants.Tags.SPAN_LAYER, Constants.Tags.SPAN_LAYER_CACHE);
        span.tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_CLIENT);
        span.tag(Constants.Tags.HOST, peer);
        span.tag(Constants.Tags.DB_STATEMENT, dbStatement.toString());
        span.tag(Constants.Tags.PEER_ADDRESS, peer);
        span.tag(Constants.Tags.PEER_HOSTNAME, peer);

        TransactionMetricBuilder transactionMetricBuilder = new TransactionMetricBuilder(Constants.Metrics.APPLICATION_CACHE, false);
        context.setAttachment(Constants.Keys.METRIC_BUILDER, transactionMetricBuilder);
        transactionMetricBuilder.tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_REDISSON)
                .tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_CLIENT)
                .tag(Constants.Tags.PEER_SERVICE, peer)
                .tag(Constants.Tags.PEER_ADDRESS, peer)
                .tag(Constants.Tags.PEER_HOSTNAME, peer)
                .tag(Constants.Tags.HOST, peer)
                .tag(Constants.Tags.DB_STATEMENT, dbStatement.toString())
                .tag(Constants.Tags.DB_TYPE, Constants.Tags.DB_TYPE_REDIS);
    }

    private void addCommandData(StringBuilder dbStatement, CommandData commandData) {
        dbStatement.append(commandData.getCommand().getName());
        if (commandData.getParams() != null) {
            for (Object param : commandData.getParams()) {
                dbStatement.append(" ").append(param instanceof ByteBuf ? "?" : String.valueOf(param.toString()));
            }
        }
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
        TransactionMetricUtils.handleException(context);
        TracerUtils.handleException(t);
    }
}
