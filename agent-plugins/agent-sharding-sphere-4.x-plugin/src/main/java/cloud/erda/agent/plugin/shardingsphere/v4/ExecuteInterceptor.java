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

package cloud.erda.agent.plugin.shardingsphere.v4;

import cloud.erda.agent.core.utils.Constants;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import cloud.erda.agent.core.tracing.Scope;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.TracerSnapshot;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.utils.TracerUtils;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricBuilder;
import cloud.erda.agent.plugin.app.insight.MetricReporter;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricUtils;
import org.apache.shardingsphere.core.execute.ShardingExecuteDataMap;

import java.util.Map;

/**
 * {@link ExecuteInterceptor} enhances {@link org.apache.shardingsphere.core.execute.sql.execute.SQLExecuteCallback},
 * creating a local span that records the execution of sql.
 */
public class ExecuteInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        Object data = ShardingExecuteDataMap.getDataMap().remove(Constant.TRACER_SNAPSHOT);
        if (!(data instanceof TracerSnapshot)) {
            Object[] args = context.getArguments();
            if (args != null && args.length >= 3 && args[2] instanceof Map) {
                data = ((Map) args[2]).remove(Constant.TRACER_SNAPSHOT);
            }
        }
        if (!(data instanceof TracerSnapshot)) {
            return;
        }
        TracerSnapshot snapshot = (TracerSnapshot) data;

        Span span = TracerManager.currentTracer().attach(snapshot).span();
        String statement = span.getTags().get(Constants.Tags.DB_STATEMENT);

        TransactionMetricBuilder transactionMetricBuilder = new TransactionMetricBuilder(Constants.Metrics.APPLICATION_DB, false);
        context.setAttachment(Constants.Keys.METRIC_BUILDER, transactionMetricBuilder);
        transactionMetricBuilder.tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_SHARDING_SPHERE)
                .tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_CLIENT)
                .tag(Constants.Tags.DB_STATEMENT, statement);
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        TransactionMetricBuilder transactionMetricBuilder = context.getAttachment(Constants.Keys.METRIC_BUILDER);
        if (transactionMetricBuilder != null) {
            MetricReporter.report(transactionMetricBuilder);
        }

        Scope scope = TracerManager.currentTracer().active();
        if (scope != null) {
            scope.close();
        }
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        TransactionMetricUtils.handleException(context);
        TracerUtils.handleException(t);
    }
}
