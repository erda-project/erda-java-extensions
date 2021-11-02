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

package cloud.erda.agent.plugin.redisson.v3;

import cloud.erda.agent.core.tracing.Scope;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.plugin.app.insight.MetricReporter;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricBuilder;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * @author liuhaoyang
 * @date 2021/11/1 15:25
 */
public class ChannelFutureTraceListener implements GenericFutureListener<ChannelFuture> {

    private final Scope spanScope;
    private final TransactionMetricBuilder metricBuilder;

    public ChannelFutureTraceListener(Scope spanScope, TransactionMetricBuilder metricBuilder) {
        this.spanScope = spanScope;
        this.metricBuilder = metricBuilder;
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        if (!channelFuture.isSuccess()) {
            metricBuilder.tag(Constants.Tags.ERROR, Constants.Tags.ERROR_TRUE);
            spanScope.span().tag(Constants.Tags.ERROR, Constants.Tags.ERROR_TRUE);
        }
        MetricReporter.report(metricBuilder);
        spanScope.close();
    }
}
