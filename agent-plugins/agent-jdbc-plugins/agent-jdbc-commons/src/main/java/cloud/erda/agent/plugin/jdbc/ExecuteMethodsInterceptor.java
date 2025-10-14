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

package cloud.erda.agent.plugin.jdbc;

import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.tracing.span.SpanBuilder;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.TracerUtils;
import cloud.erda.agent.plugin.app.insight.MetricReporter;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricBuilder;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricUtils;
import cloud.erda.agent.plugin.jdbc.define.StatementEnhanceInfos;
import cloud.erda.agent.plugin.jdbc.trace.ConnectionInfo;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.DynamicFieldEnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.agent.core.util.Strings;

/**
 * @author liuhaoyang
 * @since 2019-01-09 14:54
 **/
public class ExecuteMethodsInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        if (context.getInstance() == null) return;

        StatementEnhanceInfos cacheObject = (StatementEnhanceInfos) ((DynamicFieldEnhancedInstance) context.getInstance()).getDynamicField();
        if(cacheObject == null) return;
        ConnectionInfo connectInfo = cacheObject.getConnectionInfo();

        if (connectInfo != null) {
            // 获取SQL，如果StatementEnhanceInfos中的SQL为空，则从方法参数中获取
            String statement = cacheObject.getSql();
            if (statement == null || statement.isEmpty()) {
                // 对于Statement.executeQuery(String sql)等方法，SQL作为第一个参数传入
                if (context.getArguments() != null && context.getArguments().length > 0 && context.getArguments()[0] instanceof String) {
                    statement = (String) context.getArguments()[0];
                } else {
                    statement = "";
                }
            }
            
            if (statement != null) {
                statement = statement.replace("\n", "");
            }

            Tracer tracer = TracerManager.currentTracer();
            SpanContext spanContext = tracer.active() != null ? tracer.active().span().getContext() : null;
            SpanBuilder spanBuilder = tracer.buildSpan(
                    buildOperationName(connectInfo, context.getMethod().getName(), cacheObject.getStatementName()));
            Span span = spanBuilder.childOf(spanContext).startActive().span();
            span.tag(Constants.Tags.PEER_HOSTNAME, connectInfo.getDatabasePeer());
            span.tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_CLIENT);
            span.tag(Constants.Tags.DB_INSTANCE, connectInfo.getDatabaseName());
            span.tag(Constants.Tags.DB_STATEMENT, statement);
            span.tag(Constants.Tags.DB_SYSTEM, connectInfo.getDBType());
            span.tag(Constants.Tags.SPAN_LAYER, Constants.Tags.SPAN_LAYER_DB);
            span.tag(Constants.Tags.COMPONENT, connectInfo.getComponent());
            span.tag(Constants.Tags.DB_HOST, connectInfo.getDatabasePeer());
            span.tag(Constants.Tags.PEER_ADDRESS, connectInfo.getDatabasePeer());
            span.tag(Constants.Tags.PEER_SERVICE, connectInfo.getDatabasePeer());
            if (Strings.isEmpty(statement)) {
                return;
            }
            TransactionMetricBuilder transactionMetricBuilder = new TransactionMetricBuilder(Constants.Metrics.APPLICATION_DB, false);
            context.setAttachment(Constants.Keys.METRIC_BUILDER, transactionMetricBuilder);
            transactionMetricBuilder.tag(Constants.Tags.COMPONENT, connectInfo.getComponent())
                    .tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_CLIENT)
                    .tag(Constants.Tags.PEER_HOSTNAME, connectInfo.getDatabasePeer())
                    .tag(Constants.Tags.PEER_ADDRESS, connectInfo.getDatabasePeer())
                    .tag(Constants.Tags.PEER_SERVICE, connectInfo.getDatabasePeer())
                    .tag(Constants.Tags.DB_HOST, connectInfo.getDatabasePeer())
                    .tag(Constants.Tags.DB_INSTANCE, connectInfo.getDatabaseName())
                    .tag(Constants.Tags.DB_STATEMENT, statement)
                    .tag(Constants.Tags.DB_SYSTEM, connectInfo.getDBType());
        }
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        if (context.getInstance() == null) return ret;

        TransactionMetricBuilder transactionMetricBuilder = context.getAttachment(Constants.Keys.METRIC_BUILDER);
        if (transactionMetricBuilder != null) {
            MetricReporter.report(transactionMetricBuilder);
        }

        StatementEnhanceInfos cacheObject = (StatementEnhanceInfos) ((DynamicFieldEnhancedInstance) context.getInstance()).getDynamicField();
        if (cacheObject != null && cacheObject.getConnectionInfo() != null) {
            // 添加null检查，防止active()返回null导致空指针异常
            Tracer tracer = TracerManager.currentTracer();
            if (tracer != null && tracer.active() != null) {
                tracer.active().close();
            }
        }
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        if (context.getInstance() == null) return;

        StatementEnhanceInfos cacheObject = (StatementEnhanceInfos) ((DynamicFieldEnhancedInstance) context.getInstance()).getDynamicField();
        if (cacheObject != null && cacheObject.getConnectionInfo() != null) {
            TransactionMetricUtils.handleException(context);
            TracerUtils.handleException(t);
            // 添加null检查，防止active()返回null导致空指针异常
            Tracer tracer = TracerManager.currentTracer();
            if (tracer != null && tracer.active() != null) {
                tracer.active().close();
            }
        }
    }

    private String buildOperationName(ConnectionInfo connectionInfo, String methodName, String statementName) {
        return connectionInfo.getDBType() + "/" + statementName + "/" + methodName;
    }
}
