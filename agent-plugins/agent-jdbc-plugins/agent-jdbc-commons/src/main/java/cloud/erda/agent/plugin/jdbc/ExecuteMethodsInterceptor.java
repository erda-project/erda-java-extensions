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

import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import cloud.erda.agent.core.tracing.SpanContext;
import cloud.erda.agent.core.tracing.Tracer;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.tracing.span.SpanBuilder;
import cloud.erda.agent.core.utils.Constants;
import org.apache.skywalking.apm.agent.core.util.Strings;
import cloud.erda.agent.core.utils.TracerUtils;
import cloud.erda.agent.plugin.app.insight.AppMetricBuilder;
import cloud.erda.agent.plugin.app.insight.AppMetricRecorder;
import cloud.erda.agent.plugin.app.insight.AppMetricUtils;
import cloud.erda.agent.plugin.jdbc.define.StatementEnhanceInfos;
import cloud.erda.agent.plugin.jdbc.trace.ConnectionInfo;

/**
 * @author liuhaoyang
 * @since 2019-01-09 14:54
 **/
public class ExecuteMethodsInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) throws Throwable {
        if (context.getInstance() == null) return;

        StatementEnhanceInfos cacheObject = (StatementEnhanceInfos) context.getInstance().getDynamicField();
        ConnectionInfo connectInfo = cacheObject.getConnectionInfo();

        if (connectInfo != null) {
            String statement = cacheObject.getSql().replace("\n", "");

            Tracer tracer = TracerManager.tracer();
            SpanContext spanContext = tracer.active() != null ? tracer.active().span().getContext() : null;
            SpanBuilder spanBuilder = tracer.buildSpan(
                    buildOperationName(connectInfo, context.getMethod().getName(), cacheObject.getStatementName()));
            Span span = spanBuilder.childOf(spanContext).startActive().span();

            span.tag(Constants.Tags.PEER_HOSTNAME, connectInfo.getDatabasePeer());
            span.tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_CLIENT);
            span.tag(Constants.Tags.DB_INSTANCE, connectInfo.getDatabaseName());
            span.tag(Constants.Tags.DB_STATEMENT, statement);
            span.tag(Constants.Tags.DB_TYPE, connectInfo.getDBType());
            span.tag(Constants.Tags.SPAN_LAYER, Constants.Tags.SPAN_LAYER_DB);
            span.tag(Constants.Tags.COMPONENT, connectInfo.getComponent());
            span.tag(Constants.Tags.HOST, connectInfo.getDatabasePeer());

            if (Strings.isEmpty(statement)) {
                return;
            }
            AppMetricBuilder appMetricBuilder = new AppMetricBuilder(Constants.Metrics.APPLICATION_DB, false);
            context.setAttachment(Constants.Keys.METRIC_BUILDER, appMetricBuilder);
            appMetricBuilder.tag(Constants.Tags.COMPONENT, connectInfo.getComponent())
                    .tag(Constants.Tags.SPAN_KIND, Constants.Tags.SPAN_KIND_CLIENT)
                    .tag(Constants.Tags.PEER_HOSTNAME, connectInfo.getDatabasePeer())
                    .tag(Constants.Tags.HOST, connectInfo.getDatabasePeer())
                    .tag(Constants.Tags.DB_INSTANCE, connectInfo.getDatabaseName())
                    .tag(Constants.Tags.DB_STATEMENT, statement)
                    .tag(Constants.Tags.DB_TYPE, connectInfo.getDBType());
        }
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        if (context.getInstance() == null) return ret;

        AppMetricBuilder appMetricBuilder = context.getAttachment(Constants.Keys.METRIC_BUILDER);
        if (appMetricBuilder != null) {
            AppMetricRecorder.record(appMetricBuilder);
        }

        StatementEnhanceInfos cacheObject = (StatementEnhanceInfos) context.getInstance().getDynamicField();
        if (cacheObject.getConnectionInfo() != null) {
            TracerManager.tracer().active().close();
        }
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        if (context.getInstance() == null) return;

        StatementEnhanceInfos cacheObject = (StatementEnhanceInfos) context.getInstance().getDynamicField();
        if (cacheObject.getConnectionInfo() != null) {
            AppMetricUtils.handleException(context);
            TracerUtils.handleException(t);
        }
    }

    private String buildOperationName(ConnectionInfo connectionInfo, String methodName, String statementName) {
        return connectionInfo.getDBType() + "/" + statementName + "/" + methodName;
    }
}