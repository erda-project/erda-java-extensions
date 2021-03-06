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
import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.utils.TracerUtils;
import org.apache.shardingsphere.core.execute.ShardingExecuteDataMap;

/**
 * {@link JDBCRootInvokeInterceptor} enhances
 * {@link org.apache.shardingsphere.shardingjdbc.executor.AbstractStatementExecutor},
 * creating a local span that records the overall execution of sql.
 */
public class JDBCRootInvokeInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        Object value = ShardingExecuteDataMap.getDataMap().remove(Constant.STATEMENT);
        if (!(value instanceof String)) {
            return;
        }
        String statement = (String) value;

        Tracer tracer = TracerManager.currentTracer();
        SpanContext spanContext = tracer.active() != null ? tracer.active().span().getContext() : null;
        Span span = tracer.buildSpan("/ShardingSphere/JDBC/" + statement).childOf(spanContext).startActive().span();
        span.tag(Constants.Tags.COMPONENT, Constants.Tags.COMPONENT_SHARDING_SPHERE);
        span.tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_CLIENT);
        span.tag(Constants.Tags.SPAN_LAYER, Constants.Tags.SPAN_LAYER_DB);
        span.tag(Constants.Tags.DB_STATEMENT, statement);

        ShardingExecuteDataMap.getDataMap().put(Constant.TRACER_SNAPSHOT, TracerManager.currentTracer().capture());
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        Object value = ShardingExecuteDataMap.getDataMap().get(Constant.STATEMENT);
        if (!(value instanceof String)) {
            return ret;
        }

        Scope scope = TracerManager.currentTracer().active();
        if (scope != null) {
            scope.close(false);
        }

        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        TracerUtils.handleException(t);
    }
}
