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
 */

package cloud.erda.agent.plugin.httpasyncclient.v4.wrapper;

import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import cloud.erda.agent.core.tracing.Scope;
import cloud.erda.agent.core.tracing.TracerManager;
import cloud.erda.agent.core.tracing.span.Span;
import cloud.erda.agent.core.utils.Constants;
import cloud.erda.agent.core.utils.TracerUtils;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricBuilder;
import cloud.erda.agent.plugin.app.insight.MetricReporter;
import cloud.erda.agent.plugin.app.insight.transaction.TransactionMetricUtils;
import cloud.erda.agent.plugin.httpasyncclient.v4.ThreadTransferInfo;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.concurrent.FutureCallback;

/**
 * a wrapper for {@link FutureCallback} so we can be notified when the hold response (when one or more request fails the
 * pipeline mode may not callback though we haven't support pipeline) received whether it fails or completed or
 * canceled.
 *
 * @author lican
 */
public class FutureCallbackWrapper<T> implements FutureCallback<T> {

    private FutureCallback<T> callback;

    public FutureCallbackWrapper(FutureCallback<T> callback) {
        this.callback = callback;
    }

    @Override
    public void completed(T o) {
        try {
            if (callback != null) {
                callback.completed(o);
            }
        } finally {
            try {
                this.finallyCompleted(o);
            } catch (Throwable throwable) {
                ILog log = LogManager.getLogger(FutureCallbackWrapper.class);
                log.error(throwable.getMessage(), throwable);
            }
        }
    }

    @Override
    public void failed(Exception e) {
        try {
            if (callback != null) {
                callback.failed(e);
            }
        } finally {
            try {
                this.finallyFailed(e);
            } catch (Throwable throwable) {
                ILog log = LogManager.getLogger(FutureCallbackWrapper.class);
                log.error(throwable.getMessage(), throwable);
            }
        }
    }

    @Override
    public void cancelled() {
        try {
            if (callback != null) {
                callback.cancelled();
            }
        } finally {
            try {
                this.finallyCancelled();
            } catch (Throwable throwable) {
                ILog log = LogManager.getLogger(FutureCallbackWrapper.class);
                log.error(throwable.getMessage(), throwable);
            }
        }
    }

    private void finallyCompleted(T o) {
        ThreadTransferInfo.LOCAL.remove();

        if (!(o instanceof HttpResponse)) {
            return;
        }
        HttpResponse response = (HttpResponse) o;
        StatusLine status = response.getStatusLine();

        TransactionMetricBuilder transactionMetricBuilder =
                TracerManager.tracer().context().getAttachment(Constants.Keys.METRIC_BUILDER);
        if (transactionMetricBuilder != null) {
//            Header[] headers = response.getHeaders(Constants.Carriers.RESPONSE_TERMINUS_KEY);
//            if (headers == null || headers.length <= 0) {
                if (status != null) {
                    TransactionMetricUtils.handleStatusCode(transactionMetricBuilder, status.getStatusCode());
                }
                MetricReporter.report(transactionMetricBuilder);
//            }
        }

        Scope scope = TracerManager.tracer().active();
        if (scope != null) {
            if (status != null) {
                TracerUtils.handleStatusCode(scope, status.getStatusCode());
            }
            scope.close();
        }
    }

    private void finallyFailed(Exception e) {
        ThreadTransferInfo.LOCAL.remove();

        Scope scope = TracerManager.tracer().active();
        if (scope != null) {
            TracerUtils.handleException(scope, e);
            scope.close();
        }
    }

    private void finallyCancelled() {
        ThreadTransferInfo.LOCAL.remove();

        Scope scope = TracerManager.tracer().active();
        if (scope != null && scope.span() != null) {
            Span span = scope.span();
            span.tag(Constants.Tags.ERROR, Constants.Tags.ERROR_TRUE);
            scope.close();
        }
    }
}
