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

package cloud.erda.agent.plugin.sdk.interceptors;

import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.plugin.app.insight.MetricBuilder;
import cloud.erda.agent.plugin.app.insight.MetricReporter;
import cloud.erda.agent.plugin.app.insight.invoke.InvokeMetricBuilder;
import cloud.erda.msp.monitor.metrics.MethodInvoke;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.context.IMethodInterceptContext;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.StaticMethodsAroundInterceptor;

/**
 * @author liuhaoyang
 * @date 2021/8/3 11:34
 */
public class MethodInvokeAnnotationInterceptor implements InstanceMethodsAroundInterceptor, StaticMethodsAroundInterceptor {

    @Override
    public void beforeMethod(IMethodInterceptContext context, MethodInterceptResult result) {
        MetricBuilder metricBuilder = InvokeMetricBuilder.New();
        metricBuilder.tag(Constants.Tags.CLASS, context.getOriginClass().getName());
        metricBuilder.tag(Constants.Tags.METHOD, context.getMethod().getName());
        metricBuilder.tag(Constants.Tags.INVOKE, getOperationName(context));
        context.setAttachment(Constants.Keys.METRIC_BUILDER, metricBuilder);
    }

    @Override
    public Object afterMethod(IMethodInterceptContext context, Object ret) throws Throwable {
        MetricBuilder metricBuilder = context.getAttachment(Constants.Keys.METRIC_BUILDER);
        if (metricBuilder != null) {
            MetricReporter.report(metricBuilder);
        }
        return ret;
    }

    @Override
    public void handleMethodException(IMethodInterceptContext context, Throwable t) {
        MetricBuilder metricBuilder = context.getAttachment(Constants.Keys.METRIC_BUILDER);
        if (metricBuilder != null) {
            metricBuilder.tag(Constants.Tags.ERROR, "true");
        }
    }

    private String getOperationName(IMethodInterceptContext context) {
        MethodInvoke methodInvoke = context.getMethod().getAnnotation(MethodInvoke.class);
        if (methodInvoke != null) {
            if (methodInvoke.value().length() > 0) {
                return methodInvoke.value();
            }
        }
        StringBuilder methodDescriptor = new StringBuilder();
        methodDescriptor.append(context.getOriginClass().getName());
        methodDescriptor.append(".");
        methodDescriptor.append(context.getMethod().getName());
        methodDescriptor.append("(");
        for (int i = 0; i < context.getMethod().getParameterTypes().length; i++) {
            methodDescriptor.append(context.getMethod().getParameterTypes()[i].getSimpleName());
            if (i < context.getMethod().getParameterTypes().length - 1) {
                methodDescriptor.append(",");
            }
        }
        methodDescriptor.append(")");
        return methodDescriptor.toString();
    }
}
