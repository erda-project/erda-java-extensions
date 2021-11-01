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

import cloud.erda.agent.core.tracing.TracerSnapshot;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * @author liuhaoyang
 * @date 2021/11/1 15:02
 */
public class ChannelPromiseWrapper extends ChannelFutureWrapper implements ChannelPromise {

    private final ChannelPromise delegate;

    public ChannelPromiseWrapper(ChannelPromise delegate, TracerSnapshot tracerSnapshot) {
        super(delegate, tracerSnapshot);
        this.delegate = delegate;
    }

    @Override
    public ChannelPromise setSuccess(Void unused) {
        return delegate.setSuccess(unused);
    }

    @Override
    public boolean trySuccess(Void unused) {
        return delegate.trySuccess();
    }

    @Override
    public ChannelPromise setSuccess() {
        return delegate.setSuccess();
    }

    @Override
    public boolean trySuccess() {
        return delegate.trySuccess();
    }

    @Override
    public ChannelPromise setFailure(Throwable throwable) {
        return delegate.setFailure(throwable);
    }

    @Override
    public boolean tryFailure(Throwable throwable) {
        return delegate.tryFailure(throwable);
    }

    @Override
    public boolean setUncancellable() {
        return delegate.setUncancellable();
    }

    @Override
    public ChannelPromise unvoid() {
        return delegate.unvoid();
    }

    @Override
    public Channel channel() {
        return super.channel();
    }

    @Override
    public ChannelPromise syncUninterruptibly() {
        return (ChannelPromise) super.syncUninterruptibly();
    }


    @Override
    public ChannelPromise awaitUninterruptibly() {
        return (ChannelPromise) super.awaitUninterruptibly();
    }

    @Override
    public ChannelPromise await() throws InterruptedException {
        return (ChannelPromise) super.await();
    }

    @Override
    public ChannelPromise addListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        return (ChannelPromise) super.addListener(listener);
    }

    @Override
    public ChannelPromise addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
        return (ChannelPromise) super.addListeners(listeners);
    }

    @Override
    public ChannelPromise removeListener(GenericFutureListener<? extends Future<? super Void>> listener) {
        return (ChannelPromise) super.removeListener(listener);
    }

    @Override
    public ChannelPromise removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
        return (ChannelPromise) super.removeListeners(listeners);
    }

    @Override
    public ChannelPromise sync() throws InterruptedException {
        return (ChannelPromise) super.sync();
    }
}
