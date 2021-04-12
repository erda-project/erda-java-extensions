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

package io.terminus.spot.plugin.shardingsphere.v4;

import cloud.erda.agent.core.utils.Constants;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import cloud.erda.agent.core.tracing.Scope;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.TracerSnapshot;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.utils.TracerUtils;
import cloud.erda.plugin.app.insight.AppMetricBuilder;
import cloud.erda.plugin.app.insight.AppMetricRecorder;
import cloud.erda.plugin.app.insight.AppMetricUtils;
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
                data = ((Map)args[2]).remove(Constant.TRACER_SNAPSHOT);
            }
        }
        if (!(data instanceof TracerSnapshot)) {
            return;
        }
        TracerSnapshot snapshot = (TracerSnapshot)data;

        Span span = TracerManager.tracer().attach(snapshot).span();
        String statement = span.getTags().get(Constants.Tags.DB_STATEMENT);

        AppMetricBuilder appMetricBuilder = new AppMetricBuilder(Constants.Metrics.APPLICATION_DB, false);
        context.setAttachment(Constants.Keys.METRIC_BUILDER, appMetricBuilder);
        appMetricBuilder.tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_SHARDING_SPHERE)
            .tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_CLIENT)
            .tag(Constants.Tags.DB_STATEMENT, statement);
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        AppMetricBuilder appMetricBuilder = context.getAttachment(Constants.Keys.METRIC_BUILDER);
        if (appMetricBuilder != null) {
            AppMetricRecorder.record(appMetricBuilder);
        }

        Scope scope = TracerManager.tracer().active();
        if (scope != null) {
            scope.close();
        }
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        AppMetricUtils.handleException(context);
        TracerUtils.handleException(t);
    }
}
