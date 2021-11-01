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
import cloud.erda.agent.core.tracing.Scope;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.span.Span;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;

import static cloud.erda.agent.core.utils.Constants.Tags.MESSAGE_BUS_STATUS;

/**
 * {@link MessageConcurrentlyConsumeInterceptor} set the process status after the {@link
 * org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently#consumeMessage(java.util.List,
 * org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext)} method execute.
 *
 * @author zhang xin
 */
public class MessageConcurrentlyConsumeInterceptor extends AbstractMessageConsumeInterceptor {

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        Scope scope = TracerManager.currentTracer().active();
        Span span = scope.span();
        ConsumeConcurrentlyStatus status = (ConsumeConcurrentlyStatus) ret;
        span.tag(MESSAGE_BUS_STATUS, status.name());

        sendMetric(context, status.name());
        scope.close();
        return ret;
    }
}

