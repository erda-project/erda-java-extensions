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
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

import java.util.function.BiConsumer;

/**
 * @author liuhaoyang
 * @date 2021/11/1 20:13
 */
public class RFutureTraceListener<T> implements FutureListener<T> {

    private final Scope spanScope;

    public RFutureTraceListener(Scope spanScope) {
        this.spanScope = spanScope;
    }

    @Override
    public void operationComplete(Future<T> tFuture) throws Exception {
        if (!tFuture.isSuccess()) {
            spanScope.span().tag(Constants.Tags.ERROR, Constants.Tags.ERROR_TRUE);
        }
        spanScope.close();
    }
}
